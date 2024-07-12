package com.example.laundrytallyai.pages.laundries

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.R
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.dataschemes.LaundryData
import com.example.laundrytallyai.api.datastates.LaundryDataState
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.ui.theme.Purple40
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation
import com.example.laundrytallyai.utils.dateFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaundryScreen(navController: NavController, paddingValues: PaddingValues? = null) {
    val viewModel: LaundryViewModel = hiltViewModel()

    val dataState by viewModel.dataState.collectAsState()

    if (viewModel.getToken() == null) {
        navController.navigate("login")
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        PageTitle(title = "Laundry History")

        when (val state = dataState) {
            is LaundryDataState.Loading -> RotatingArcLoadingAnimation()
            is LaundryDataState.Success -> {
                LaundrySuccessScreen(laundries = state.data)
            }

            is LaundryDataState.Error -> {
                if (state.code == "401" || state.code == "403") {
                    viewModel.deleteToken()
                    navController.navigate("login")
                } else {
                    Text(text = "Error: ${state.error}, Code: ${state.code}")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaundrySuccessScreen(laundries: List<LaundryData>) {
    LazyColumn {
        items(laundries) { item ->
            LaundererItemCard(item)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaundererItemCard(laundryData: LaundryData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Left side with icon and name
            AsyncImage(
                model = BASE_URL + laundryData.launderer.launderer_pic,
                contentDescription = "Launderer Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(120.dp)
                    .width(80.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = laundryData.launderer.name.replace(
                            "laundry",
                            "",
                            ignoreCase = true
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatter(laundryData.laundered_at),
                        style = MaterialTheme.typography.bodySmall
                    )
                }


                LazyRow {
                    val visibleItems = laundryData.clothes.take(3)
                    val remainingItems = laundryData.clothes.size - 3

                    items(visibleItems) { item ->
                        AsyncImage(
                            modifier = Modifier.size(40.dp),
                            model = BASE_URL + item.cloth_pic,
                            contentDescription = "clothing image"
                        )
                    }

                    if (remainingItems > 0) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+$remainingItems",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 8.dp, top = 16.dp, bottom = 13.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Box(
                    modifier = Modifier
                        .background(Color.LightGray, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = laundryData.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }


                Button(
                    modifier = Modifier,
                    onClick = { /* Handle validation */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple40)
                ) {
                    Text(
                        text = "Validate",
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    )
                }
            }
        }
    }
}