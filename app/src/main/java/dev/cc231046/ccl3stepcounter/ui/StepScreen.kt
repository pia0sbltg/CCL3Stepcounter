package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.cc231046.ccl3stepcounter.data.GoalsDao
import dev.cc231046.ccl3stepcounter.data.OwnedPetsDao
import dev.cc231046.ccl3stepcounter.data.PetDao
import dev.cc231046.ccl3stepcounter.data.StepEntity
import dev.cc231046.ccl3stepcounter.data.StepsDao
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import dev.cc231046.ccl3stepcounter.R
import java.time.DayOfWeek

enum class Routes(val route: String) {
    Main("Steps"),
    Goals("Goals"),
    Edit("Edit_Goal"),
    Shop("Shop")

}

@Composable
fun AppNavigation(stepsDao: StepsDao,goalsDao: GoalsDao, petDao: PetDao, ownedPetsDao: OwnedPetsDao, navController: NavHostController, modifier: Modifier){
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
        composable(Routes.Shop.name){
            ShopScreen(viewModel = ShopViewModel(petDao, ownedPetsDao), navController)
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
    var canFeed = todayGoalReached && petState?.lastFedDate != LocalDate.now().toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row with Shop button and coins
        // Top row with Shop button and coins
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { navController.navigate("Shop") },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text("Shop")
            }

            // Coins display with an image and text
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replace with your actual coin.png resource
                Image(
                    painter = rememberAsyncImagePainter(R.drawable.coin), // Ensure coin.png is added as a drawable resource
                    contentDescription = "Coins",
                    modifier = Modifier
                        .size(30.dp) // Adjust size as needed
                        .padding(end = 8.dp) // Add some spacing between the image and text
                )

                Text(
                    text = "${petState?.coins ?: 0}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }


        // Circular progress with pet (reduced weight to leave space for steps text)
        CircularProgressWithPet(
            steps = steps,
            goalSteps = todayGoal,
            animalType = petState?.selectedAnimal ?: "dog",
            petState = petState?.currentStage,
            modifier = Modifier
                .weight(1.75f)
                .padding(vertical = 8.dp)
        )

        // Spacer to ensure separation between components
        Spacer(modifier = Modifier.height(16.dp))

        // Goals button and feed text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate(Routes.Goals.name) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 8.dp)
            ) {
                Text("Goals")
            }

            if (canFeed) {
                Button(
                    onClick = {
                        viewModel.viewModelScope.launch { viewModel.feedPet() }
                        canFeed = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Feed Pet")
                }
            } else if (!todayGoalReached) {
                Text("Reach your goal to feed the pet!", style = MaterialTheme.typography.bodyMedium)
            } else {
                Text("Pet already fed today!", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Step history list
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(48.dp)
    ) {
        // Circle with checkmark
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .aspectRatio(1f)
                .background(
                    color = if (stepEntity.goalReached)
                        MaterialTheme.colorScheme.primary // Light green for completed
                    else
                        MaterialTheme.colorScheme.secondary, // Light blue-grey for incomplete
                    shape = CircleShape
                )
        ) {
            if (stepEntity.goalReached) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Goal reached",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Day text
        Text(
            text = when (LocalDate.parse(stepEntity.date).dayOfWeek) {
                DayOfWeek.MONDAY -> "Mo"
                DayOfWeek.TUESDAY -> "Tue"
                DayOfWeek.WEDNESDAY -> "Wed"
                DayOfWeek.THURSDAY -> "Thu"
                DayOfWeek.FRIDAY -> "Fri"
                DayOfWeek.SATURDAY -> "Sat"
                DayOfWeek.SUNDAY -> "Sun"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StepHistory(stepEntities: List<StepEntity>) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        stepEntities.takeLast(7).forEach { stepEntity ->
            StepHistoryItem(stepEntity)
        }
    }
}