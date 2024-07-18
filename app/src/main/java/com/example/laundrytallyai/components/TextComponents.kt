package com.example.laundrytallyai.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun PageTitle(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = MaterialTheme.typography.headlineSmall.fontSize,
    title: String,
    onBackClick: (() -> Unit)? = null,
    rightLabelText: String? = null,
    onRightLabelClick: (() -> Unit)? = null,
    onMiddleButtonClick: (() -> Unit)? = null,
    middleButtonIcon: ImageVector? = null,
) {
//    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = { onBackClick() },
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
        )

        if (middleButtonIcon != null && onMiddleButtonClick != null) {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = { onMiddleButtonClick() },
                Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .size(28.dp),
            ) {
                Icon(
                    modifier = Modifier.size(14.dp),
                    imageVector = middleButtonIcon,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        if (onRightLabelClick != null && rightLabelText != null) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(30.dp),
                onClick = { onRightLabelClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    text = rightLabelText,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                )
            }
        } else if (rightLabelText != null) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(Color.LightGray, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = rightLabelText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SectionTitle(text: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                if (onClick != null) {
                    onClick()
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)

        if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.Create,
                contentDescription = "Edit Clothes"
            )
        }
    }
}