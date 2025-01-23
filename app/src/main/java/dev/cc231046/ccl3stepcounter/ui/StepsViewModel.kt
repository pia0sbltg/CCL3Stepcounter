package dev.cc231046.ccl3stepcounter.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import dev.cc231046.ccl3stepcounter.data.PetDao
import dev.cc231046.ccl3stepcounter.data.PetEntity
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

class StepsViewModel(
    private val stepsDao: StepsDao,
    val goalsDao: GoalsDao,
    private val petDao: PetDao,
    private val applicationContext: Context
) : ViewModel() {

    private val stepTracker = StepTracker(applicationContext, stepsDao, goalsDao, { steps -> addStepsForRevival(steps) }) {
        loadStepHistory()
    }
    private val _currentSteps = MutableLiveData(0)
    val currentSteps: LiveData<Int> = _currentSteps

    private val _stepHistory = MutableLiveData<List<StepEntity>>()
    val stepHistory: LiveData<List<StepEntity>> = _stepHistory

    private val _todayGoal = MutableLiveData<Int>()
    val todayGoal: LiveData<Int> = _todayGoal

    private val _petState = MutableLiveData<PetEntity?>()
    val petState: LiveData<PetEntity?> = _petState

    var reviveButton = (false)

    private val today = LocalDate.now()

    var stepsToRevivalLimit :Int = 0

    init {
        viewModelScope.launch {
            /*
            stepsDao.deleteEverything()
             goalsDao.deleteEveryGoal()
             petDao.resetPet()

            */
            //stepsDao.deleteToday(LocalDate.now().toString())
            loadPetState()
            setLimit()
            stepTracker.getOrInitializeInitialSteps()
            startCounting()
            loadStepHistory()
            loadTodayGoal()
            checkDailyGoal()
            checkPet()
            updateAnimationVersionBasedOnTime()
           // testPrep()
        }
    }

    private fun setLimit(){
        stepsToRevivalLimit = 5000 + (5000 * _petState.value?.deaths!!)
        println("stepsToRevive $stepsToRevivalLimit")
    }

    private suspend fun testPrep(){
        if(_petState.value?.coins!! < 49) {
            withContext(Dispatchers.IO) {
                _petState.value?.let {
                    petDao.updatePet(
                        feeds = it.feeds,
                        coins = 49,
                        currentStage = it.currentStage,
                        lastFedDate = "01-01-2000",
                        stepsForRevival = 0
                    )
                }
                loadPetState()
            }
        }
    }

    private fun startCounting() {
        stepTracker.startTracking { updatedSteps ->
            _currentSteps.postValue(updatedSteps)
        }
    }

    private suspend fun checkPet(){
        val pet = petDao.getPet() ?: return
        val lastFedDate = if (pet.lastFedDate.isEmpty()) {
            LocalDate.now()
        } else {
                LocalDate.parse(pet.lastFedDate)
        }
        val daysSinceFed = today.toEpochDay() - lastFedDate.toEpochDay()

        println("Dayssincefed $daysSinceFed")
        println("stepstorevival ${pet.stepsForRevival}")

        withContext(Dispatchers.IO){
            if (daysSinceFed >= 5 && pet.currentStage!=4) {
                petDao.updatePet(
                    feeds = pet.feeds,
                    coins = pet.coins,
                    lastFedDate = lastFedDate.toString(),
                    stepsForRevival = 0,
                    currentStage = 4
                )
                petDao.addDeath()
            }

            loadPetState()
            setLimit()
        }
    }

    private suspend fun checkDailyGoal() {
        val todaySteps = stepsDao.getStepsForDate(today.toString())
        val goalsForToday = goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value)

        if(todaySteps != null && goalsForToday.isNotEmpty()){
            val highestGoal = goalsForToday.maxOf { it.stepGoal }
            val goalReached = todaySteps.totalSteps >= highestGoal
            stepsDao.updateGoalReached(today.toString(),goalReached)
        }else{
            stepsDao.updateGoalReached(today.toString(),false)
        }
    }

    private suspend fun loadStepHistory() {
        val stepHistory = stepsDao.getLastSixDays()
        _stepHistory.postValue(stepHistory)
    }

    private suspend fun loadTodayGoal() {
        _todayGoal.postValue(
            goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value).maxOfOrNull { it.stepGoal }
                ?: 0)
    }

    private suspend fun loadPetState(){
        withContext(Dispatchers.IO) {
            var pet = petDao.getPet()
            if (pet == null) {
                println("init Pet")
                pet = PetEntity(id = 1, currentStage = 1, feeds = 0, lastFedDate = "")
                petDao.insertOrUpdatePet(pet)
            }
            withContext(Dispatchers.Main) {
                _petState.value = pet
            }
        }
    }

    suspend fun feedPet() {
        val today = LocalDate.now().toString()
        val pet = petDao.getPet() ?: PetEntity()

        if (pet.lastFedDate == today) {
            println("DEBUG: PET ALREADY FED TODAY: $pet")
            return
        }

        val newFeeds = pet.feeds + 1
        val ogStage = pet.currentStage
        val newStage = 3
        val newCoins = pet.coins +1
        //val newStage =2
        val newFeedsReset = if (newFeeds >= 10) 0 else newFeeds

        withContext(Dispatchers.IO) {
            petDao.updatePet(
                feeds = newFeedsReset,
                currentStage = newStage,
                coins= newCoins,
                lastFedDate = today,
                stepsForRevival = pet.stepsForRevival
            )

            if (newStage == 3) {
                loadPetState()
                kotlinx.coroutines.delay(4000)

                petDao.updatePet(
                    feeds = newFeedsReset,
                    currentStage = ogStage,
                    coins = newCoins,
                    lastFedDate = today,
                    stepsForRevival = pet.stepsForRevival
                )
            }
        }

        loadPetState() // Refresh state in LiveData
    }

    private fun updateAnimationVersionBasedOnTime(){
        viewModelScope.launch {
            while (true){
                val currentPetState = _petState.value

                if (currentPetState != null) {

                    val currentTime = LocalTime.now()
                    val newAnimVersion = when {
                        currentPetState.currentStage == 4 -> 4 // "dead" -> now just in foreversleep lol
                        currentTime.isAfter(LocalTime.of(22, 0)) || currentTime.isBefore(LocalTime.of(8, 0)) -> 2 // Sleepy
                        else -> 1
                    }
                    if (currentPetState.currentStage != newAnimVersion) {
                        withContext(Dispatchers.Main) {
                            petDao.updatePet(
                                feeds = currentPetState.feeds,
                                currentStage = newAnimVersion,
                                coins = currentPetState.coins,
                                lastFedDate = currentPetState.lastFedDate,
                                stepsForRevival = currentPetState.stepsForRevival
                            )
                            loadPetState()
                        }
                    }
                }
                kotlinx.coroutines.delay(60_000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepTracker.stopTracking()
    }

    fun wakeUpAnimal(){
        viewModelScope.launch(Dispatchers.IO) {
            val pet = petDao.getPet() ?: return@launch
            if(pet.currentStage ==4 && pet.stepsForRevival >= stepsToRevivalLimit){
                val newCoins = pet.coins +1
                val newFeeds = pet.feeds +1
                petDao.updatePet(
                    stepsForRevival = 0,
                    currentStage = 1,
                    lastFedDate = LocalDate.now().toString() ,
                    coins = newCoins ,
                    feeds = newFeeds
                )
            }
            loadPetState()
        }
        reviveButton =false
    }

    private fun addStepsForRevival(steps: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val pet = petDao.getPet() ?: return@launch

            if (pet.currentStage == 4) {
                val lastDate = pet.lastRevivalStepsDate
                val lastStepsAdded = if (lastDate == today.toString()) pet.lastRevivalStepsAdded else 0
                val stepsToAdd = steps - lastStepsAdded

                val newStepsForRevival = pet.stepsForRevival + if (stepsToAdd > 0) stepsToAdd else 0

                petDao.updatePet(
                    stepsForRevival = newStepsForRevival,
                    currentStage =  4,
                    lastFedDate = pet.lastFedDate,
                    coins = pet.coins,
                    feeds = pet.feeds
                )
                petDao.updateLastRevivalSteps(steps, today.toString())
                loadPetState()

                if( newStepsForRevival >= stepsToRevivalLimit){
                    println("$newStepsForRevival $stepsToRevivalLimit")
                    reviveButton = true
                    println("button set true")
                }
            }
        }
    }

}

class StepTracker(
    private val context: Context,
    private val stepsDao: StepsDao,
    private val goalsDao: GoalsDao,
    private val addStepsForRevival: (Int) -> Unit,
    private val onGoalUpdated: suspend () -> Unit
) : SensorEventListener {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var initialStepCount: Int? = null
    private var onStepsUpdated: ((Int) -> Unit)? = null
    private var calcTodaySteps: Int = 0

    suspend fun getOrInitializeInitialSteps(): Int {
        val today = LocalDate.now().toString()

        val storedSteps = stepsDao.getStepsForDate(today)
        if (storedSteps != null) {
            initialStepCount = storedSteps.initialStepCount
            return storedSteps.totalSteps
        }
        return 0
    }

    fun startTracking(onStepsUpdated: (Int) -> Unit) {
        this.onStepsUpdated = onStepsUpdated
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
        onStepsUpdated = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val totalDeviceSteps = it.values[0].toInt()
            val today = LocalDate.now().toString()

            CoroutineScope(Dispatchers.IO).launch {
                val goalForDay= goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value).maxOfOrNull { it.stepGoal } ?: 0

                // Retrieve today's entry or initialize it if not present
                if (initialStepCount == null) {
                    val storedSteps = stepsDao.getStepsForDate(today)

                    if (storedSteps == null) {
                        val yesterday = LocalDate.now().minusDays(1).toString()
                        val yesterdaySteps = stepsDao.getStepsForDate(yesterday)

                        if (yesterdaySteps != null) {
                            calcTodaySteps =
                                totalDeviceSteps - (yesterdaySteps.initialStepCount + yesterdaySteps.totalSteps)
                            initialStepCount = totalDeviceSteps - calcTodaySteps
                        } else {
                            initialStepCount = totalDeviceSteps
                            stepsDao.insertOrUpdateSteps(
                                StepEntity(
                                    date = today,
                                    initialStepCount = totalDeviceSteps,
                                    totalSteps = 0,
                                    stepGoal = goalForDay
                                )
                            )
                            println("DEBUG: First-time initialization in onSensorChanged. InitialStepCount: $initialStepCount")
                        }
                    } else {
                        initialStepCount = storedSteps.initialStepCount
                    }
                }
                // Calculate steps for today
                var todaySteps = totalDeviceSteps - (initialStepCount ?: totalDeviceSteps)
                if (calcTodaySteps > todaySteps) {
                    todaySteps = calcTodaySteps
                }

                stepsDao.insertOrUpdateSteps(
                    StepEntity(
                        date = today,
                        initialStepCount = initialStepCount!!,
                        totalSteps = todaySteps,
                        stepGoal = goalForDay
                    )
                )
                addStepsForRevival(todaySteps)

                val goalsForToday = goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value)
                if (goalsForToday.isNotEmpty()) {
                    val highestGoal = goalsForToday.maxOf { it.stepGoal }
                    val goalReached = todaySteps >= highestGoal
                    val storedSteps = stepsDao.getStepsForDate(today)

                    if (storedSteps?.goalReached != goalReached) {
                        stepsDao.updateGoalReached(today, goalReached)
                    }
                }

                withContext(Dispatchers.Main) {
                    onStepsUpdated?.invoke(todaySteps)
                    onGoalUpdated()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Neyy
    }


}