package com.example.laundrytallyai.pages.laundries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.components.BackgroundedItem
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.pages.clothes.ClothesViewModel
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation

@Composable
fun LaundryClothesSelectScreen(
    clothesViewModel: ClothesViewModel,
    laundryViewModel: LaundryViewModel,
    navController: NavController
) {
    val dataState by clothesViewModel.dataState.collectAsState()
    val previousSelectedClothes by laundryViewModel.selectedClothesState.collectAsState()
    var selectedClothes by remember { mutableStateOf(previousSelectedClothes) }

    LaunchedEffect(Unit) {
        clothesViewModel.fetchData()
    }

    when (val state = dataState) {
        is ClothesDataState.Loading -> RotatingArcLoadingAnimation()
        is ClothesDataState.Success -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    PageTitle(
                        title = "Validate Clothes",
                        onBackClick = { navController.popBackStack() })

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
                                secondaryText = clothes.color,
                                onChecked = { isChecked ->
                                    selectedClothes = if (isChecked) {
                                        (selectedClothes.toList() + clothes)
                                    } else {
                                        selectedClothes.filter { it != clothes }
                                    }
                                },
                                checked = (selectedClothes.contains(clothes))
                            )
                        }
                    }
                }
                WideFAB(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    text = "Input selected ${selectedClothes.size} Clothes",
                    onClick = {
                        laundryViewModel.setSelectedClothes(
                            selectedClothes
                        )
                        navController.navigate(Screen.LaundryCreate.route)
                    },
                    colors = MaterialTheme.colorScheme.primary
                )
            }
        }

        is ClothesDataState.Error -> {

        }
    }

}