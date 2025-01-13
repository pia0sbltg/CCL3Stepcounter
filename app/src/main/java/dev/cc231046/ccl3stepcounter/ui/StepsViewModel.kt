package dev.cc231046.ccl3stepcounter.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.LocalDate
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StepsViewModel(stepsDao: StepsDao, context: Context) : ViewModel() {
    private val stepTracker = StepTracker(context,stepsDao)

    private val _currentSteps = MutableLiveData(0)
    val currentSteps: LiveData<Int> = _currentSteps

    init {
        viewModelScope.launch {
            stepTracker.getOrInitializeInitialSteps()
            startCounting()
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

}

class StepTracker(private val context: Context, private val stepsDao: StepsDao) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var initialStepCount: Int? = null
    private var onStepsUpdated: ((Int) -> Unit)? = null

    suspend fun getOrInitializeInitialSteps(): Int {
        val today = java.time.LocalDate.now().toString()
        val storedSteps = stepsDao.getStepsForDate(today)
        return if (storedSteps != null) {
            initialStepCount = storedSteps.initialStepCount
            storedSteps.initialStepCount
        } else {
            0
        }
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
            val today = java.time.LocalDate.now().toString()
            print(today)

            if (initialStepCount == null) {
                // Save the initial step count to the database if it´s not there
                initialStepCount = totalDeviceSteps
                CoroutineScope(Dispatchers.IO).launch {
                    stepsDao.insertSteps(
                        StepEntity(
                            date = today,
                            initialStepCount = totalDeviceSteps,
                            totalSteps = 0
                        )
                    )
                }
            }

            val todaySteps = totalDeviceSteps - (initialStepCount ?: totalDeviceSteps)
            CoroutineScope(Dispatchers.IO).launch {
                stepsDao.insertSteps(
                    StepEntity(
                        date = today,
                        initialStepCount =  initialStepCount!!,
                        totalSteps = todaySteps
                    )
                )
            }
            onStepsUpdated?.invoke(todaySteps)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}