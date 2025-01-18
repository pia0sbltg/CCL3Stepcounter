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
    animalType: String,
    petState: Int?,
    modifier: Modifier = Modifier
) {
    var progressValue by remember { mutableFloatStateOf(0f) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val size = min(screenWidth.value * 0.7f, 250f).dp
    val petSize = size.value * 0.7f

    val context = LocalContext.current

    val progressColor = MaterialTheme.colorScheme.secondary

    val currentAnim by remember(animalType, petState) {
        derivedStateOf {
            when (animalType) {
                "dog" -> when (petState) {
                    2 -> R.drawable.dog_sleeping
                    3 -> R.drawable.dog_workout
                    else -> R.drawable.dog_animation
                }
                "cat" -> when (petState) {
                    2 -> R.drawable.cat_sleeping
                    3 -> R.drawable.cat_workout
                    else -> R.drawable.cat_animation
                }"rabbit" -> when (petState) {
                    2 -> R.drawable.rabbit_sleeping
                    3 -> R.drawable.rabbit_workout
                    else -> R.drawable.rabbit_animation
                }
                else -> R.drawable.dog_animation
            }
        }
    }

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
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .aspectRatio(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .size(size)
                    .aspectRatio(1f)
            ) {
                val strokeWidth = 16.dp.toPx()
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }

            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(currentAnim)
                        .size(Size.ORIGINAL)
                        .build(),
                    imageLoader = imageLoader
                ),
                contentDescription = "Pet Animation",
                modifier = Modifier.size(petSize.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Updated steps display
        Text(
            text = "$steps",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Text(
            text = "/ $goalSteps steps",
            style = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
    }
}