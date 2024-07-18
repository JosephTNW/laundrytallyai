package com.example.laundrytallyai.pages.clothes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Space
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.dataschemes.ClothesFormItemData
import com.example.laundrytallyai.api.datastates.ModifyDataState
import com.example.laundrytallyai.api.datastates.TypePredictionDataState
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation
import java.io.File
import kotlin.math.exp

@Composable
fun ClothesCreateScreen(viewModel: ClothesViewModel, navController: NavController) {
    val predictedClothes = viewModel.predictedClothes.collectAsState()
    val predictedTypes = viewModel.predictedType.collectAsState()
    val clothingItems = remember { mutableStateListOf<ClothesFormItemData>() }
    val context = LocalContext.current

    LaunchedEffect(predictedClothes.value) {
        clothingItems.clear()

        viewModel.predictClothingType(predictedClothes.value.map {
            val file = File(context.cacheDir, "temp_image.jpg")
            file.outputStream().use { out ->
                it.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            file
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            clothingItems.clear()
            viewModel.clearPredictedTypesData() // Implement this function to reset the ViewModel's state
            viewModel.setModified(ModifyDataState.Idle)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PageTitle(title = "New Clothes", onBackClick = { navController.popBackStack() })
        when (val state = predictedTypes.value) {
            is TypePredictionDataState.Loading -> {
                RotatingArcLoadingAnimation()
            }

            is TypePredictionDataState.Error -> {
                Text("Code: ${state.code} Error: ${state.error}")
            }

            is TypePredictionDataState.Success -> {
                clothingItems.clear()

                clothingItems.addAll(predictedClothes.value.mapIndexed { index, cloth ->
                    ClothesFormItemData(
                        desc = mutableStateOf(""),
                        c_type = mutableStateOf(state.data[index]),
                        color = mutableStateOf(cloth.className),
                        cloth_pic = mutableStateOf(cloth.bitmap)
                    )
                })

                ClothesSuccessScreen(
                    clothingItems = clothingItems,
                    navController = navController,
                    context = context,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ClothesSuccessScreen(
    clothingItems: SnapshotStateList<ClothesFormItemData>,
    navController: NavController,
    context: Context,
    viewModel: ClothesViewModel,
) {
    val modifyState = viewModel.modifyState.collectAsState()

    when (val state = modifyState.value) {
        is ModifyDataState.Idle -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp, start = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(clothingItems.size) { index ->
                        ClothingFormItem(
                            item = clothingItems[index],
                            index = index,
                            context = context,
                            onItemDeleted = {
                                clothingItems.removeAt(index)
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            clothingItems.add(
                                ClothesFormItemData(
                                    mutableStateOf(""),
                                    mutableStateOf(""),
                                    mutableStateOf(""),
                                    mutableStateOf(
                                        Bitmap.createBitmap(
                                            1,
                                            1,
                                            Bitmap.Config.ARGB_8888
                                        )
                                    )
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Add Another Clothing Item to Input",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { navController.navigate(Screen.Home.route) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                viewModel.submitAllClothesData(clothingItems)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Submit All")
                        }
                    }
                }
            }
        }

        is ModifyDataState.Success -> {
            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Clothes.route)
            viewModel.setModified(ModifyDataState.Idle)
        }

        is ModifyDataState.Loading -> RotatingArcLoadingAnimation()

        is ModifyDataState.Error -> {
            Text(text = "code: ${state.code}, message: ${state.error}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingFormItem(
    item: ClothesFormItemData,
    index: Int,
    context: Context,
    onItemDeleted: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }

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
                item.cloth_pic.value = bitmap
            }
        }

    val pickPhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Handle the selected image URI
            if (uri != null) {
                item.cloth_pic.value = if (Build.VERSION.SDK_INT < 28) {
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
        modifier = Modifier.wrapContentHeight()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clothing ${index + 1}",
                style = MaterialTheme.typography.headlineSmall
            )
            Row {

                IconButton(onClick = onItemDeleted) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
                IconButton(onClick = {
                    expanded.value = !expanded.value
                }) {
                    Icon(
                        imageVector = if (expanded.value) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = if (expanded.value) "Collapse" else "Expand"
                    )
                }
            }
        }

        if (expanded.value) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.LightGray)
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    bitmap = item.cloth_pic.value.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize()
                )
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

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.c_type.value,
                onValueChange = { item.c_type.value = it },
                label = { Text("Clothing Type") },
                supportingText = {
                    Text(text = "Predicted, may need revision")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.color.value,
                onValueChange = { item.color.value = it },
                label = { Text("Clothing Color") },
                supportingText = {
                    Text(text = "Predicted, may need revision")
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = item.desc.value,
                onValueChange = { item.desc.value = it },
                label = { Text("Description") }
            )
        }
    }
}