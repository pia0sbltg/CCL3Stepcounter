package dev.cc231046.ccl3stepcounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet")
data class PetEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val selectedAnimal: String = "dog",
    val currentStage: Int = 1,
    val feeds: Int = 0,
    val lastFedDate: String = "" // Last feed date
)
