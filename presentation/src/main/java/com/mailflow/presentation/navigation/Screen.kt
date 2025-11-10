package com.mailflow.presentation.navigation

sealed class Screen(val route: String) {
    data object EmailManagement : Screen("email_management")
    data object ActivityLog : Screen("activity_log")
    data object Settings : Screen("settings")
}

