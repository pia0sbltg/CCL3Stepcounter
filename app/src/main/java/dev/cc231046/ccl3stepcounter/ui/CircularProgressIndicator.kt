package dev.cc231046.ccl3stepcounter.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import dev.cc231046.ccl3stepcounter.R
import kotlin.math.min

@Composable
fun CircularProgressWithPet(
    steps: Int,
    goalSteps: Int,
    animalType: String, // The selected animal type (e.g., "dog", "cat", etc.)
    petState: Int?,
    modifier: Modifier = Modifier

) {
    var progressValue by remember { mutableFloatStateOf(0f) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val size = min(screenWidth.value * 0.7f, 250f).dp
    val petSize = size.value * 0.8f

    // Context for ImageLoader
    val context = LocalContext.current

    val currentAnim by remember(animalType, petState) {
        derivedStateOf {
            when (animalType) {
                "dog" -> when (petState) {
                    2 -> R.drawable.dog_sleeping
                    3 -> R.drawable.dog_workout
                    else -> R.drawable.dog_animation
                }
                "cat" -> when (petState) {
                    //2 -> R.drawable.cat_sleeping
                  //  3 -> R.drawable.cat_workout
                  else -> R.drawable.dog_animation
                }
                /*
                "rabbit" -> when (petState) {
                    2 -> R.drawable.rabbit_sleeping
                    3 -> R.drawable.rabbit_workout
                    else -> R.drawable.rabbit_animation
                }

                 */
                else -> R.drawable.dog_animation // Default to the dog animation
            }
        }
    }


    println("ANIMAL TYPE: $animalType, STATE: $petState")
    // Create ImageLoader that can handle GIFs
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(ImageDecoderDecoder.Factory())
                add(GifDecoder.Factory())
            }
            .build()
    }


    LaunchedEffect(steps, goalSteps) {
        progressValue = if (goalSteps > 0) {
            (steps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 1000),
        label = "Progress Animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .aspectRatio(1f)
        ) {
            // Progress Circle
            Canvas(
                modifier = Modifier
                    .size(size)
                    .aspectRatio(1f)
            ) {
                val strokeWidth = 16.dp.toPx()
                val diameter = min(size.toPx(), size.toPx()) - strokeWidth
                val radius = diameter / 2



                // Progress arc
                drawArc(
                    color = Color(0xFF90EE90),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Animated Dog GIF
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(currentAnim)
                        .size(Size.ORIGINAL) // Maintain original GIF size
                        .build(),
                    imageLoader = imageLoader
                ),
                contentDescription = "Dog Animation",
                modifier = Modifier.size(petSize.dp)
            )
        }

        // Steps display below circle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "$steps",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "steps",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}