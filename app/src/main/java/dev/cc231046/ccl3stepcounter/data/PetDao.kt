package dev.cc231046.ccl3stepcounter.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PetDao {
    @Query("SELECT * FROM pet WHERE id = 1")
    suspend fun getPet(): PetEntity?

    @Query("SELECT * FROM pet WHERE id = 1")
    fun getPetLiveData(): LiveData<PetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePet(pet: PetEntity)

    @Query("UPDATE pet SET coins = coins + :coins WHERE id = 1")
    suspend fun addCoins(coins: Int)

    @Query("UPDATE pet SET selectedAnimal = :selectedAnimal WHERE id = 1")
    suspend fun updateSelectedAnimal(selectedAnimal: String)

    @Query("UPDATE pet SET coins= :coins, feeds = :feeds, currentStage = :currentStage, lastFedDate = :lastFedDate, stepsForRevival = :stepsForRevival WHERE id = 1")
    suspend fun updatePet(coins: Int, feeds: Int, currentStage: Int, lastFedDate: String, stepsForRevival:Int)

    @Query("DELETE FROM pet")
    suspend fun resetPet()

    @Query("UPDATE pet SET deaths = deaths + 1 WHERE id = 1")
    suspend fun addDeath()

    @Query("UPDATE pet SET feeds = 9 WHERE id = 1")
    suspend fun updatePetFeeds9()
}