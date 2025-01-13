package dev.cc231046.ccl3stepcounter.ui

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StepTrackingWorker (context: Context, workerParams: WorkerParameters): CoroutineWorker(context,workerParams){
    override suspend fun doWork(): Result{
        val stepsDao = StepsDatabase.getDatabase(applicationContext).stepsDao()

        val today = LocalDate.now().toString()

        val storedSteps = withContext(Dispatchers.IO){
            stepsDao.getStepsForDate(today)
        }
        if(storedSteps == null){
            withContext(Dispatchers.IO){
                stepsDao.insertSteps(
                    StepEntity(
                        date= today,
                        initialStepCount = 0,
                        totalSteps = 0
                    )
                )
            }
        }

        return Result.success()
    }
}