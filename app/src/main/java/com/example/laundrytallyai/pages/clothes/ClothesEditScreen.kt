package com.example.laundrytallyai.pages.clothes

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.datastates.ModifyDataState
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothesEditScreen(
    viewModel: ClothesViewModel,
    navController: NavController,
    paddingValues: PaddingValues? = null
) {
    val clothesData by viewModel.selectedClothes.collectAsState()
    val context = LocalContext.current
    val modifyState by viewModel.modifyState.collectAsState()
    var clothingType by remember { mutableStateOf(clothesData?.type) }
    var clothingColor by remember { mutableStateOf(clothesData?.color) }
    var clothingDesc by remember { mutableStateOf(clothesData?.desc) }
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

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = modifyState) {
            is ModifyDataState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.setModified(ModifyDataState.Idle)
            }

            is ModifyDataState.Error -> {
                Text("${state.code}, ${state.error}")
                viewModel.setModified(ModifyDataState.Idle)
            }

            is ModifyDataState.Loading -> RotatingArcLoadingAnimation()
            is ModifyDataState.Idle -> {

            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            PageTitle(
                title = "Edit Clothes",
                onBackClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .aspectRatio(1f)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (selectedImageBitmap != null) {
                    Image(
                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = BASE_URL + clothesData?.cloth_pic,
                        contentDescription = "Clothing Image"
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                clothingType?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = it,
                        onValueChange = { newType ->
                            clothingType = newType
                        },
                        label = { Text("Clothing Type") },
                        singleLine = true
                    )
                }

                clothingColor?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = it,
                        onValueChange = { newColor ->
                            clothingColor = newColor
                        },
                        label = { Text("Clothing Color") },
                        singleLine = true
                    )
                }

                clothingDesc?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = it,
                        onValueChange = { newDesc ->
                            clothingDesc = newDesc
                        },
                        label = { Text("Clothing Description") },
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (clothingType.isNullOrBlank() || clothingColor.isNullOrBlank() || clothingDesc.isNullOrBlank()) {
                            return@Button
                        }
                        viewModel.editClothes(
                            clothesId = clothesData?.id ?: return@Button,
                            clothingType = clothingType ?: return@Button,
                            clothingColor = clothingColor ?: return@Button,
                            clothingDesc = clothingDesc ?: return@Button,
                            clothingImage = selectedImageBitmap?.let { bitmap ->
                                // Convert Bitmap to File
                                val file = File(context.cacheDir, "temp_image.jpg")
                                file.outputStream().use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                }
                                file
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Submit")
                }
            }
        }
    }
}