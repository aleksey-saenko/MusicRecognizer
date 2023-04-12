package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object AboutScreenNavigation {

    private const val ROUTE = "about"

    fun NavGraphBuilder.aboutScreen(onBackPressed: () -> Unit) {
        composable(ROUTE) {
            AboutScreen(onBackPressed = onBackPressed)
        }
    }

    fun NavController.navigateToAboutScreen(
        navOptions: NavOptions? = null
    ) {
        this.navigate(route = ROUTE, navOptions = navOptions)
    }

}

