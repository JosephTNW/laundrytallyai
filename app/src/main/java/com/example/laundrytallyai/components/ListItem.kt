package com.example.laundrytallyai.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.laundrytallyai.api.dataschemes.LaundererData

@Composable
fun BackgroundedItem(
    modifier: Modifier = Modifier,
    imageUrl: String,
    primaryText: String,
    secondaryText: String,
    onDelete: (() -> Unit)? = null,
    onUpdate: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .width(150.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .padding(8.dp)
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
            Column {
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = primaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = secondaryText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (onDelete != null && onUpdate != null) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        IconButton(
                            onClick = onDelete,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Clothes",
                                tint = MaterialTheme.colorScheme.error
                                )
                        }
                        IconButton(
                            onClick = onUpdate,
                            ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Clothes",
                                tint = MaterialTheme.colorScheme.primary
                                )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

        }
    }
}

@Composable
fun DetachedItem(imageUrl: String, primaryText: String, secondaryText: String) {
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp),
            ) {
                Log.d("ClothesItem", imageUrl)
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Clothes Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = primaryText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = secondaryText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}