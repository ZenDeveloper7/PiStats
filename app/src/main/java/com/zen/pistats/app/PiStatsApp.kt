package com.zen.pistats.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zen.pistats.dashboard.presentation.DashboardRoot
import com.zen.pistats.settings.presentation.SettingsRoot
import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data object SettingsRoute

@Composable
fun PiStatsApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
    ) {
        composable<DashboardRoute> {
            DashboardRoot(
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<SettingsRoute> {
            SettingsRoot(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
