package dev.cc231046.ccl3stepcounter.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface OwnedPetsDao {

    @Query("SELECT * FROM owned_pets")
    fun getOwnedPets(): LiveData<List<OwnedPetsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnedPet(pet: OwnedPetsEntity)

    @Query("UPDATE owned_pets SET isSelected = :isSelected WHERE animalName = :animal")
    suspend fun updatePetSelection(animal: String, isSelected: Boolean)

    @Query("UPDATE owned_pets SET isSelected = :isSelected")
    suspend fun updateAllPetsSelection(isSelected: Boolean)

    @Query("SELECT EXISTS (SELECT 1 FROM owned_pets WHERE animalName = :animal)")
    suspend fun isPetOwned(animal: String):Boolean
}