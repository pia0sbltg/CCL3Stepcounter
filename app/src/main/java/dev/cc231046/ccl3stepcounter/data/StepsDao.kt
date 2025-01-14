package dev.cc231046.ccl3stepcounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StepsDao {
    @Query("SELECT * FROM steps WHERE date = :date")
    suspend fun getStepsForDate(date: String): StepEntity?

    @Query("SELECT * FROM steps ORDER BY date DESC LIMIT 6")
    suspend fun getLastSixDays(): List<StepEntity>

    @Query("SELECT * FROM steps")
    suspend fun getAllSteps(): List<StepEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(step: StepEntity)

    @Query("UPDATE steps SET goalReached = :goalReached WHERE date = :date")
    suspend fun updateGoalReached(date: String, goalReached: Boolean)
}