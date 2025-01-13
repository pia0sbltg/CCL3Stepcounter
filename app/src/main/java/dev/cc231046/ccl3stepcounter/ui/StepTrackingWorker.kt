package dev.cc231046.ccl3stepcounter.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StepTrackingWorker (context: Context, workerParams: WorkerParameters): CoroutineWorker(context,workerParams){
    private val stepsDao = StepsDatabase.getDatabase(applicationContext).stepsDao()

    override suspend fun doWork(): Result{

        val today = LocalDate.now().toString()
        val currentStepCount = getCurrentStepCount()

        val storedSteps = withContext(Dispatchers.IO){
            stepsDao.getStepsForDate(today)
        }
        if(storedSteps == null){
            withContext(Dispatchers.IO){
                stepsDao.insertSteps(
                    StepEntity(
                        date= today,
                        initialStepCount = currentStepCount,
                        totalSteps = 0
                    )
                )
            }
        }

        updatePreviousDays(today, currentStepCount)

        return Result.success()
    }
    private fun getCurrentStepCount():Int{
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var currentSteps = 0

        if(stepSensor!= null){
            val sensorEventListener = object: SensorEventListener{
                override fun onSensorChanged(event: SensorEvent?) {
                    currentSteps = event?.values?.get(0)?.toInt() ?: 0
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Not used
                }
            }
            sensorManager.registerListener(sensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.unregisterListener(sensorEventListener)
        }
        return currentSteps
    }

    private suspend fun updatePreviousDays(today: String, currentStepCount: Int){
        val previousEntries = stepsDao.getAllSteps()

        previousEntries?.forEach{entry ->
            if(entry.date != today && entry.totalSteps==0){
                val stepsForThatDay = currentStepCount - entry.initialStepCount
                stepsDao.insertSteps(entry.copy(totalSteps = stepsForThatDay))
            }

        }
    }

}