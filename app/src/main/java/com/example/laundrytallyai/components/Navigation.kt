package com.example.laundrytallyai.components

import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Left
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Right
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.laundrytallyai.navigation.Screen

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<Screen>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                label = {
                    Text(item.route.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    })
                },
                selected = index == selectedItem,
                icon = { Icon(item.icon(), contentDescription = item.route) },
                onClick = {
                    onItemSelected(index)
                    Log.d("BottomNavigationBar", "onClick: $index")
                    navController.navigate(item.route)
                },
                alwaysShowLabel = false
            )
        }
    }
}

fun NavGraphBuilder.animatedComposable(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            val targetIndex = getIndexOfRoute(targetState.destination.route)
            val initialIndex = getIndexOfRoute(initialState.destination.route)
            if (targetIndex > initialIndex) {
                slideIntoContainer(Left, animationSpec = tween(300))
            } else {
                slideIntoContainer(Right, animationSpec = tween(300))
            }
        },
        exitTransition = {
            val targetIndex = getIndexOfRoute(targetState.destination.route)
            val initialIndex = getIndexOfRoute(initialState.destination.route)
            if (targetIndex > initialIndex) {
                slideOutOfContainer(Left, animationSpec = tween(300))
            } else {
                slideOutOfContainer(Right, animationSpec = tween(300))
            }
        },
        popEnterTransition = {
            val targetIndex = getIndexOfRoute(targetState.destination.route)
            val initialIndex = getIndexOfRoute(initialState.destination.route)
            if (targetIndex > initialIndex) {
                slideIntoContainer(Left, animationSpec = tween(300))
            } else {
                slideIntoContainer(Right, animationSpec = tween(300))
            }
        },
        popExitTransition = {
            val targetIndex = getIndexOfRoute(targetState.destination.route)
            val initialIndex = getIndexOfRoute(initialState.destination.route)
            if (targetIndex > initialIndex) {
                slideOutOfContainer(Left, animationSpec = tween(300))
            } else {
                slideOutOfContainer(Right, animationSpec = tween(300))
            }
        }
    ) {
        content(it)
    }
}

fun getIndexOfRoute(route: String?): Int {
    return when (route) {
        Screen.Home.route -> 0
        Screen.Clothes.route -> 1
        Screen.Launderer.route -> 2
        Screen.Laundry.route -> 3
        Screen.Settings.route -> 4
        else -> -1
    }
}

//@Preview(showBackground = true)
//@Composable
//fun BottomNavigationBarPreview() {
//    val navController = rememberNavController()
//    BottomNavigationBar(navController)
//}