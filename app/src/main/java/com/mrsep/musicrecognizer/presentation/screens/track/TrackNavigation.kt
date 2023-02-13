package com.mrsep.musicrecognizer.presentation.screens.track

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

private const val ARG_STRING_MB_ID = "mbId"
private const val ROOT_ROUTE = "track"

fun NavGraphBuilder.trackScreen(onBackPressed: () -> Unit) {
    composable("$ROOT_ROUTE/{$ARG_STRING_MB_ID}") {
        TrackScreen(onBackPressed = onBackPressed)
    }
}

fun NavController.navigateToTrackScreen(
    mbId: String,
    navOptions: NavOptions? = null
) {
    this.navigate(route = "$ROOT_ROUTE/$mbId", navOptions = navOptions)
}

data class TrackScreenArguments(
    val mbId: String
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        mbId = checkNotNull(savedStateHandle[ARG_STRING_MB_ID])
    )
}
