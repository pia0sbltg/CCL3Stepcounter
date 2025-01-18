package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cc231046.ccl3stepcounter.data.GoalEntity

@Composable
fun EditGoalScreen(viewModel: GoalsViewModel, onGoalSaved: () -> Unit) {
    val dayOfWeek = remember { mutableIntStateOf(DateUtils.getCurrentDayOfWeek()) }
    val stepGoal = remember { mutableStateOf("") }
    val isDropdownExpanded = remember { mutableStateOf(false) }
    val snackbarMessage = remember { mutableStateOf("") }
    val applyToAllDays = remember { mutableStateOf(false) }

    val dayOptions = DateUtils.getDropdownDayOptions()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Set Goal",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        if(!applyToAllDays.value) {

            Button(
                onClick = { isDropdownExpanded.value = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(DateUtils.getRelativeDayName(dayOfWeek.intValue))
            }

            DropdownMenu(
                expanded = isDropdownExpanded.value,
                onDismissRequest = { isDropdownExpanded.value = false }
            ) {
                dayOptions.forEach { (day, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                color = if (day == dayOfWeek.intValue)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            dayOfWeek.intValue = day
                            isDropdownExpanded.value = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = stepGoal.value,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    stepGoal.value = it
                }
            },
            label = { Text("Step Goal") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Checkbox(
                checked = applyToAllDays.value,
                onCheckedChange = { applyToAllDays.value = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply to all days")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (stepGoal.value.isNotEmpty()) {
                    viewModel.addOrUpdateGoal(
                        stepGoal =stepGoal.value.toInt(),
                        dayOfWeek = dayOfWeek.intValue,
                        applyToAllDays.value,
                        onConflict = { message -> snackbarMessage.value = message },
                        onSuccess = {
                            snackbarMessage.value = "Goal saved successfully!"
                            onGoalSaved()
                        }
                    )
                } else {
                    snackbarMessage.value = "Please enter a step goal."
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Save Goal")
        }

        if (snackbarMessage.value.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    Button(onClick = { snackbarMessage.value = "" }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(snackbarMessage.value)
            }
        }
    }
}
