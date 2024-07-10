package com.example.laundrytallyai.pages.clothes

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.navigation.NavController
import com.example.laundrytallyai.R
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen
import java.io.File

@Composable
fun SelectMediaScreen(navController: NavController) {
    val context = LocalContext.current
    val imageUri by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    ) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // Handle the image captured by the camera
            imageUri?.let {
                // Process the captured image
                // For example, you might want to save it or display it
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Handle the selected image URI here
        uri?.let {
            // Process the selected image
            // For example, you might want to save it or display it
        }
    }

    fun getTempFileUri(): Uri {
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
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
        onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}