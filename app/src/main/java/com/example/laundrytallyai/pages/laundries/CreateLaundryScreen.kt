package com.example.laundrytallyai.pages.laundries

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun CreateLaundryScreen(navController: NavController, laundererId: String) {
    
    Text(text = laundererId)

}