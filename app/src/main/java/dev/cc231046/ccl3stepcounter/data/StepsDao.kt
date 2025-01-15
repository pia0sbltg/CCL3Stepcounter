package dev.cc231046.ccl3stepcounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface StepsDao {
    @Query("SELECT * FROM steps WHERE date = :date")
    suspend fun getStepsForDate(date: String): StepEntity?

    @Query("SELECT * FROM steps ORDER BY date DESC LIMIT 6")
    suspend fun getLastSixDays(): List<StepEntity>

    @Query("SELECT * FROM steps ORDER BY date DESC LIMIT 1")
    suspend fun getLastStepEntry(): StepEntity?

    @Query("SELECT * FROM steps")
    suspend fun getAllSteps(): List<StepEntity>?

    @Query("UPDATE steps SET totalSteps = :totalSteps WHERE date = :date")
    suspend fun updateTotalSteps(date: String, totalSteps: Int)

    @Transaction
    suspend fun insertOrUpdateSteps(steps: StepEntity) {
        val existingEntry = getStepsForDate(steps.date)
        if (existingEntry == null) {
            insertSteps(steps)
        } else {
            updateTotalSteps(steps.date, steps.totalSteps)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(step: StepEntity)

    @Query("DELETE FROM steps")
    suspend fun deleteEverything()

    @Query("DElETE FROM steps WHERE date = :date")
    suspend fun deleteToday(date:String){
        println("Deleted TOday")
    }

    @Query("UPDATE steps SET goalReached = :goalReached WHERE date = :date")
    suspend fun updateGoalReached(date: String, goalReached: Boolean)
}