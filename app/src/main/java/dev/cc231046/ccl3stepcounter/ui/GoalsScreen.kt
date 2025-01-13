package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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

@Composable
fun GoalsScreen(viewModel: GoalsViewModel) {
    val goals = viewModel.goals.collectAsState().value
    LazyColumn {
        items(goals) { goal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Goal: ${goal.goal}")
                IconButton(onClick = { /* Edit goal */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
        item {
            Button(onClick = { /* Navigate to add goal */ }) {
                Text("Add Goal")
            }
        }
    }
}
