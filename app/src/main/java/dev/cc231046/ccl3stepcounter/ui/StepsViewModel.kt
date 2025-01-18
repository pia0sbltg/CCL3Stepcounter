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
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StepsViewModel(
    private val stepsDao: StepsDao,
    private val goalsDao: GoalsDao,
    private val petDao: PetDao,
    private val applicationContext: Context) : ViewModel() {

    private val stepTracker = StepTracker(applicationContext,stepsDao, goalsDao){
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


    init {
        viewModelScope.launch {
           /*
           stepsDao.deleteEverything()
            goalsDao.deleteEveryGoal()
            */
            //stepsDao.deleteToday(LocalDate.now().toString())
            stepTracker.getOrInitializeInitialSteps()
            loadPetState()
            startCounting()
            checkDailyGoal()
            loadStepHistory()
            loadTodayGoal()
        }
    }

    private fun startCounting() {
        stepTracker.startTracking { updatedSteps ->
            _currentSteps.postValue(updatedSteps) // Update LiveData with new step count
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepTracker.stopTracking()
    }

    private suspend fun loadPetState() {
        val pet = petDao.getPet() ?: PetEntity()
        _petState.postValue(pet)
    }

    private suspend fun checkDailyGoal(){
        val today = LocalDate.now().toString()
        val todaySteps = stepsDao.getStepsForDate(today )
        val goalsForToday = goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value)

        if(todaySteps != null && goalsForToday.isNotEmpty()){
            val highestGoal = goalsForToday.maxOf { it.stepGoal }
            val goalReached = todaySteps.totalSteps >= highestGoal
            stepsDao.updateGoalReached(today,goalReached)
        }else{
            stepsDao.updateGoalReached(today,false)
        }
    }

    private suspend fun loadStepHistory(){
        _stepHistory.postValue(stepsDao.getLastSixDays())
    }

    private suspend fun loadTodayGoal() {
        _todayGoal.postValue(goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value).maxOfOrNull { it.stepGoal } ?: 0)
    }

    suspend fun feedPet() {
        val today = LocalDate.now().toString()
        val pet = petDao.getPet() ?: PetEntity()

        if (pet.lastFedDate == today) return // Already fed today

        val newFeeds = pet.feeds + 1
        val newStage = if (newFeeds >= 10) pet.currentStage + 1 else pet.currentStage
        val newFeedsReset = if (newFeeds >= 10) 0 else newFeeds

        petDao.updatePet(
            feeds = newFeedsReset,
            currentStage = newStage,
            lastFedDate = today
        )

        loadPetState()
    }

}

class StepTracker(private val context: Context, private val stepsDao: StepsDao, private val goalsDao: GoalsDao,private val onGoalUpdated: suspend () -> Unit) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var initialStepCount: Int? = null
    private var onStepsUpdated: ((Int) -> Unit)? = null
    private var calcTodaySteps:Int = 0


    suspend fun getOrInitializeInitialSteps(): Int {
        val today = LocalDate.now().toString()

        val storedSteps = stepsDao.getStepsForDate(today)
        if (storedSteps != null) {
            initialStepCount = storedSteps.initialStepCount
            println("DEBUG: Loaded initialStepCount from DB: $initialStepCount")
            return storedSteps.totalSteps
        }
        println("DEBUG: Initialized initialStepCount: $initialStepCount")
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
                // Retrieve today's entry or initialize it if not present
                if (initialStepCount == null) {
                    val storedSteps = stepsDao.getStepsForDate(today)

                    if (storedSteps == null) {
                        val yesterday = LocalDate.now().minusDays(1).toString()
                        val yesterdaySteps = stepsDao.getStepsForDate(yesterday)

                        if(yesterdaySteps!=null){
                            calcTodaySteps = totalDeviceSteps - (yesterdaySteps.initialStepCount+yesterdaySteps.totalSteps)
                            initialStepCount = totalDeviceSteps-calcTodaySteps
                        }else {
                            initialStepCount = totalDeviceSteps
                            stepsDao.insertOrUpdateSteps(
                                StepEntity(
                                    date = today,
                                    initialStepCount = totalDeviceSteps,
                                    totalSteps = 0
                                )
                            )
                            println("DEBUG: First-time initialization in onSensorChanged. InitialStepCount: $initialStepCount")
                        }
                    } else {

                        initialStepCount = storedSteps.initialStepCount
                        println("DEBUG: Loaded initialStepCount in onSensorChanged: $initialStepCount")
                    }
                }
                // Calculate steps for today
                var todaySteps = totalDeviceSteps - (initialStepCount?: totalDeviceSteps)
                if(calcTodaySteps>todaySteps){
                    todaySteps=calcTodaySteps
                }

                println(todaySteps)
                println(initialStepCount)
                stepsDao.insertOrUpdateSteps(
                    StepEntity(
                        date = today,
                        initialStepCount = initialStepCount!!,
                        totalSteps = todaySteps
                    )
                )

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