package com.example.laundrytallyai.pages.laundries

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.components.SectionTitle
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.pages.clothes.ClothesViewModel
import com.example.laundrytallyai.pages.launderers.LaundererViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryCreateScreen(
    laundryViewModel: LaundryViewModel,
    clothesViewModel: ClothesViewModel,
    laundererViewModel: LaundererViewModel,
    navController: NavController,
) {
    val selectedClothes by laundryViewModel.selectedClothesState.collectAsState()
    val selectedLaunderer by laundererViewModel.selectedLaunderer.collectAsState()
    val laundryDays = remember { mutableStateOf("") }
    val context = LocalContext.current

    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
            hasStoragePermission =
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: hasStoragePermission
        }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            // Handle the captured image
            if (bitmap != null) {
                selectedImageBitmap = bitmap
            }
        }

    val pickPhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Handle the selected image URI
            if (uri != null) {
                selectedImageBitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
            }
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PageTitle(
            title = "New Laundry at ${selectedLaunderer?.name}",
            fontSize = 20.sp,
            onBackClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.padding(top = 0.dp)
        ) {
            SectionTitle(
                text = "Select Clothes",
                onClick = {
                    laundryViewModel.setSelectedClothes(selectedClothes)
                    navController.navigate(Screen.LaundrySelectClothes.route)
                }
            )
            LazyRow {
                if (selectedClothes.isEmpty()) {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .size(100.dp)
                                .padding(start = 16.dp, top = 8.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.t_shirt),
                                contentDescription = "Clothes Placeholder",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    items(selectedClothes) { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .size(100.dp)
                                .padding(start = 16.dp, top = 8.dp)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                ),
                        ) {
                            AsyncImage(
                                modifier = Modifier.fillMaxWidth(),
                                model = BASE_URL + item?.cloth_pic,
                                contentDescription = "Clothes Picture",
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        SectionTitle(text = "Bill Picture")
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .size(150.dp)
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageBitmap == null) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Bill Picture Placeholder",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Image(
                    bitmap = selectedImageBitmap!!.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Black.copy(0.5f))
                        .clickable {
                            takePictureLauncher.launch(null)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.camera),
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Black.copy(0.5f))
                        .clickable {
                            pickPhotoLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.upload),
                        contentDescription = "Upload Photo",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            value = laundryDays.value,
            onValueChange = { value: String ->
                if (value.toIntOrNull() != null) {
                    laundryDays.value = value
                }
            },
            label = { Text("Laundry Days") },
            placeholder = { Text(text = "e.g., 3 (In Days)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {
                selectedLaunderer?.id?.let { laundererId ->
                    selectedImageBitmap?.let { bitmap ->
                        // Convert Bitmap to File
                        val file = File(context.cacheDir, "temp_image.jpg")
                        file.outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        }
                        file
                    }?.let { file ->
                        laundryViewModel.createLaundry(
                            laundererId = laundererId,
                            clothesIds = selectedClothes.map { it!!.id }.toIntArray(),
                            laundryDays = laundryDays.value.toInt(),
                            billPic = file
                        )
                    }
                }
                navController.navigate(Screen.Laundry.route)
            }
        ) {
            Text(text = "Submit")
        }
    }
}