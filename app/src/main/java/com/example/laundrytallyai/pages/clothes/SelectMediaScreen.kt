package com.example.laundrytallyai.pages.clothes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.dataschemes.PredictionsData
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.yolo.BoundingBox
import com.example.laundrytallyai.yolo.Constants.LABELS_PATH
import com.example.laundrytallyai.yolo.Constants.MODEL_PATH
import com.example.laundrytallyai.yolo.YoloDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SelectMediaScreen(viewModel: ClothesViewModel, navController: NavController) {
    val context = LocalContext.current
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

    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val detector = remember {
        YoloDetector(context, MODEL_PATH, LABELS_PATH, object : YoloDetector.DetectorListener {
            override fun onEmptyDetect() {
                val predictionData = selectedImageBitmap?.let { PredictionsData(it, "") }

                if (predictionData != null)
                    viewModel.setPredictions(listOf(predictionData))
            }

            override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
                val predictions = mutableListOf<PredictionsData>()

                if (boundingBoxes.isNotEmpty()) {
                    boundingBoxes.forEach { box ->
                        val croppedBitmap = selectedImageBitmap?.let {
                            Bitmap.createBitmap(
                                it,
                                (box.x1 * it.width).toInt(),
                                (box.y1 * it.height).toInt(),
                                ((box.x2 - box.x1) * it.width).toInt(),
                                ((box.y2 - box.y1) * it.height).toInt()
                            )
                        }
                        croppedBitmap?.let {
                            PredictionsData(
                                it,
                                box.clsName
                            )
                        }?.let {
                            predictions.add(
                                it
                            )
                        }
                    }
                } else {
                    selectedImageBitmap?.let { PredictionsData(it, "") }
                        ?.let { predictions.add(it) }
                }

                viewModel.setPredictions(predictions)

                navController.navigate(Screen.ClothesCreate.route)
            }
        })
    }

    LaunchedEffect(Unit) {
        detector.setup()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        try {
            if (uri != null) {
                try {
                    selectedImageBitmap =
                        if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source)
                        }


                    selectedImageBitmap = if (selectedImageBitmap?.config != Bitmap.Config.ARGB_8888) {
                        selectedImageBitmap?.copy(Bitmap.Config.ARGB_8888, true)
                    } else {
                        selectedImageBitmap
                    }
                    selectedImageBitmap?.let { detector.detect(it) }
                } catch (e: Exception) {
                    Log.e("SelectMediaScreen", "Error processing image", e)
                }
            }
        } catch (e: Exception) {
            Log.e("SelectMediaScreen", e.message.toString())
        }
    }

    PageTitle(
        title = "New Clothes",
        onBackClick = {
            navController.navigate(Screen.Clothes.route)
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top space
        Spacer(modifier = Modifier.weight(1f))

        // Image placeholders row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ImagePlaceholder(
                icon = ImageVector.vectorResource(id = R.drawable.upload),
                onClick = {
                    galleryLauncher.launch("image/*")
                }
            )
            ImagePlaceholder(
                icon = ImageVector.vectorResource(id = R.drawable.camera),
                onClick = {
                    if (hasCameraPermission) {
                        navController.navigate(Screen.CameraPreview.route)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }

        // Text below images
        Text(
            text = "Clothing Image",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "We will infer the number of clothes instances within the image, and categorize the clothes by its colors and types.",
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )

        // Bottom space
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ImagePlaceholder(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .size(100.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}