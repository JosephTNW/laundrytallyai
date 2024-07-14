package com.example.laundrytallyai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.laundrytallyai.R

sealed class Screen(val route: String, val icon: @Composable (() -> ImageVector)) {
    object Login : Screen("login", {Icons.Filled.Lock})
    object Register : Screen("register", {Icons.Filled.AddCircle})
    object Home : Screen("home", {Icons.Filled.Home})
    object Clothes : Screen("clothes", {ImageVector.vectorResource(R.drawable.t_shirt)})
    object ClothesDetail : Screen("clothesDetail", {ImageVector.vectorResource(R.drawable.t_shirt)})
    object ClothesEdit : Screen("clothesEdit", {ImageVector.vectorResource(R.drawable.t_shirt)})
    object Launderer : Screen("launderer", {ImageVector.vectorResource(R.drawable.baseline_local_laundry_service_24)})
    object LaundererDetail: Screen("laundererDetail", {ImageVector.vectorResource(R.drawable.baseline_local_laundry_service_24)})
    object Laundry : Screen("laundry", {ImageVector.vectorResource(R.drawable.laundry_basket_24)})
    object LaundryDetail : Screen("laundryDetail", {ImageVector.vectorResource(R.drawable.laundry_basket_24)})
    object LaundryValidation : Screen("laundryValidation", {ImageVector.vectorResource(R.drawable.laundry_basket_24)})
    object Settings : Screen("settings", {Icons.Filled.Settings})
    object SelectMedia : Screen("select-media", {Icons.Filled.AddCircle})
    object CameraPreview : Screen("camera-preview", {Icons.Filled.Face})
    object CreateLaundry : Screen("create-laundry/{launderer_id}/{launderer_name}", {ImageVector.vectorResource(id = R.drawable.laundry_basket_24)})

}
