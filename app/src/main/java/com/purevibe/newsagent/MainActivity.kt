package com.purevibe.newsagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.purevibe.newsagent.ui.AgentReportScreen
import com.purevibe.newsagent.ui.HomeScreen
import com.purevibe.newsagent.ui.SettingsScreen
import com.purevibe.newsagent.ui.theme.NewsAgentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAgentTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onAgentClick = { agentId -> nav.navigate("report/$agentId") },
                            onSettingsClick = { nav.navigate("settings") }
                        )
                    }
                    composable("report/{agentId}") { entry ->
                        AgentReportScreen(
                            agentId = entry.arguments?.getString("agentId"),
                            onBack = { nav.popBackStack() },
                            onSettings = { nav.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(onBack = { nav.popBackStack() })
                    }
                }
            }
        }
    }
}
