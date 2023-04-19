package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object DeveloperScreenNavigation {

    private const val ROUTE = "developer"

    fun NavGraphBuilder.developerScreen(
        onBackPressed: () -> Unit,
    ) {
        composable(ROUTE) {
            DeveloperScreen(onBackPressed = onBackPressed)
        }
    }

    fun NavController.navigateToDeveloperScreen(
        navOptions: NavOptions? = null
    ) {
        this.navigate(route = ROUTE, navOptions = navOptions)
    }

}