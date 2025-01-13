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
import dev.cc231046.ccl3stepcounter.data.StepsDao

class StepsViewModel(stepsDao: StepsDao, context: Context) : ViewModel() {
    private val stepCounter = StepCounter(context)

    private val _currentSteps = MutableLiveData(0)
    val currentSteps: LiveData<Int> = _currentSteps

    fun startCounting() {
        stepCounter.startListening { updatedSteps ->
            _currentSteps.postValue(updatedSteps) // Update LiveData with new step count
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepCounter.stopListening()
    }
}

class StepCounter(private val context: Context) : SensorEventListener {
    private val sensorManager: SensorManager = context.createAttributionContext("StepCounter")
        .getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var stepCount: Int = 0
    private var initialStepCount: Int? = null // To track steps since startListening()

    private var isListening = false
    private var onStepCountUpdated: ((Int) -> Unit)? = null

    fun startListening(onStepCountUpdated: (Int) -> Unit) {
        this.onStepCountUpdated = onStepCountUpdated
        if (!isListening) {
            stepSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                isListening = true
            }
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            initialStepCount = null // Reset the offset when stopping
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val totalSteps = it.values[0].toInt()

            if (initialStepCount == null) {
                initialStepCount = totalSteps // Set initial value on the first event
            }

            // Compute session-specific steps
            stepCount = totalSteps - (initialStepCount ?: 0)

            // Notify the listener
            onStepCountUpdated?.invoke(stepCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing for now
    }
}