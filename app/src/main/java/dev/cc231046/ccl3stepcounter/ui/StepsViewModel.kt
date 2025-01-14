package dev.cc231046.ccl3stepcounter.ui

import android.annotation.SuppressLint
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
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StepsViewModel(private val stepsDao: StepsDao, private val goalsDao: GoalsDao, private val applicationContext: Context) : ViewModel() {
    private val stepTracker = StepTracker(applicationContext,stepsDao)

    private val _currentSteps = MutableLiveData(0)
    val currentSteps: LiveData<Int> = _currentSteps

    private val _stepHistory = MutableLiveData<List<StepEntity>>()
    val stepHistory: LiveData<List<StepEntity>> = _stepHistory

    private val _todayGoal = MutableLiveData<Int>()
    val todayGoal: LiveData<Int> = _todayGoal

    init {
        viewModelScope.launch {
           /*
           stepsDao.deleteEverything()
            goalsDao.deleteEveryGoal()
            */
            stepTracker.getOrInitializeInitialSteps()
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


    private suspend fun checkDailyGoal(){
        val today = LocalDate.now().toString()
        val todaySteps = stepsDao.getStepsForDate(today )
        val goalsForToday = goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value)

        if(todaySteps != null && goalsForToday.isNotEmpty()){
            val highestGoal = goalsForToday.maxOf { it.stepGoal }
            val goalReached = todaySteps.totalSteps >= highestGoal

            stepsDao.updateGoalReached(today,goalReached)
        }
    }

    private suspend fun loadStepHistory(){
        _stepHistory.postValue(stepsDao.getLastSixDays())
    }

    private suspend fun loadTodayGoal() {
        _todayGoal.postValue(goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value).maxOfOrNull { it.stepGoal } ?: 0)
    }

}

class StepTracker(private val context: Context, private val stepsDao: StepsDao) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var initialStepCount: Int? = null
    private var onStepsUpdated: ((Int) -> Unit)? = null

    suspend fun getOrInitializeInitialSteps(): Int {
        val today = LocalDate.now().toString()

        val storedSteps = stepsDao.getStepsForDate(today)
        if (storedSteps != null) {
            // Use the existing initialStepCount and totalSteps
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

            println("DEBUG totaldevicesteps: $totalDeviceSteps")


            CoroutineScope(Dispatchers.IO).launch {
                // Retrieve today's entry or initialize it if not present
                if (initialStepCount == null) {
                    val storedSteps = stepsDao.getStepsForDate(today)
                    if (storedSteps == null) {
                        initialStepCount = totalDeviceSteps
                        stepsDao.insertOrUpdateSteps(
                            StepEntity(
                                date = today,
                                initialStepCount = totalDeviceSteps,
                                totalSteps = 0
                            )
                        )
                        println("DEBUG: First-time initialization in onSensorChanged. InitialStepCount: $initialStepCount")
                    } else {
                        println(storedSteps)
                        initialStepCount = storedSteps.initialStepCount
                        println("DEBUG: Loaded initialStepCount in onSensorChanged: $initialStepCount")
                    }
                }

                // Calculate steps for today

                val todaySteps = totalDeviceSteps - (initialStepCount?: totalDeviceSteps)

                println(todaySteps)
                println(initialStepCount)
                stepsDao.insertOrUpdateSteps(
                    StepEntity(
                        date = today,
                        initialStepCount = initialStepCount!!,
                        totalSteps = todaySteps
                    )
                )

                // Update UI
                withContext(Dispatchers.Main) {
                    onStepsUpdated?.invoke(todaySteps)
                }
            }
        }
    }


    private fun getCurrentStepCount(): Int {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var currentSteps = 0

        if (stepSensor != null) {
            val sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    currentSteps = event?.values?.get(0)?.toInt() ?: 0
                    println("DEBUG: SensorEventListener received step count: $currentSteps")
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Not used
                }
            }
            sensorManager.registerListener(sensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Thread.sleep(100) // Allow some time for the listener to receive a value
            sensorManager.unregisterListener(sensorEventListener)
        } else {
            println("DEBUG: Step sensor not available")
        }
        println("DEBUG: getCurrentStepCount returning: $currentSteps")
        return currentSteps
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Neyy
    }


}