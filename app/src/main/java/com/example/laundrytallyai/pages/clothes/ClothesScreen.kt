package com.example.laundrytallyai.pages.clothes

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.components.BackgroundedItem
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation

@Composable
fun ClothesScreen(navController: NavController, paddingValues: PaddingValues? = null) {
    val viewModel: ClothesViewModel = hiltViewModel()
    val dataState by viewModel.dataState.collectAsState()

    if (viewModel.getToken() == null) {
        navController.navigate("login")
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
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
                                imageUrl = BASE_URL + clothes.cloth_pic,
                                primaryText = clothes.type,
                                secondaryText = clothes.color
                            )
                        }
                        items(11) {
                            BackgroundedItem(
                                imageUrl = BASE_URL + "/clothes/clothes1.jpg",
                                primaryText = "T-Shirt",
                                secondaryText = "Black"
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
            }
        }

        is ClothesDataState.Error -> {
            if (state.message == "401" || state.message == "403") {
                Text(text = "Error: ${state.message}")
                viewModel.deleteToken()
                navController.navigate("login")
            } else {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}