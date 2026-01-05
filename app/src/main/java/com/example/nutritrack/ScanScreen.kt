package com.example.nutritrack

import android.Manifest
import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.util.Base64
import java.io.ByteArrayOutputStream
import com.example.nutritrack.data.remote.openai.OpenAIService
import kotlinx.coroutines.tasks.await
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.get
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.nutritrack.domain.model.Food
import com.example.nutritrack.ml.NutriMobilenet
import com.example.nutritrack.presentation.food.FoodViewModel
import com.example.nutritrack.ui.theme.DarkGreen
import com.google.accompanist.permissions.*
import org.koin.androidx.compose.koinViewModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ImageProcessor as TFLiteImageProcessor
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/* ===================== STATE ===================== */
private enum class ScanState { SCANNING, LOADING, RESULT }

/* ===================== MAIN SCREEN ===================== */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: FoodViewModel = koinViewModel(),
    mealViewModel: com.example.nutritrack.presentation.meal.MealViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scanState by remember { mutableStateOf(ScanState.SCANNING) }
    var classificationResult by remember { mutableStateOf<NutriClassifier.Result?>(null) }
    var selectedMealType by remember { mutableStateOf<com.example.nutritrack.domain.model.MealType>(com.example.nutritrack.domain.model.MealType.SNACK) }
    val searchResults by viewModel.searchResults.collectAsState()
    var fetchedFoodInfo by remember { mutableStateOf<Food?>(null) }
    var searchRequested by remember { mutableStateOf(false) }

    val foodsState by viewModel.foodsState.collectAsState()
    val allFoods = (foodsState as? com.example.nutritrack.domain.model.UiState.Success<List<Food>>)?.data ?: emptyList()

    val classifier = remember { NutriClassifier(context) }
    val coroutineScope = rememberCoroutineScope()
    val openAIService = remember { OpenAIService() }
    val firestore: FirebaseFirestore = get()
    val firebaseAuth: FirebaseAuth = get()
    var lastCapturedBase64 by remember { mutableStateOf<String?>(null) }

    // Monitor save meal state
    val saveMealState by mealViewModel.saveMealState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // Handle save meal result
    LaunchedEffect(saveMealState) {
        try {
            when (saveMealState) {
                is com.example.nutritrack.domain.model.UiState.Success -> {
                    android.util.Log.d("ScanScreen", "✅ Meal save success callback")
                    Toast.makeText(context, "✅ Meal saved successfully!", Toast.LENGTH_SHORT).show()
                    mealViewModel.resetSaveState()

                    // Reset scan after successful save
                    lastCapturedBase64 = null
                    fetchedFoodInfo = null
                    capturedBitmap = null
                    scanState = ScanState.SCANNING
                }
                is com.example.nutritrack.domain.model.UiState.Error -> {
                    val errorMsg = (saveMealState as com.example.nutritrack.domain.model.UiState.Error).message
                    android.util.Log.e("ScanScreen", "❌ Meal save error callback: $errorMsg")
                    Toast.makeText(context, "❌ Failed to save meal: $errorMsg", Toast.LENGTH_LONG).show()
                    mealViewModel.resetSaveState()
                }
                else -> {
                    // Idle or Loading state, do nothing
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ScanScreen", "❌ Exception in saveMealState LaunchedEffect", e)
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (cameraPermission.status.isGranted) {
            AIScannerUI(
                onImageCaptured = { imageProxy ->
                    scanState = ScanState.LOADING

                    val bitmap = imageProxy.toBitmap()
                    imageProxy.close()

                    if (bitmap != null) {
                        val result = classifier.classify(bitmap)
                        classificationResult = result
                        // Call OpenAI to analyze nutrition and wait before showing result
                        coroutineScope.launch {
                            try {
                                // convert bitmap to base64
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                                val bytes = baos.toByteArray()
                                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                lastCapturedBase64 = base64

                                val aiResult = openAIService.analyzeFoodImage(base64)
                                if (aiResult.isSuccess) {
                                    val resp = aiResult.getOrNull()
                                    if (resp != null) {
                                        fetchedFoodInfo = Food(
                                            foodId = "openai_${resp.name}_${System.currentTimeMillis()}",
                                            name = resp.name,
                                            nameIndonesian = resp.nameIndonesian,
                                            category = resp.category,
                                            nutrition = com.example.nutritrack.domain.model.NutritionInfo(
                                                calories = resp.calories,
                                                protein = resp.protein,
                                                carbs = resp.carbs,
                                                fat = resp.fat,
                                                fiber = resp.fiber,
                                                sugar = resp.sugar,
                                                sodium = resp.sodium
                                            ),
                                            servingSize = com.example.nutritrack.domain.model.ServingSize(
                                                amount = 100f,
                                                unit = "g"
                                            ),
                                            barcode = null,
                                            imageUrl = null,
                                            isVerified = false,
                                            source = "openai"
                                        )
                                    }
                                } else {
                                    android.util.Log.e("ScanScreen", "OpenAI analysis failed: ${aiResult.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ScanScreen", "Error calling OpenAI", e)
                            } finally {
                                capturedBitmap = bitmap
                                scanState = ScanState.RESULT
                            }
                        }
                    } else {
                        scanState = ScanState.SCANNING
                    }
                }
            )
        } else {
            NoPermissionScreen { launcher.launch(Manifest.permission.CAMERA) }
        }

        if (scanState == ScanState.LOADING) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    if (scanState == ScanState.RESULT && capturedBitmap != null) {
        val confidence = classificationResult?.confidence ?: 0
        val label = classificationResult?.label ?: "Unknown"

        // Find local food first if confidence > 45%
        val localFood = if (confidence > 45) {
            allFoods.find { it.name.equals(label, ignoreCase = true) || it.nameIndonesian?.equals(label, ignoreCase = true) == true }
        } else null

        // If not found locally, trigger backend search once and keep fetched result
        LaunchedEffect(key1 = Pair(label, confidence)) {
            if (confidence > 45 && localFood == null && !searchRequested) {
                viewModel.searchFoods(label)
                searchRequested = true
            }
        }

        // When backend returns results, pick first as fetchedFoodInfo
        LaunchedEffect(key1 = searchResults) {
            if (searchRequested && searchResults.isNotEmpty()) {
                fetchedFoodInfo = searchResults.first()
            }
        }

        val foodInfo = localFood ?: fetchedFoodInfo

        ScanResultSheet(
            bitmap = capturedBitmap!!,
            label = label,
            confidence = confidence,
            foodInfo = foodInfo,
            selectedMealType = selectedMealType,
            onMealTypeChange = { selectedMealType = it },
            onSave = {
                // Save scan log to Firestore
                coroutineScope.launch {
                    try {
                        val userId = firebaseAuth.currentUser?.uid ?: "anonymous"

                        // Structure: log_scan (collection) -> {userId} (document) -> scans (subcollection) -> {randomId} (document)
                        val userDocRef = firestore.collection("log_scan").document(userId)
                        val scansCol = userDocRef.collection("scans")
                        val newDocRef = scansCol.document()
                        val scanId = newDocRef.id

                        val data = mapOf<String, Any?>(
                            "scanId" to scanId,
                            "userId" to userId,
                            "timestamp" to System.currentTimeMillis(),
                            "detectedLabel" to label,
                            "confidence" to confidence,
                            "source" to (foodInfo?.source ?: "openai"),
                            "foodName" to (foodInfo?.name ?: label),
                            "foodNameIndonesian" to (foodInfo?.nameIndonesian ?: null),
                            "category" to (foodInfo?.category ?: null),
                            "nutrition" to if (foodInfo != null) mapOf(
                                "calories" to foodInfo.nutrition.calories,
                                "protein" to foodInfo.nutrition.protein,
                                "carbs" to foodInfo.nutrition.carbs,
                                "fat" to foodInfo.nutrition.fat,
                                "fiber" to foodInfo.nutrition.fiber,
                                "sugar" to foodInfo.nutrition.sugar,
                                "sodium" to foodInfo.nutrition.sodium
                            ) else null,
                            "image_base64" to lastCapturedBase64
                        )

                        try {
                            newDocRef.set(data).await()
                            android.util.Log.d("ScanScreen", "✅ Scan log saved: ${newDocRef.id} under log_scan/$userId/scans")
                        } catch (e: Exception) {
                            android.util.Log.e("ScanScreen", "❌ Failed to save scan log", e)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ScanScreen", "❌ Exception saving scan log", e)
                    }
                }

                // Save meal to backend (will trigger LaunchedEffect for UI feedback)
                try {
                    if (foodInfo != null) {
                        android.util.Log.d("ScanScreen", "🍽️ Saving meal to backend: ${foodInfo.name}")
                        mealViewModel.saveMealFromScan(foodInfo, selectedMealType)
                    } else {
                        android.util.Log.e("ScanScreen", "❌ No food info to save")
                        Toast.makeText(context, "No food information to save", Toast.LENGTH_SHORT).show()

                        // Reset manually if no food to save
                        lastCapturedBase64 = null
                        fetchedFoodInfo = null
                        capturedBitmap = null
                        scanState = ScanState.SCANNING
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ScanScreen", "❌ Exception calling saveMealFromScan", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            onDone = {
                capturedBitmap = null
                scanState = ScanState.SCANNING
            }
        )
    }
}

/* ===================== CAMERA UI ===================== */
@Composable
fun AIScannerUI(onImageCaptured: (ImageProxy) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val provider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("Camera", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        IconButton(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .size(80.dp),
            onClick = {
                try {
                    android.util.Log.d("ScanScreen", "📸 Taking picture...")
                    imageCapture.takePicture(
                        executor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                try {
                                    android.util.Log.d("ScanScreen", "✅ Picture captured successfully")
                                    onImageCaptured(image)
                                } catch (e: Exception) {
                                    android.util.Log.e("ScanScreen", "❌ Error processing captured image", e)
                                }
                            }
                            override fun onError(exception: ImageCaptureException) {
                                android.util.Log.e("ScanScreen", "❌ Camera capture error", exception)
                            }
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ScanScreen", "❌ Exception calling takePicture", e)
                }
            }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White.copy(0.3f))
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Scan",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/* ===================== RESULT SHEET ===================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultSheet(
    bitmap: Bitmap,
    label: String,
    confidence: Int,
    foodInfo: Food?,
    selectedMealType: com.example.nutritrack.domain.model.MealType,
    onMealTypeChange: (com.example.nutritrack.domain.model.MealType) -> Unit,
    onSave: () -> Unit,
    onDone: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDone,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(24.dp)) {
                if (confidence <= 45) {
                    Text(
                        "Objek tidak dikenali",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Text("Pastikan pencahayaan cukup dan objek terlihat jelas.", color = Color.Gray)

                    Spacer(Modifier.height(24.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDone,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Try Again", color = Color.White)
                    }
                } else {
                    Text("Detected Food", color = Color.Gray)
                    Text(foodInfo?.name ?: label, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Akurasi: $confidence%", fontSize = 14.sp, color = DarkGreen)

                    if (foodInfo != null) {
                        Spacer(Modifier.height(16.dp))
                        NutritionCard(foodInfo)

                        Spacer(Modifier.height(24.dp))
                        Text("Meal Type", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.example.nutritrack.domain.model.MealType.entries.forEach { mealType ->
                                FilterChip(
                                    selected = selectedMealType == mealType,
                                    onClick = { onMealTypeChange(mealType) },
                                    label = { Text(mealType.displayName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = onDone
                            ) {
                                Text("Retake")
                            }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = onSave,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Text("Done", color = Color.White)
                            }
                        }
                    } else {
                        Spacer(Modifier.height(16.dp))
                        Text("Data nutrisi tidak ditemukan untuk item ini.", color = Color.Gray)

                        Spacer(Modifier.height(24.dp))
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onDone,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                        ) {
                            Text("Done", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionCard(food: Food) {
    Surface(
        color = Color.Gray.copy(0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutritionItem("Calories", "${food.nutrition.calories.toInt()} kcal")
            NutritionItem("Protein", "${food.nutrition.protein}g")
            NutritionItem("Carbs", "${food.nutrition.carbs}g")
            NutritionItem("Fat", "${food.nutrition.fat}g")
        }
    }
}

@Composable
fun NutritionItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/* ===================== PERMISSION ===================== */
@Composable
fun NoPermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera permission required")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequestPermission) { Text("Grant Permission") }
    }
}

/* ===================== TFLITE CLASSIFIER ===================== */
class NutriClassifier(context: Context) {
    private val labels = try {
        FileUtil.loadLabels(context, "labels.txt")
    } catch (e: Exception) {
        Log.e("ML", "Label load error", e)
        listOf("Unknown")
    }

    private val model: NutriMobilenet by lazy { NutriMobilenet.newInstance(context) }

    private val imageProcessor = TFLiteImageProcessor.Builder()
        .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
        .build()

    data class Result(val label: String, val confidence: Int)

    fun classify(bitmap: Bitmap): Result {
        return try {
            var tfImage = TensorImage(DataType.FLOAT32)
            tfImage.load(bitmap)
            tfImage = imageProcessor.process(tfImage)

            val outputs = model.process(tfImage.tensorBuffer)
            val probs = outputs.outputFeature0AsTensorBuffer.floatArray

            val maxIdx = probs.indices.maxByOrNull { index -> probs[index] } ?: 0
            val maxScore = probs[maxIdx]

            Result(labels.getOrElse(maxIdx) { "Unknown" }, (maxScore * 100).roundToInt())
        } catch (e: Exception) {
            Log.e("ML", "Classification error", e)
            Result("Classification Error", 0)
        }
    }
}
