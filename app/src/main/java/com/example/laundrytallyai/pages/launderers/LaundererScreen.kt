package com.example.laundrytallyai.pages.launderers

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.dataschemes.LaundererData
import com.example.laundrytallyai.api.datastates.LaundererDataState
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.components.SearchBar
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun LaundererScreen(navController: NavController, paddingValues: PaddingValues? = null) {
    val viewModel: LaundererViewModel = hiltViewModel()
    val dataState by viewModel.dataState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    if (viewModel.getToken() == null) {
        navController.navigate("login")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        PageTitle(title = "Explore Launderers")
        SearchBar(
            searchText = searchText,
            onSearchTextChange = { searchText = it }
        )

        LaunchedEffect(searchText) {
            searchJob?.cancel()
            searchJob = coroutineScope.launch {
                if (isActive) {
                    viewModel.fetchData(searchText)
                }
            }
        }

        when (val state = dataState) {
            is LaundererDataState.Loading -> RotatingArcLoadingAnimation()
            is LaundererDataState.Success -> {
                LaundererGrid(laundererItems = state.data)
            }

            is LaundererDataState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}

@Composable
fun LaundererGrid(laundererItems: List<LaundererData>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(laundererItems) {laundererData ->
            LaundererCard(
                imageUrl = BASE_URL + laundererData.launderer_pic,
                name = laundererData.name,
                address = laundererData.address.split(" ").take(2).joinToString(" "),
                has_deliv = laundererData.has_delivery,
                has_whatsapp = laundererData.has_whatsapp
                )
        }
        items(5) {
            LaundererCard(
                imageUrl = BASE_URL + "/launderers/launderer1.jpg",
                name = "Launderer Name",
                address = "Address",
                has_deliv = true,
                has_whatsapp = false
            )
        }
    }
}

@Composable
fun LaundererCard(
    imageUrl: String,
    name: String,
    address: String,
    has_deliv: Boolean,
    has_whatsapp: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Make it square
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(2f)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Log.d("ClothesItem", imageUrl)
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Clothes Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Cross",
                        tint = if (has_whatsapp) Color.Green else Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Cross",
                        tint = if (has_deliv) Color.Green else Color.Red
                    )
                }
            }
        }
    }
}