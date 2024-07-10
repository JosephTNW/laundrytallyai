package com.example.laundrytallyai.pages.clothes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.laundrytallyai.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraXScreen(navController: NavController) {
    val context = LocalContext.current
    Log.d("CameraXScreen", "CameraXScreen Called")
    val detector = remember { YoloDetector(context) }
    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            CameraPreview(
                context = context,
                lifecycleOwner = lifecycleOwner,
                cameraExecutor = cameraExecutor,
                modifier = Modifier.fillMaxSize(),
                onFrameAnalyzed = { bitmap ->
                    detections = detector.detect(bitmap)
                }
            )

            DetectionOverlay(
                detections = detections,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left IconButton
                IconButton(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Left Icon",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
                IconButton(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    onClick = { /* Handle other buttons */ }) {
                    Icon(
                        modifier = Modifier
                            .size(48.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.camera),
                        contentDescription = "Take Picture",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.size(70.dp))
            }

        }
    } else {
        Text(
            text = "Camera permission is required",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraExecutor: ExecutorService,
    onFrameAnalyzed: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)


            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(
                        android.util.Size(
                            800,
                            800
                        )
                    )  // Adjust based on your model's input size
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                fun Bitmap.rotate(degrees: Float): Bitmap {
                    val matrix = Matrix().apply { postRotate(degrees) }
                    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
                }

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val rotatedBitmap =
                        imageProxy.toBitmap().rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                    onFrameAnalyzed(rotatedBitmap)
                    imageProxy.close()
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

@Composable
fun DetectionOverlay(detections: List<Detection>, modifier: Modifier) {
    Canvas(modifier = modifier) {
        detections.forEach { detection ->
            drawRect(
                color = Color.Blue,
                topLeft = Offset(detection.bbox.left, detection.bbox.top),
                size = Size(detection.bbox.width(), detection.bbox.height()),
                style = Stroke(width = 2f)
            )
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "${detection.label} ${detection.confidence}",
                    detection.bbox.left,
                    detection.bbox.top - 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLUE
                        textSize = 16 * density
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}