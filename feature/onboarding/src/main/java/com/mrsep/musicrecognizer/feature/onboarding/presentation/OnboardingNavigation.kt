package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object OnboardingScreen {

    const val ROUTE = "onboarding"

    fun NavGraphBuilder.onboardingScreen(
        onOnboardingCompleted: () -> Unit,
        onOnboardingClose: () -> Unit
    ) {
        composable(ROUTE) {
            OnboardingScreen(
                onOnboardingCompleted = onOnboardingCompleted,
                onOnboardingClose = onOnboardingClose,
            )
        }
    }

    fun NavController.navigateToOnboardingScreen(
        navOptions: NavOptions? = null
    ) {
        this.navigate(route = ROUTE, navOptions = navOptions)
    }

}

