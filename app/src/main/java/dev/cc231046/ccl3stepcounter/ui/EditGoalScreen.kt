package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cc231046.ccl3stepcounter.data.GoalEntity

@Composable
fun EditGoalScreen(viewModel: GoalsViewModel, onGoalSaved:() -> Unit) {
    var dayOfWeek by remember { mutableIntStateOf(1) }
    var stepGoal by remember { mutableStateOf("") }

    Column (modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center){

        Text("Set Goal", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Day:")
        DropdownMenu(
            expanded = false, // Implement dropdown logic here
            onDismissRequest = {  }
        ) {
            (1..7).forEach { day ->
                DropdownMenuItem({Text("Day $day")},onClick = { dayOfWeek = day })
            }
        }

            OutlinedTextField(
                value = stepGoal,
                onValueChange = { stepGoal = it },
                label = { Text("Step Goal") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                viewModel.addOrUpdateGoal(
                    GoalEntity(
                        dayOfWeek = dayOfWeek,
                        stepGoal = stepGoal.toInt()
                    )
                )
                onGoalSaved()

            },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Save Goal")
            }

    }
}