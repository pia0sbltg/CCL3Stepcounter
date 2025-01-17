package dev.cc231046.ccl3stepcounter.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cc231046.ccl3stepcounter.data.PetDao
import dev.cc231046.ccl3stepcounter.data.PetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShopViewModel (private val petDao: PetDao): ViewModel() {
    private val _petState = MutableLiveData<PetEntity?>()
    val petState: LiveData<PetEntity?> = _petState

    init {
        viewModelScope.launch {
            loadPetState()
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
        val pet = petDao.getPet() ?: PetEntity()
        if (pet.coins > 0) {
            petDao.insertOrUpdatePet(pet.copy(coins = pet.coins - 1, selectedAnimal = animal))
            loadPetState()
        }
    }
}