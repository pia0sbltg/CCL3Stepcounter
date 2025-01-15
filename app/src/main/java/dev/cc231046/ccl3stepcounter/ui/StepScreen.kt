package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import dev.cc231046.ccl3stepcounter.data.PetDao
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDao
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class Routes(val route: String) {
    Main("Steps"),
    Goals("Goals"),
    Edit("Edit_Goal")

}

@Composable
fun AppNavigation(stepsDao: StepsDao,goalsDao: GoalsDao, petDao: PetDao, navController: NavHostController, modifier: Modifier){
    val context = LocalContext.current

    NavHost(navController =navController, startDestination = Routes.Main.name) {
        composable(Routes.Main.name){
            StepScreen(StepsViewModel(stepsDao = stepsDao, goalsDao = goalsDao , petDao = petDao,applicationContext = context ), navController)
        }
        composable(Routes.Goals.name){
            GoalsScreen(GoalsViewModel(goalsDao),navController= navController, onAddGoalClick = {navController.navigate(Routes.Edit.name)})
        }
        composable(Routes.Edit.name){
            EditGoalScreen(viewModel = GoalsViewModel(goalsDao), onGoalSaved = {navController.popBackStack()})
        }
    }
}

@Composable
fun StepScreen(viewModel: StepsViewModel, navController: NavHostController) {
    val steps by viewModel.currentSteps.observeAsState(0)
    val stepsHistory by viewModel.stepHistory.observeAsState(emptyList())
    val todayGoal by viewModel.todayGoal.observeAsState(0)
    val petState by viewModel.petState.observeAsState()

    val todayGoalReached = stepsHistory.any { it.date == LocalDate.now().toString() && it.goalReached }
    var canFeed by remember{ mutableStateOf( petState?.lastFedDate != LocalDate.now().toString() && todayGoalReached)}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        CircularProgressWithDog(
            steps = steps,
            goalSteps = todayGoal,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigate(Routes.Goals.name) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Goals")
            }

            if (canFeed) {
                Button(
                    onClick = {
                        viewModel.viewModelScope.launch { viewModel.feedPet() }
                        canFeed=false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Feed Pet")
                }
            } else if (!todayGoalReached) {
                Text("Reach your goal to feed the pet!")
            } else {
                Text("Pet already fed today!")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            items(stepsHistory) { stepEntity ->
                StepHistoryItem(stepEntity)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun StepHistoryItem(stepEntity: StepEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stepEntity.date,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${stepEntity.totalSteps} steps",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = if (stepEntity.goalReached) "✓" else "✗",
            style = MaterialTheme.typography.bodyMedium,
            color = if (stepEntity.goalReached)
                Color(0xFF4CAF50) else Color(0xFFE57373)
        )
    }
}

