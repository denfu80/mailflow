package com.mailflow.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mailflow.presentation.screens.dashboard.ActivityLogScreen
import com.mailflow.presentation.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ActivityLog.route,
        modifier = modifier
    ) {
        composable(Screen.ActivityLog.route) {
            ActivityLogScreen(
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}
