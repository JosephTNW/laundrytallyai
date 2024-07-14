package com.example.laundrytallyai.pages.laundries

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.pages.clothes.ClothesViewModel
import com.example.laundrytallyai.pages.launderers.LaundererViewModel
import com.example.laundrytallyai.utils.dateFormatterDMYHms

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaundryDetailScreen(
    viewModel: LaundryViewModel,
    clothesViewModel: ClothesViewModel,
    laundererViewModel: LaundererViewModel,
    navController: NavController
) {
    val laundryData by viewModel.selectedLaundry.collectAsState()

    laundryData?.let { data ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth(),
                    model = BASE_URL + data.launderer.launderer_pic,
                    contentDescription = "Launderer Picture",
                    contentScale = ContentScale.FillWidth
                )
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.7f),
                                    Color.White.copy(alpha = 0.0f)
                                )
                            )
                        )
                )
                PageTitle(
                    fontSize = 22.sp,
                    title = dateFormatterDMYHms(data.laundered_at),
                    onBackClick = { navController.popBackStack() },
                    rightLabelText = data.status.uppercase()
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = "Clothes Laundered",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                LazyRow {
                    items(data.clothes) { item ->
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .size(100.dp)
                            .padding(start = 16.dp, top = 8.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                clothesViewModel.setSelectedClothes(item)
                                navController.navigate(Screen.ClothesDetail.route)
                            }
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp)),
                                model = BASE_URL + item.cloth_pic,
                                contentDescription = "Clothes Picture",
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Text(
                    text = "Laundry Duration",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
                Text(
                    text = "${data.laundry_days} Days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                Row {

                    Text(
                        text = "Laundered by ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                    )

                    Text(
                        text = data.launderer.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                            .clickable {
                                laundererViewModel.setSelectedLaunderer(data.launderer)
                                navController.navigate(Screen.LaundererDetail.route)
                            }
                    )
                }
            }
        }
    }
}