package com.example.laundrytallyai.pages.laundries

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.laundrytallyai.api.RetrofitClient.BASE_URL
import com.example.laundrytallyai.api.dataschemes.LaundryData
import com.example.laundrytallyai.api.datastates.LaundryDataState
import com.example.laundrytallyai.components.PageTitle
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.utils.RotatingArcLoadingAnimation
import com.example.laundrytallyai.utils.addDaysToFormattedDate
import com.example.laundrytallyai.utils.dateFormatterDMY

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun LaundryScreen(
    viewModel: LaundryViewModel,
    navController: NavController,
    paddingValues: PaddingValues? = null
) {
    val dataState by viewModel.dataState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedLaundry by remember { mutableStateOf<LaundryData?>(null) }
    var generatedText by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (viewModel.getToken() == null) {
        navController.navigate("login")
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            PageTitle(title = "Laundry History")

            when (val state = dataState) {
                is LaundryDataState.Loading -> RotatingArcLoadingAnimation()
                is LaundryDataState.Success -> {
                    LazyColumn {
                        items(state.data) { item ->
                            LaundryItemCard(
                                modifier = Modifier.clickable {
                                    viewModel.setSelectedLaundry(item)
                                    navController.navigate(Screen.LaundryDetail.route)
                                },
                                laundryData = item,
                                onValidateButtonClick = {
                                    viewModel.setSelectedLaundry(item)
                                    navController.navigate(Screen.LaundryValidation.route)
                                },
                                onSendButtonClick = {
                                    selectedLaundry = item
                                    showDialog = true
                                }
                            )
                        }
                    }
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

        if (showDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = {
                    Text(text = "Message Generated!")
                },
                text = {
                    generatedText =
                        "Hello ${selectedLaundry?.launderer?.name}, I would like to make " +
                                "a request for my ${selectedLaundry?.clothes?.size} number of clothes " +
                                "laundered at ${
                                    selectedLaundry?.laundered_at?.let {
                                        dateFormatterDMY(
                                            it
                                        )
                                    }
                                }" +
                                " with a ${selectedLaundry?.laundry_days} laundry period conclusively " +
                                "ending on ${
                                    selectedLaundry?.laundered_at?.let { laundered_at ->
                                        dateFormatterDMY(laundered_at)
                                    }
                                        ?.let {
                                            selectedLaundry?.laundry_days?.let { days ->
                                                addDaysToFormattedDate(
                                                    it,
                                                    days
                                                )
                                            }
                                        }
                                }. I'm doing this on behalf of ${viewModel.getUsername()}. Thank You"
                    Text(text = generatedText)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied Text", generatedText)
                            clipboard.setPrimaryClip(clip)

                            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT)
                                .show()

                            showDialog = false
                        }
                    ) {
                        Text("Copy")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"  // Change the MIME type to text/plain
                                putExtra(Intent.EXTRA_TEXT, generatedText)
                            }

                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Share message using"
                                )
                            )
                        }
                    ) {
                        Text("Share")
                    }
                }
            )
        }

    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LaundryItemCard(
    modifier: Modifier,
    laundryData: LaundryData,
    onSendButtonClick: () -> Unit,
    onValidateButtonClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                    ) {
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
                            text = dateFormatterDMY(laundryData.laundered_at),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row{
                        if (laundryData.status == "pending") {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .padding(6.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = rememberRipple(
                                            bounded = false,
                                            radius = 14.dp
                                        ),
                                        onClick = onSendButtonClick
                                    )
                                    .clip(CircleShape)
                            ) {
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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
                    }

                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

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
                    if (laundryData.status != "finish") {
                        Button(
                            modifier = Modifier,
                            onClick = onValidateButtonClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
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
    }
}