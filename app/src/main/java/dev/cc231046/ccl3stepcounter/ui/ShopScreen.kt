package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ShopScreen(viewModel: ShopViewModel, navController: NavHostController) {
    val petState by viewModel.petState.observeAsState()
    val animals = listOf("dog", "cat", "rabbit")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
            Text("Coins: ${petState?.coins ?: 0}")
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(animals.chunked(3)) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    row.forEach { animal ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(animal)
                            Button(onClick = {
                                viewModel.viewModelScope.launch {
                                    viewModel.purchaseAnimal(animal)
                                }
                            }) {
                                Text("Buy")
                            }
                        }
                    }
                }
            }
        }
    }
}