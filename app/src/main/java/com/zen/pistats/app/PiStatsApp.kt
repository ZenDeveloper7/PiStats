package com.zen.pistats.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 280),
            ) + fadeIn(animationSpec = tween(durationMillis = 180))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 280),
            ) + fadeOut(animationSpec = tween(durationMillis = 180))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(durationMillis = 280),
            ) + fadeIn(animationSpec = tween(durationMillis = 180))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(durationMillis = 280),
            ) + fadeOut(animationSpec = tween(durationMillis = 180))
        },
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
