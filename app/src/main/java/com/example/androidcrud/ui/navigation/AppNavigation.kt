package com.example.androidcrud.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidcrud.ui.screens.add.AddEntryScreen
import com.example.androidcrud.ui.screens.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeDestination
    ) {
        composable<HomeDestination> {
            HomeScreen(
                onAddEntryClick = {
                    navController.navigate(AddEntryDestination(entryId = null))
                },
                onEditEntryClick = { entryId ->
                    navController.navigate(AddEntryDestination(entryId = entryId))
                }
            )
        }
        composable<AddEntryDestination> {
            AddEntryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
