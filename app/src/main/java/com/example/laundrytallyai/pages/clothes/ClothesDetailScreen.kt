package com.example.laundrytallyai.pages.clothes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.utils.dateFormatterDMY
import com.example.laundrytallyai.utils.dateFormatterDMYHms

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClothesDetailScreen(
    viewModel: ClothesViewModel,
    navController: NavController,
    paddingValues: PaddingValues? = null
) {
    val clothesData by viewModel.selectedClothes.collectAsState()

    clothesData?.let { data ->
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth(),
                    model = BASE_URL + data.cloth_pic,
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
                    title = "${data.type} - ${data.color}",
                    onBackClick = { navController.popBackStack() }
                )
            }

            Column (modifier = Modifier.padding(16.dp)){
                Text(
                    text = "Inputted at: ${dateFormatterDMYHms(data.inputted_at)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    data.desc,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}