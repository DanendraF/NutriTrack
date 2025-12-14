package com.example.nutritrack.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray
import kotlin.math.roundToInt
import com.example.nutritrack.presentation.onboarding.viewmodel.OnboardingViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    viewModel: OnboardingViewModel,
    onNavigateNext: () -> Unit,
    onNavigateBack: () -> Unit,
    step: Int,
    totalSteps: Int
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentHeight by remember { mutableStateOf(uiState.height.toFloatOrNull() ?: 170f) }
    var currentWeight by remember { mutableStateOf(uiState.weight.toFloatOrNull() ?: 70f) }

    LaunchedEffect(currentHeight, currentWeight) {
        viewModel.updateHeight(currentHeight.roundToInt().toString())
        viewModel.updateWeight(currentWeight.roundToInt().toString())
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextGray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OnboardingProgressBar(currentStep = step, totalSteps = totalSteps)
            }
        },
        bottomBar = { NavigationButtons(onBack = onNavigateBack, onNext = onNavigateNext) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            var tabIndex by remember { mutableStateOf(0) }
            val tabs = listOf("Height", "Weight")

            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.White,
                contentColor = DarkGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            AnimatedContent(
                targetState = tabIndex,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "MeasurementTabAnimation"
            ) { targetIndex ->
                if (targetIndex == 0) {
                    RulerContent(
                        title = "What's Your Height?",
                        subtitle = "Height in cm. Don't worry, you can always change it later.",
                        value = currentHeight,
                        onValueChange = { currentHeight = it },
                        unit = "Centimeters",
                        range = 100f..220f
                    )
                } else {
                    RulerContent(
                        title = "What's Your Weight?",
                        subtitle = "Weight in kg. Don't worry, you can always change it later.",
                        value = currentWeight,
                        onValueChange = { currentWeight = it },
                        unit = "Kilograms",
                        range = 30f..150f
                    )
                }
            }
        }
    }
}

@Composable
fun RulerContent(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    unit: String,
    range: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, fontSize = 14.sp, color = TextGray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalRuler(
                    value = value,
                    onValueChange = onValueChange,
                    range = range,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* No action needed */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text(text = unit, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun HorizontalRuler(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    val lineSpacing = 12.dp

    BoxWithConstraints(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                val newValue = value - (dragAmount.x / lineSpacing.toPx())
                onValueChange(newValue.coerceIn(range))
            }
        },
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val centerValue = value.roundToInt()

        Text(centerValue.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenterY = size.height / 2
            val tickCount = (width / lineSpacing.toPx()).roundToInt()
            val startValue = centerValue - tickCount / 2

            for (i in 0..tickCount) {
                val currentValueTick = startValue + i
                val xPosition = (width / 2) + ((currentValueTick - value) * lineSpacing.toPx())
                val isMajorTick = currentValueTick % 5 == 0

                drawLine(
                    color = TextGray.copy(alpha = 0.5f),
                    start = Offset(xPosition, canvasCenterY - (if (isMajorTick) 20.dp else 12.dp).toPx() / 2),
                    end = Offset(xPosition, canvasCenterY + (if (isMajorTick) 20.dp else 12.dp).toPx() / 2),
                    strokeWidth = (if (isMajorTick) 2.dp else 1.dp).toPx()
                )

                if (isMajorTick && currentValueTick != centerValue) {
                    drawContext.canvas.nativeCanvas.drawText(
                        currentValueTick.toString(),
                        xPosition,
                        canvasCenterY - 30.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = Color.Gray.toArgb()
                            textSize = 16.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            }

            val indicatorPath = Path().apply {
                moveTo(width / 2 - 5.dp.toPx(), canvasCenterY + 20.dp.toPx())
                lineTo(width / 2 + 5.dp.toPx(), canvasCenterY + 20.dp.toPx())
                lineTo(width / 2, canvasCenterY + 25.dp.toPx())
                close()
            }
            drawPath(path = indicatorPath, color = DarkGreen)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MeasurementScreenPreview() {
    NutriTrackTheme {
        MeasurementScreen(viewModel(), {}, {}, 2, 5)
    }
}
