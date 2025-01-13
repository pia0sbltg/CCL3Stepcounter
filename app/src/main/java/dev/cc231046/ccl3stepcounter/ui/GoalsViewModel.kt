package dev.cc231046.ccl3stepcounter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.cc231046.ccl3stepcounter.data.GoalEntity
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(private val goalsDao: GoalsDao) : ViewModel() {
    private val _goals = MutableStateFlow(emptyList<GoalEntity>())
    val goals: StateFlow<List<GoalEntity>> = _goals

    init {
        viewModelScope.launch {
            _goals.value = goalsDao.getAllGoals()
        }
    }

    fun addOrUpdateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalsDao.insertGoal(goal)
            _goals.value = goalsDao.getAllGoals()
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalsDao.deleteGoal(goal)
            _goals.value = goalsDao.getAllGoals()
        }
    }
}