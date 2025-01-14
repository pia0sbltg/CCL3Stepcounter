package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import dev.cc231046.ccl3stepcounter.data.GoalEntity

@Composable
fun GoalsScreen(viewModel: GoalsViewModel,navController: NavHostController, onAddGoalClick: () -> Unit) {
    val goals = viewModel.goals.collectAsState().value
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = {navController.popBackStack()}) {
            Text("<-")
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(goals) { goal ->
                GoalItem(goal = goal, onDeleteClick = { viewModel.deleteGoal(goal) })
            }
        }
        Button(
            onClick = onAddGoalClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Add Goal")
        }
    }
}

@Composable
fun GoalItem(goal: GoalEntity, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Day: ${goal.dayOfWeek}, Steps: ${goal.stepGoal}")
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Goal")
        }
    }
}
