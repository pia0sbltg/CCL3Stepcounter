package dev.cc231046.ccl3stepcounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_pets")
data class OwnedPetsEntity(
    @PrimaryKey val animalName: String,
    val isSelected: Boolean = false
)