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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.utils.dateFormatter

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
                        .height(200.dp),
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
                    onRightLabelClick = { navController.navigate("create-laundry/${data.id}") })
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                Text("Launderer Profile", style = MaterialTheme.typography.headlineSmall)
                Text(
                    data.desc,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(section = "Address", value = data.address)
                Text(
                    text = if (data.has_delivery) "Has Delivery" else "No Delivery"
                )
                InfoRow(section = "Phone", value = data.phone_num)
                Text(
                    text = if (data.has_whatsapp) "Has WhatsApp" else "No WhatsApp"
                )
                Text(text = "Inputted Since: ${dateFormatter(data.inputted_at)}")
            }
        }
    }
}

@Composable
fun InfoRow(section: String, value: String) {
    Column {
        Text("$section -", style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}