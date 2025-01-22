package dev.cc231046.ccl3stepcounter.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cc231046.ccl3stepcounter.R
import dev.cc231046.ccl3stepcounter.data.OwnedPetsDao
import dev.cc231046.ccl3stepcounter.data.OwnedPetsEntity
import dev.cc231046.ccl3stepcounter.data.PetDao
import dev.cc231046.ccl3stepcounter.data.PetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ShopViewModel (private val petDao: PetDao, private val ownedPetsDao: OwnedPetsDao): ViewModel() {
    private val _petState = MutableLiveData<PetEntity?>()
    val petState: LiveData<PetEntity?> = _petState


    val ownedAnimals: LiveData<List<OwnedPetsEntity>> = ownedPetsDao.getOwnedPets()
    init {
        viewModelScope.launch {
            loadPetState()
            addDogAsDefault()
        }
    }

    private suspend fun addDogAsDefault(){
        val default= "dog"
        if(!ownedPetsDao.isPetOwned(default)){
            ownedPetsDao.insertOwnedPet(OwnedPetsEntity(animalName = "dog", isSelected = true))
        }
    }

    private suspend fun loadPetState() {
        withContext(Dispatchers.IO) {
            val pet = petDao.getPet() ?: PetEntity()
            withContext(Dispatchers.Main) {
                _petState.value = pet
            }
        }
    }

    suspend fun purchaseAnimal(animal: String) {
        val pet = petDao.getPet() ?: PetEntity() //gets how many coins the user has

        if (pet.coins >= 10 && !ownedPetsDao.isPetOwned(animal)) {
            ownedPetsDao.insertOwnedPet(OwnedPetsEntity(animalName = animal, isSelected = true))
            petDao.insertOrUpdatePet(pet.copy(coins = pet.coins - 10))
            selectPet(animal)
        }
    }

    suspend fun selectPet(animal: String){
        val pet = petDao.getPet() ?: PetEntity()
        ownedPetsDao.updateAllPetsSelection(false)
        ownedPetsDao.updatePetSelection(animal,true)
        petDao.insertOrUpdatePet(pet.copy(selectedAnimal = animal))
        loadPetState()
    }

    fun getAnimalAnimationResource(animal: String): Int {
        return when (animal.lowercase(Locale.getDefault())) {
            "dog" -> R.drawable.dog_animation
            "cat" -> R.drawable.cat_animation
            "rabbit" -> R.drawable.rabbit_animation
            else -> R.drawable.dog_animation // Default to dog animation
        }
    }

}