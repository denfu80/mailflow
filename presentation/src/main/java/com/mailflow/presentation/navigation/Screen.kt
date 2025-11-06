package com.mailflow.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object CreateAgent : Screen("create_agent")
    data object AgentDetail : Screen("agent_detail/{agentId}") {
        fun createRoute(agentId: String) = "agent_detail/$agentId"
    }
    data object Chat : Screen("chat/{agentId}") {
        fun createRoute(agentId: String) = "chat/$agentId"
    }
    data object Settings : Screen("settings")
}
