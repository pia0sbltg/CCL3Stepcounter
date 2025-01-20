package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import dev.cc231046.ccl3stepcounter.R
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import androidx.lifecycle.viewModelScope
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.min

@Composable
fun ShopScreen(viewModel: ShopViewModel, navController: NavHostController) {
    val petState by viewModel.petState.observeAsState()
    val ownedPets by viewModel.ownedAnimals.observeAsState(emptyList())
    val ownedPetsNames = ownedPets.map {it.animalName}
    val selectedPet = ownedPets.find{ it.isSelected }?.animalName

    val animals = listOf("dog", "cat", "rabbit")
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val size = min(screenWidth.value * 0.7f, 250f).dp
    val petSize = size.value * 0.3f

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(ImageDecoderDecoder.Factory())
                add(GifDecoder.Factory())
            }
            .build()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
            Box(modifier = Modifier) {
                Row {
                    Image(
                        painter = rememberAsyncImagePainter(R.drawable.coin), // Ensure coin.png is added as a drawable resource
                        contentDescription = "Coins",
                        modifier = Modifier
                            .size(30.dp) // Adjust size as needed
                            .padding(end = 8.dp) // Add some spacing between the image and text
                    )

                    Text(
                        text = "${petState?.coins ?: 0}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }

            }

        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(animals.chunked(3)) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    row.forEach { animal ->
                        val isOwned = animal in ownedPetsNames
                        val isSelected = animal == selectedPet

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = animal.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                    else it.toString()
                                }
                            )
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(viewModel.getAnimalAnimationResource(animal))
                                        .size(Size.ORIGINAL)
                                        .build(),
                                    imageLoader = imageLoader
                                ),
                                contentDescription = "$animal Animation",
                                modifier = Modifier.size(petSize.dp)
                            )

                            // Show cost if not owned
                            if (!isOwned) {
                                Row {
                                    Text(
                                    text = "50",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                    Image(
                                        painter = rememberAsyncImagePainter(R.drawable.coin), // Ensure coin.png is added as a drawable resource
                                        contentDescription = "Coins",
                                        modifier = Modifier
                                            .size(24.dp) // Adjust size as needed
                                            .padding(end = 8.dp) // Add some spacing between the image and text
                                    )
                                }

                            }

                            Button(
                                onClick = {
                                    if(isOwned){
                                        viewModel.viewModelScope.launch {
                                            viewModel.selectPet(animal)
                                        }
                                    }else{
                                        viewModel.viewModelScope.launch {
                                            viewModel.purchaseAnimal(animal)
                                        }
                                    }
                                },
                                enabled = !isSelected
                            ) {
                                Text(
                                    when {
                                        isSelected -> "Selected"
                                        isOwned -> "Select"
                                        else -> "Buy"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}