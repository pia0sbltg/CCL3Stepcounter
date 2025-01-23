package dev.cc231046.ccl3stepcounter.ui

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dev.cc231046.ccl3stepcounter.data.GoalEntity
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.math.max

@SuppressLint("SpecifyJobSchedulerIdRange")
class StepTrackingJobService: JobService(){
    private val stepsDao = StepsDatabase.getDatabase(applicationContext).stepsDao()
    val goalsDao = StepsDatabase.getDatabase(applicationContext).goalsDao()

    override fun onStartJob(params: JobParameters?): Boolean {
        // Start the job in the background
        CoroutineScope(Dispatchers.IO).launch {
            performStepTracking()
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        //Rescedule Job if interrupted
        return true

    }

    private suspend fun performStepTracking(){
        val today= LocalDate.now().toString()
        val currentStepCount = getCurrentStepCount(applicationContext)
        val storedSteps = withContext(Dispatchers.IO){
            stepsDao.getStepsForDate(today)
        }
        val goalsForToday = goalsDao.getGoalsForDay(LocalDate.now().dayOfWeek.value)
        val goal = goalsForToday.maxOf { it.stepGoal }

        if(storedSteps === null){
            withContext(Dispatchers.IO) {
                stepsDao.insertOrUpdateSteps(
                    StepEntity(
                        date = today,
                        initialStepCount = currentStepCount,
                        totalSteps = 0,
                        stepGoal = goal
                    )
                )
            }
        }

        if (storedSteps != null && goalsForToday.isNotEmpty()) {
            val highestGoal = goalsForToday.maxOf { it.stepGoal }
            val goalReached = storedSteps.totalSteps >= highestGoal
            stepsDao.updateGoalReached(today, goalReached)
        }

    }


    private fun getCurrentStepCount(applicationContext: Context): Int{
        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        var currentSteps = 0

        if(stepSensor != null){
            val sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    currentSteps = event?.values?.get(0)?.toInt() ?: 0
                }

                override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
                    //no
                }
            }
            sensorManager.registerListener(sensorEventListener,stepSensor,SensorManager.SENSOR_DELAY_UI)
            sensorManager.unregisterListener(sensorEventListener)
        }
        return currentSteps
    }


}


