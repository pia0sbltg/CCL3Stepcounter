package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDao

enum class Routes(val route: String) {
    Main("Steps"),
    Goals("Goals"),
    Edit("Edit_Goal")

}

@Composable
fun AppNavigation(stepsDao: StepsDao,goalsDao: GoalsDao, navController: NavHostController, modifier: Modifier){
    val context = LocalContext.current

    NavHost(navController =navController, startDestination = Routes.Main.name) {
        composable(Routes.Main.name){
            StepScreen(StepsViewModel(stepsDao = stepsDao, goalsDao = goalsDao ,applicationContext = context ), navController)
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
fun StepScreen ( viewModel: StepsViewModel, navController: NavHostController) {
    val steps by viewModel.currentSteps.observeAsState(0)
    val stepsHistory by viewModel.stepHistory.observeAsState(emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Column (modifier = Modifier.padding(0.dp,50.dp), verticalArrangement = Arrangement.Center){
            Text(text = "Steps: $steps", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = {navController.navigate(Routes.Goals.name)}) {
                Text("Goals")
            }

        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ){
            LazyColumn() {
                items(stepsHistory){stepEntity ->
                    StepHistoryItem(stepEntity)

                }
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stepEntity.date)
        Text(
            text = if (stepEntity.goalReached) "Goal Reached 🎉" else "Goal Not Reached",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}