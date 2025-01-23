package dev.cc231046.ccl3stepcounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val dayOfWeek: Int, // Ensure this is marked as the primary key
    val stepGoal: Int
)
