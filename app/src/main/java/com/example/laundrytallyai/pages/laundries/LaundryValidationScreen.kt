package com.example.laundrytallyai.pages.laundries

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.datastates.ModifyDataState
import com.example.laundrytallyai.components.BackgroundedItem
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen

@Composable
fun LaundryValidationScreen(viewModel: LaundryViewModel, navController: NavController) {
    val laundryData by viewModel.selectedLaundry.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    var validatedClothes by remember { mutableStateOf(intArrayOf()) }
    var showResult by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(validationState) {
        Toast(context).apply {
            when (validationState) {
                is ModifyDataState.Loading -> {
                    setText("Validating Clothes...")
                    show()
                }

                is ModifyDataState.Success -> {
                    setText("Clothes Validated!")
                    show()
                }

                is ModifyDataState.Error -> {
                    setText(
                        "Error ${(validationState as ModifyDataState.Error).code}: " +
                                (validationState as ModifyDataState.Error).error
                    )
                    show()
                }

                is ModifyDataState.Idle -> {
                    // Do nothing
                }
            }
            viewModel.setValidationState(ModifyDataState.Idle)
        }
    }

    laundryData?.let { data ->
        val clothesData = data.clothes
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

                    items(clothesData) { clothes ->

                        BackgroundedItem(
                            imageUrl = BASE_URL + clothes.cloth_pic,
                            primaryText = clothes.type,
                            secondaryText = clothes.color,
                            onChecked = { isChecked ->
                                validatedClothes = if (isChecked) {
                                    (validatedClothes.toList() + clothes.id).toIntArray()
                                } else {
                                    validatedClothes.filter { it != clothes.id }.toIntArray()
                                }
                            },
                            checked = (validatedClothes.contains(clothes.id))
                        )
                    }
                }
            }
            WideFAB(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = "Finish (${validatedClothes.size} / ${clothesData.size} Clothes Validated)",
                onClick = {
                    viewModel.validateLaundry(
                        laundryId = laundryData!!.id,
                        clothesIds = validatedClothes
                    )
                    showResult = true
                    navController.navigate(Screen.Laundry.route)
                },
                colors = if (validatedClothes.size == clothesData.size) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            )
        }
    }
}

@Composable
fun WideFAB(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: Color = MaterialTheme.colorScheme.secondary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Text(text = text)
    }
}
