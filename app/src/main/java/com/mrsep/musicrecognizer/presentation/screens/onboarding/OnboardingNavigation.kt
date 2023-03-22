package com.mrsep.musicrecognizer.presentation.screens.onboarding

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val ONBOARDING_ROUTE = "onboarding"

fun NavGraphBuilder.onboardingScreen(
    onOnboardingCompleted: () -> Unit,
    onOnboardingClose: () -> Unit
) {
    composable(ONBOARDING_ROUTE) {
        OnboardingScreen(
            onOnboardingCompleted = onOnboardingCompleted,
            onOnboardingClose = onOnboardingClose,
        )
    }
}

fun NavController.navigateToOnboardingScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = ONBOARDING_ROUTE, navOptions = navOptions)
}