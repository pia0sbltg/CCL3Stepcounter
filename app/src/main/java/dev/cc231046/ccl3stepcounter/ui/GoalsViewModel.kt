package dev.cc231046.ccl3stepcounter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cc231046.ccl3stepcounter.data.GoalEntity
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class GoalsViewModel(private val goalsDao: GoalsDao) : ViewModel() {
    private val _goals = MutableStateFlow<List<GoalEntity>>(emptyList())
    val goals: StateFlow<List<GoalEntity>> = _goals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    _goals.value = goalsDao.getAllGoals()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load goals: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addOrUpdateGoal(stepGoal: Int, dayOfWeek: Int, applyToAllDays: Boolean, onConflict: (String) -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if(applyToAllDays){
                        for (day in 1..7) {
                            val goal = GoalEntity(dayOfWeek = day, stepGoal = stepGoal)
                            goalsDao.insertGoal(goal)
                        }
                    }else {
                        val existingGoal = goalsDao.getGoalForDay(dayOfWeek)
                        if (existingGoal != null) {
                            // Notify UI of the conflict
                            withContext(Dispatchers.Main) {
                                onConflict("A goal already exists for this day.")
                            }
                            return@withContext
                        } else {
                            val goal = GoalEntity(dayOfWeek = dayOfWeek, stepGoal = stepGoal)
                            goalsDao.insertGoal(goal)
                        }
                    }
                    val allGoals = goalsDao.getAllGoals()
                    withContext(Dispatchers.Main) {
                        _goals.value = allGoals
                        onSuccess()
                    }

                }
            } catch (e: Exception) {
                // Notify UI of any general error
                onConflict("Failed to save goal: ${e.message}")
            }
        }
    }


    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    goalsDao.deleteGoal(goal)
                    _goals.value = goalsDao.getAllGoals()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete goal: ${e.message}"
            }
        }
    }
}