package com.mrsep.musicrecognizer.presentation.screens.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

private const val ROOT_ROUTE = "onboarding"

fun NavGraphBuilder.onboardingScreen(
    onSignUpClick: (link: String) -> Unit,
    onApplyTokenClick: () -> Unit
) {
    composable(ROOT_ROUTE) {
        OnboardingScreen(
            onSignUpClick = onSignUpClick,
            onApplyTokenClick = onApplyTokenClick
        )
    }
}

fun NavController.navigateToOnboardingScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = ROOT_ROUTE, navOptions = navOptions)
}