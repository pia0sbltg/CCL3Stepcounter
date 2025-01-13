package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import dev.cc231046.ccl3stepcounter.data.StepsDao

enum class Routes(val route: String) {
    Main("Steps"),
    Goals("Goals")

}

@Composable
fun AppNavigation(stepsDao: StepsDao,goalsDao: GoalsDao, navController: NavHostController, modifier: Modifier){
    val context = LocalContext.current
    NavHost(navController =navController, startDestination = Routes.Main.name) {
        composable(Routes.Main.name){
            StepScreen(StepsViewModel(stepsDao = stepsDao, context = context ))
        }
        composable(Routes.Goals.name){
            GoalsScreen(GoalsViewModel(goalsDao))
        }
    }
}

@Composable
fun StepScreen ( viewModel: StepsViewModel) {
    val steps by viewModel.currentSteps.observeAsState(0)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Steps: $steps", style = MaterialTheme.typography.headlineMedium)

    }
}