package com.example.laundrytallyai.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController, paddingValues: PaddingValues ?= null) {

    val viewModel: SettingsViewModel = hiltViewModel()

    if (viewModel.getToken() == null) {
        navController.navigate("login")
    }

    val settingsItems = listOf(
        SettingsItemData.Toggle(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = "Enable push notifications",
            isChecked = true,
            onClick = { /* Handle click */ },
            onToggle = { isChecked -> /* Handle toggle */ }
        ),
        SettingsItemData.Action(
            icon = Icons.Default.Lock,
            title = "Privacy",
            subtitle = "Manage your privacy settings",
            onClick = { /* Open privacy settings */ }
        ),
        SettingsItemData.Action(
            icon = Icons.Default.ExitToApp,
            title = "Logout",
            subtitle = "Log out of your account",
            onClick = {
                viewModel.deleteToken()
                navController.navigate("login")
            }
        ),
    )

    LazyColumn {
        items(settingsItems) { item ->
            SettingsItem(item)
        }
    }
}

@Composable
fun SettingsItem(item: SettingsItemData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }  // Add clickable modifier
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {  // Add weight to push the toggle to the end
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium
            )
            if (item.subtitle != null) {
                Text(
                    text = item.subtitle!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        // Add a conditional composable based on the item type
        when (item) {
            is SettingsItemData.Toggle -> {
                Switch(
                    checked = item.isChecked,
                    onCheckedChange = { item.onToggle(it) }
                )
            }
            is SettingsItemData.Action -> {
//                Icon(
//                    imageVector = Icons.Default.ChevronRight,
//                    contentDescription = "Open",
//                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
//                )
            }
        }
    }
}

sealed class SettingsItemData {
    abstract val icon: ImageVector
    abstract val title: String
    abstract val subtitle: String?
    abstract val onClick: () -> Unit

    data class Toggle(
        override val icon: ImageVector,
        override val title: String,
        override val subtitle: String? = null,
        override val onClick: () -> Unit,
        val isChecked: Boolean,
        val onToggle: (Boolean) -> Unit
    ) : SettingsItemData()

    data class Action(
        override val icon: ImageVector,
        override val title: String,
        override val subtitle: String? = null,
        override val onClick: () -> Unit
    ) : SettingsItemData()
}