package com.example.nutritrack

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@Composable
fun ScanScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            FoodImageScanner()
        } else {
            NoPermissionScreen {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
fun FoodImageScanner() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    var detectedLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Tampilan untuk kamera preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture // Tambahkan use case ImageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("FoodImageScanner", "Use case binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Tombol untuk mengambil gambar
        IconButton(
            onClick = {
                isLoading = true
                takePictureAndAnalyze(imageCapture, executor) { labels ->
                    detectedLabels = labels
                    isLoading = false
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .size(80.dp)
                .border(4.dp, Color.White, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Ambil Gambar",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        // Tampilkan loading indicator
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Tampilkan dialog hasil analisis
        if (detectedLabels.isNotEmpty()) {
            AnalysisResultDialog(
                labels = detectedLabels,
                onDismiss = { detectedLabels = emptyList() }
            )
        }
    }
}

private fun takePictureAndAnalyze(
    imageCapture: ImageCapture,
    executor: Executor,
    onResult: (List<String>) -> Unit
) {
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
        override fun onCaptureSuccess(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        // Ambil teks dari 3 label dengan keyakinan tertinggi
                        val topLabels = labels.sortedByDescending { it.confidence }
                            .take(3)
                            .map { "${it.text} (${"%.0f".format(it.confidence * 100)}%)" }
                        onResult(topLabels)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ImageAnalysis", "Analisis gambar gagal.", e)
                        onResult(listOf("Gagal menganalisis gambar"))
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("ImageAnalysis", "Gagal mengambil gambar.", exception)
            onResult(listOf("Gagal mengambil gambar"))
        }
    })
}

@Composable
fun AnalysisResultDialog(labels: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hasil Analisis") },
        text = {
            Column {
                Text("Kami mendeteksi objek berikut di gambar:")
                Spacer(modifier = Modifier.height(8.dp))
                labels.forEach { label ->
                    Text("• $label", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pilih salah satu untuk melihat detail gizinya.", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(onClick = {
                // TODO: Navigasi ke halaman detail gizi dengan label yang dipilih
                onDismiss()
            }) {
                Text("Pilih")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun NoPermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Izin Kamera Diperlukan",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aplikasi ini memerlukan akses ke kamera untuk dapat menganalisis gambar makanan.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Berikan Izin")
        }
    }
}
