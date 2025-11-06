package com.mailflow.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mailflow.presentation.screens.chat.ChatScreen
import com.mailflow.presentation.screens.createagent.CreateAgentScreen
import com.mailflow.presentation.screens.dashboard.DashboardScreen
import com.mailflow.presentation.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onAgentClick = { agentId ->
                    navController.navigate(Screen.Chat.createRoute(agentId))
                },
                onCreateAgentClick = {
                    navController.navigate(Screen.CreateAgent.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.CreateAgent.route) {
            CreateAgentScreen(
                onBackClick = { navController.navigateUp() },
                onAgentCreated = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.AgentDetail.route,
            arguments = listOf(
                navArgument("agentId") { type = NavType.StringType }
            )
        ) {
            // TODO: AgentDetailScreen
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("agentId") { type = NavType.StringType }
            )
        ) {
            ChatScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}
