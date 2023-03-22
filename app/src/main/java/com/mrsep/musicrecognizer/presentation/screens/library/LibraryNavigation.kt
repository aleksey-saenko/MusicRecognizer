package com.mrsep.musicrecognizer.presentation.screens.library

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

private const val ROOT_ROUTE = "library"

fun NavGraphBuilder.libraryScreen(
    onTrackClick: (mbId: String) -> Unit
) {
    composable(ROOT_ROUTE) {
        LibraryScreen(onTrackClick = onTrackClick)
    }
}

fun NavController.navigateToLibraryScreen(
    navOptions: NavOptions? = null
) {
    this.navigate(route = ROOT_ROUTE, navOptions = navOptions)
}