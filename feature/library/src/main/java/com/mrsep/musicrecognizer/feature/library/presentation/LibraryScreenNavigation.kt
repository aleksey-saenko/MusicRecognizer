package com.mrsep.musicrecognizer.feature.library.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object LibraryScreen {

    const val ROUTE = "library"

    fun NavGraphBuilder.libraryScreen(
        onTrackClick: (mbId: String) -> Unit
    ) {
        composable(ROUTE) {
            LibraryScreen(onTrackClick = onTrackClick)
        }
    }

    fun NavController.navigateToLibraryScreen(
        navOptions: NavOptions? = null
    ) {
        this.navigate(route = ROUTE, navOptions = navOptions)
    }

}

