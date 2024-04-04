package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object OnboardingScreen {

    const val ROUTE = "onboarding"

    fun NavGraphBuilder.onboardingScreen(onOnboardingClose: () -> Unit) {
        composable(ROUTE) {
            OnboardingScreen(onOnboardingClose = onOnboardingClose)
        }
    }

    fun NavController.navigateToOnboardingScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }

}

