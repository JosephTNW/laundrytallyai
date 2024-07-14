package com.example.laundrytallyai.pages.laundries

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.components.SectionTitle
import com.example.laundrytallyai.pages.clothes.ClothesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLaundryScreen(
    laundryViewModel: LaundryViewModel,
    clothesViewModel: ClothesViewModel,
    navController: NavController,
    laundererId: String,
    laundererName: String
) {
    val selectedClothes by remember { mutableStateOf<List<ClothesData?>>(emptyList()) }
    val laundryDays = remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PageTitle(
            title = "New Laundry at $laundererName",
            fontSize = 20.sp,
            onBackClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.padding(top = 0.dp)
        ) {
            SectionTitle(text = "Select Clothes")
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
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Bill Picture Placeholder",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            modifier = Modifier.fillMaxWidth()
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
//                laundryViewModel.createLaundry(
//                    laundererId = laundererId.toInt(),
//                    clothesIds = selectedClothes.map { it!!.id }.toIntArray(),
//                    laundryDays = laundryDays.value.toInt(),
//                    billPic = Bitmap(null),
//                )
                navController.popBackStack()
            }
        ) {
            Text(text = "Submit")
        }
    }
}