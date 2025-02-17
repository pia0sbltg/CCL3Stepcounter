package dev.cc231046.ccl3stepcounter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.cc231046.ccl3stepcounter.data.GoalEntity

@Dao
interface GoalsDao {
    @Query("SELECT * FROM goals")
    suspend fun getAllGoals(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE dayOfWeek = :dayOfWeek")
    suspend fun getGoalsForDay(dayOfWeek: Int): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun deleteEveryGoal()

    @Query("SELECT * FROM goals WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getGoalForDay(dayOfWeek: Int): GoalEntity?

}