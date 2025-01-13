package dev.cc231046.ccl3stepcounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stepCount: Int,
    val date: String // Format: YYYY-MM-DD
)