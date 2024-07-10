package com.example.laundrytallyai.pages.home

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.laundrytallyai.components.BottomNavigationBar
import com.example.laundrytallyai.components.animatedComposable
import com.example.laundrytallyai.navigation.Screen
import com.example.laundrytallyai.pages.auth.LoginScreen
import com.example.laundrytallyai.pages.auth.RegisterScreen
import com.example.laundrytallyai.pages.clothes.CameraServerScreen
import com.example.laundrytallyai.pages.clothes.CameraXScreen
import com.example.laundrytallyai.pages.clothes.ClothesScreen
import com.example.laundrytallyai.pages.clothes.SelectMediaScreen
import com.example.laundrytallyai.pages.launderers.LaundererScreen
import com.example.laundrytallyai.pages.laundries.LaundryScreen
import com.example.laundrytallyai.pages.settings.SettingsScreen
import com.example.laundrytallyai.ui.theme.LaundryTallyAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LaundryTallyAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navItems = listOf(
                        Screen.Home,
                        Screen.Clothes,
                        Screen.Launderer,
                        Screen.Laundry,
                        Screen.Settings
                    )
                    var selectedItem by remember { mutableStateOf(0) }
                    var showBottomBar by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    val cameraController = remember { LifecycleCameraController(context) }
                    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                BottomNavigationBar(
                                    navController,
                                    navItems,
                                    selectedItem,
                                    onItemSelected = { selectedItem = it },
                                )
                            }
                        }
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Launderer.route,
                            modifier = Modifier.padding(it)
                        ) {
                            composable(Screen.Login.route) {
                                LoginScreen(navController)
                                showBottomBar = false
                            }
                            composable(Screen.Register.route) {
                                RegisterScreen(navController)
                                showBottomBar = false
                            }

                            animatedComposable(Screen.Home.route) {
                                HomeScreen(navController)
                                showBottomBar = true
                            }
                            animatedComposable(Screen.Clothes.route) {
                                ClothesScreen(navController)
                                showBottomBar = true
                            }
                            animatedComposable(Screen.Laundry.route) {
                                LaundryScreen(navController)
                                showBottomBar = true
                            }
                            animatedComposable(Screen.Launderer.route) {
                                LaundererScreen(navController)
                                showBottomBar = true
                            }
                            animatedComposable(Screen.Settings.route) {
                                SettingsScreen(navController)
                                showBottomBar = true
                            }

                            composable(Screen.SelectMedia.route) {
                                SelectMediaScreen(navController)
                                showBottomBar = false
                            }

                            composable(Screen.CameraPreview.route) {
                                CameraServerScreen(navController)
                                showBottomBar = false
                            }
                        }
                    }
                }
            }
        }
    }
}