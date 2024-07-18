package com.example.laundrytallyai.pages.clothes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.dataschemes.PredictionsData
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation
import com.example.laundrytallyai.yolo.BoundingBox
import com.example.laundrytallyai.yolo.Constants.BOUNDING_RECT_TEXT_PADDING
import com.example.laundrytallyai.yolo.Constants.LABELS_PATH
import com.example.laundrytallyai.yolo.Constants.MODEL_PATH
import com.example.laundrytallyai.yolo.YoloDetector
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraClientScreen(
    viewModel: ClothesViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

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
        var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
        var boundingBoxes by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(context, lifecycleOwner, onImageCapture = { capture ->
                imageCapture = capture
            }, onBoundingBoxed = { boxes ->
                boundingBoxes = boxes
            })
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
                    onClick = {
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("CameraClientScreen", "Error navigating back $e")
                        }


                    }
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
                    onClick = {
                        imageCapture?.let { capture ->
                            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                                createTempFile(
                                    context
                                )
                            ).build()
                            capture.takePicture(
                                outputFileOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val savedUri = outputFileResults.savedUri ?: return
                                        val bitmap = BitmapFactory.decodeFile(savedUri.path)

                                        val predictions = mutableListOf<PredictionsData>()

                                        if (boundingBoxes.isNotEmpty()) {
                                            boundingBoxes.forEach { box ->
                                                val croppedBitmap = Bitmap.createBitmap(
                                                    bitmap,
                                                    (box.x1 * bitmap.width).toInt(),
                                                    (box.y1 * bitmap.height).toInt(),
                                                    ((box.x2 - box.x1) * bitmap.width).toInt(),
                                                    ((box.y2 - box.y1) * bitmap.height).toInt()
                                                )
                                                predictions.add(
                                                    PredictionsData(
                                                        croppedBitmap,
                                                        box.clsName
                                                    )
                                                )
                                            }
                                        } else {
                                            predictions.add(PredictionsData(bitmap, ""))
                                        }

                                        viewModel.setPredictions(predictions)

                                        navController.navigate(Screen.ClothesCreate.route)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e(
                                            "CameraCapture",
                                            "Error taking picture $exception"
                                        )
                                    }

                                }
                            )
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(48.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.camera),
                        contentDescription = "Take Picture",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    } else {
        Text("Camera permission is required")
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCapture: (ImageCapture) -> Unit,
    onBoundingBoxed: (List<BoundingBox>) -> Unit
) {
    var isEmptyDetection by remember { mutableStateOf(true) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var boundedBoxes by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
    var inferredTime by remember { mutableLongStateOf(0L) }
    val detector = remember {
        YoloDetector(context, MODEL_PATH, LABELS_PATH, object : YoloDetector.DetectorListener {
            override fun onEmptyDetect() {
                isEmptyDetection = true
                boundedBoxes = emptyList()
                inferredTime = 0L
                onBoundingBoxed(emptyList())
            }

            override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
                isEmptyDetection = false
                inferredTime = inferenceTime
                boundedBoxes = boundingBoxes
                onBoundingBoxed(boundingBoxes)
            }
        })
    }


    LaunchedEffect(Unit) {
        detector.setup()
    }

    Box(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also { analyzer ->
                            analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                                val bitmapBuffer =
                                    Bitmap.createBitmap(
                                        imageProxy.width,
                                        imageProxy.height,
                                        Bitmap.Config.ARGB_8888
                                    )
                                imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
                                imageProxy.close()

                                val matrix = Matrix().apply {
                                    postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                                }

                                val rotatedBitmap = Bitmap.createBitmap(
                                    bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                                    matrix, true
                                )

                                detector.detect(rotatedBitmap)
                            }
                        }

                    val imageCapture = ImageCapture.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()

                    onImageCapture(imageCapture)

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer,
                            imageCapture
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        DetectionOverlay(context, boundedBoxes, isEmptyDetection)

        Text("Inference Time: $inferredTime ms")
    }

//    DisposableEffect(Unit) {
//        onDispose {
//            cameraExecutor.shutdown()
//            detector.clear()
//        }
//    }
}

@Composable
fun DetectionOverlay(
    context: Context,
    boundingBoxes: List<BoundingBox>,
    isEmptyDetection: Boolean
) {
    if (isEmptyDetection) {
        return
    }

    val boxPaint = remember {
        Paint().apply {
            style = PaintingStyle.Stroke
            strokeWidth = 8f
            color = Color(ContextCompat.getColor(context, R.color.bounding_box_color))
        }
    }

    val textBackgroundPaint = remember {
        android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.BLACK
            textSize = 50f
        }
    }

    val textPaint = remember {
        android.graphics.Paint().apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.WHITE
            textSize = 50f
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            boundingBoxes.forEach { box ->
                val left = box.x1 * size.width
                val top = box.y1 * size.height
                val right = box.x2 * size.width
                val bottom = box.y2 * size.height

                // Draw bounding box
                canvas.drawRect(left, top, right, bottom, boxPaint)

                // Draw label
                val drawableText = box.clsName
                val textBounds = android.graphics.Rect()
                textPaint.getTextBounds(drawableText, 0, drawableText.length, textBounds)
                val textWidth = textBounds.width()
                val textHeight = textBounds.height()

                canvas.nativeCanvas.drawRect(
                    left,
                    top,
                    left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                    top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                    textBackgroundPaint
                )

                canvas.nativeCanvas.drawText(
                    drawableText,
                    left,
                    top + textBounds.height(),
                    textPaint
                )
            }
        }
    }
}

fun createTempFile(context: Context): File {

    return File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {

        deleteOnExit()

    }

}