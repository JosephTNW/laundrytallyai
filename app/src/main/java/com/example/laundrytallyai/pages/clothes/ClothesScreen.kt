package com.example.laundrytallyai.pages.clothes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.api.datastates.ModifyClothesState
import com.example.laundrytallyai.components.BackgroundedItem
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation

@Composable
fun ClothesScreen(
    viewModel: ClothesViewModel,
    navController: NavController,
    paddingValues: PaddingValues? = null
) {
    val dataState by viewModel.dataState.collectAsState()
    val modifyState by viewModel.modifyState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedClothes by remember { mutableStateOf<ClothesData?>(null) }
    val context = LocalContext.current

    if (viewModel.getToken() == null) {
        navController.navigate("login")
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    LaunchedEffect(modifyState) {
        when (modifyState) {
            is ModifyClothesState.Success -> {
                if (selectedClothes != null) {
                    Toast.makeText(
                        context,
                        (modifyState as ModifyClothesState.Success).message,
                        Toast.LENGTH_SHORT
                    ).show()
                    selectedClothes = null
                }
            }

            is ModifyClothesState.Error -> {
                Toast.makeText(
                    context,
                    "${(modifyState as ModifyClothesState.Error).code} " +
                            ": ${(modifyState as ModifyClothesState.Error).error}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> Unit
        }
    }

    when (val state = dataState) {
        is ClothesDataState.Loading -> RotatingArcLoadingAnimation()
        is ClothesDataState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                ) {
                    PageTitle(title = "Your Clothes")
                    LazyVerticalGrid(
                        modifier = Modifier
                            .fillMaxSize(),
                        columns = GridCells.Adaptive(100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data) { clothes ->
                            BackgroundedItem(
                                modifier = Modifier.clickable {
                                    viewModel.setSelectedClothes(clothes)
                                    navController.navigate(Screen.ClothesDetail.route)
                                },
                                imageUrl = BASE_URL + clothes.cloth_pic,
                                primaryText = clothes.type,
                                secondaryText = clothes.color,
                                onDelete = {
                                    selectedClothes = clothes
                                    showDialog = true
                                },
                                onUpdate = {}
                            )
                        }

                    }
                }

                FloatingActionButton(
                    onClick = {
                        navController.navigate("select-media")
                    },
                    modifier = Modifier
                        .zIndex(1f)
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }

                if (showDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.Black.copy(alpha = 0.5f))
                    )
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                        },
                        title = {
                            Text(text = "Confirm Deletion")
                        },
                        text = {
                            Text(text = "Are you sure you want to delete clothes with type ${selectedClothes?.type} and color ${selectedClothes?.color}?")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    // Perform the delete action here
                                    selectedClothes?.let { viewModel.deleteClothes(it.id) }
                                    showDialog = false
                                }
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }

        is ClothesDataState.Error -> {
            if (state.code == "401" || state.code == "403") {
                Text(text = "Error: ${state.error}")
                viewModel.deleteToken()
                navController.navigate("login")
            } else {
                Text(text = "Code: ${state.code} Error: ${state.error}")
            }
        }
    }
}