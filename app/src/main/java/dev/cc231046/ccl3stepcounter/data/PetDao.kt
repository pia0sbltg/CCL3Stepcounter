package dev.cc231046.ccl3stepcounter.data

import androidx.room.*

@Dao
interface PetDao {
    @Query("SELECT * FROM pet WHERE id = 1")
    suspend fun getPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePet(pet: PetEntity)

    @Query("UPDATE pet SET feeds = :feeds, currentStage = :currentStage, lastFedDate = :lastFedDate WHERE id = 1")
    suspend fun updatePet(feeds: Int, currentStage: Int, lastFedDate: String)

    @Query("DELETE FROM pet")
    suspend fun resetPet()
}