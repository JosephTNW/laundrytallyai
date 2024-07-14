package com.example.laundrytallyai.pages.launderers

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaundererDetailScreen(viewModel: LaundererViewModel, navController: NavController) {
    val laundererData by viewModel.selectedLaunderer.collectAsState()

    laundererData?.let { data ->

        // Use the full laundererData object here
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    model = BASE_URL + data.launderer_pic,
                    contentDescription = "Launderer Picture",
                    contentScale = ContentScale.FillWidth
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
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
                    title = data.name,
                    onBackClick = { navController.popBackStack() },
                    rightLabelText = "Laundry",
                    onRightLabelClick = { navController.navigate("create-laundry/${data.id}/${data.name}") })
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Launderer Profile", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    data.desc,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(section = "Address", value = data.address)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (data.has_delivery) "Has Delivery" else "No Delivery",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(section = "Phone", value = data.phone_num)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (data.has_whatsapp) "Has WhatsApp" else "No WhatsApp",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Inputted Since: ${dateFormatterDMY(data.inputted_at)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun InfoRow(section: String, value: String) {
    Row {
        Text(
            "$section -",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}