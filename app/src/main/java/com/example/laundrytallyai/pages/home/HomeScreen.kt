package com.example.laundrytallyai.pages.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.datastates.HomeDataState
import com.example.laundrytallyai.components.BackgroundedItem
import com.example.laundrytallyai.components.DetachedItem
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation
import com.example.laundrytallyai.utils.dateFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController, paddingValues: PaddingValues? = null) {
    val viewModel: HomeViewModel = hiltViewModel()
    val dataState by viewModel.dataState.collectAsState()

    if (viewModel.getToken() == null) {
        navController.navigate("login")
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    when (val state = dataState) {
        is HomeDataState.Loading -> RotatingArcLoadingAnimation()
        is HomeDataState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Hi, ",
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize
                        )
                        Text(
                            text = state.data.user + "!",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        )
                    }
                    PageTitle(title = "Recent Activities")
                    HorizontalListSection(
                        title = "Clothes",
                        items = state.data.clothes,
                        content = { clothes ->
                            DetachedItem(
                                imageUrl = BASE_URL + clothes.cloth_pic,
                                primaryText = clothes.type,
                                secondaryText = clothes.color
                            )
                        }
                    )
                    HorizontalListSection(
                        title = "Launderers",
                        items = state.data.launderers,
                        content = { launderer ->
                            DetachedItem(
                                imageUrl = BASE_URL + launderer.launderer_pic,
                                primaryText = launderer.name.replace("laundry", "", true),
                                secondaryText = launderer.address.split(" ").first()
                                    .replace(",", "")
                            )
                        }
                    )
                    HorizontalListSection(
                        title = "Laundries",
                        items = state.data.laundries
                    ) {
                        BackgroundedItem(
                            imageUrl = BASE_URL + it.launderer_pic,
                            primaryText = it.launderer_name.replace("laundry", "", true),
                            secondaryText = dateFormatter(it.laundered_at)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        is HomeDataState.Error -> {
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

@Composable
fun <T> HorizontalListSection(
    title: String,
    items: List<T>,
    content: @Composable (T) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Icon(Icons.Filled.ArrowForward, contentDescription = "See Detail")
    }
    LazyRow {
        items(items) { item ->
            content(item)
        }
    }
}