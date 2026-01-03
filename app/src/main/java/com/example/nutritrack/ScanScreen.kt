//Danendra Farrel Adriansyah - 23523170
//ada 3 screen dalam 1 file .kt ( ScreenCam, PermisionCam, ScanResult )
//Screen Untuk Scan Makanan
package com.example.nutritrack

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.nutritrack.ui.theme.DarkGreen
import com.example.nutritrack.ui.theme.NutriTrackTheme
import com.example.nutritrack.ui.theme.TextGray
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

private enum class ScanState {
    SCANNING,
    LOADING,
    RESULT
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var capturedImageProxy by remember { mutableStateOf<ImageProxy?>(null) }
    var scanState by remember { mutableStateOf(ScanState.SCANNING) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                //
            }
        }
    )

    LaunchedEffect(key1 = true) {
        if (!cameraPermissionState.status.isGranted) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            AIScannerUI(
                onImageCaptured = { imageProxy ->
                    capturedImageProxy = imageProxy
                    scanState = ScanState.RESULT
                },
                onLoadingStateChange = { isLoading ->
                    scanState = if (isLoading) ScanState.LOADING else ScanState.SCANNING
                }
            )
        } else {
            NoPermissionScreen {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }

        if (scanState == ScanState.LOADING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }

    if (scanState == ScanState.RESULT && capturedImageProxy != null) {
        ScanResultSheet(
            imageProxy = capturedImageProxy!!,
            onDone = {
                capturedImageProxy?.close()
                capturedImageProxy = null
                scanState = ScanState.SCANNING
            }
        )
    }
}


@Composable fun AIScannerUI(
    onImageCaptured: (ImageProxy) -> Unit,
    onLoadingStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Camera Preview ---
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraXPreview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                    } catch (e: Exception) {
                        Log.e("AIScannerUI", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Top Bar ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: Aksi Tutup */ }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Text("AI Scanner", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { /* TODO: Opsi lain */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SegmentedCameraMode()

                IconButton(
                    onClick = {
                        onLoadingStateChange(true)
                        imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                onLoadingStateChange(false)
                                onImageCaptured(image)
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onLoadingStateChange(false)
                                Log.e("AIScannerUI", "Gagal mengambil gambar.", exception)
                            }
                        })
                    },
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Scan",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoPermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera Permission Required", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("This feature needs camera access to scan food items.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun SegmentedCameraMode() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tombol AI Camera (Selected)
            Button(
                onClick = { /*TODO*/ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen, contentColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Camera", fontSize = 12.sp)
                }
            }
            // Tombol Barcode (Not Selected)
            TextButton(onClick = { /*TODO*/ }) {
                Text("Barcode", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultSheet(imageProxy: ImageProxy, onDone: () -> Unit) {
    val imageBitmap = remember(imageProxy) { imageProxy.toBitmap().asImageBitmap() }

    ModalBottomSheet(
        onDismissRequest = onDone, // Tutup sheet saat di-dismiss
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF3F4F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Padding untuk system navigation bar
        ) {
            // Gambar Hasil Scan
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = "Scanned food",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            // Detail Makanan
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FoodTitleSection()
                NutritionGrid()

                // Tombol Done
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Done", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FoodTitleSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Breakfast", color = TextGray, fontSize = 14.sp)
            Text(
                "Kiwi Smoothie Bowl \nwith Granola",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )
        }
        // Stepper
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextGray)
                Text("1", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = DarkGreen)
            }
        }
    }
}

@Composable
fun NutritionGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NutritionCard(
                name = "Calories",
                value = "450",
                icon = Icons.Default.LocalFireDepartment,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
            NutritionCard(
                name = "Protein",
                value = "20gm",
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NutritionCard(
                name = "Carbs",
                value = "140gm",
                icon = Icons.Default.BubbleChart,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
            NutritionCard(
                name = "Fat",
                value = "12gm",
                icon = Icons.Default.WaterDrop,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NutritionCard(
    name: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = name, tint = color)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(name, color = TextGray, fontSize = 14.sp)
                }
                Text("Edit", color = TextGray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    NutriTrackTheme {
        ScanScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun NoPermissionScreenPreview() {
    NutriTrackTheme {
        NoPermissionScreen { }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AIScannerUIPreview() {
    NutriTrackTheme {
        AIScannerUI(onImageCaptured = {}, onLoadingStateChange = {})
    }
}