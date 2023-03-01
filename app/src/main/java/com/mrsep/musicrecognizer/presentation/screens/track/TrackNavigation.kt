package com.mrsep.musicrecognizer.presentation.screens.track

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink

const val URI = "https://www.mrsep.com/track"
private const val ARG_STRING_MB_ID = "mbId"
const val TRACK_ROUTE = "track"

fun NavGraphBuilder.trackScreen(onBackPressed: () -> Unit) {
    composable(
        route = "$TRACK_ROUTE/{$ARG_STRING_MB_ID}",
        deepLinks = listOf(navDeepLink { uriPattern = "$URI/{$ARG_STRING_MB_ID}" })
    ) {
        TrackScreen(onBackClick = onBackPressed)
    }
}

fun NavController.navigateToTrackScreen(
    mbId: String,
    navOptions: NavOptions? = null
) {
    this.navigate(route = "$TRACK_ROUTE/$mbId", navOptions = navOptions)
}

data class TrackScreenArguments(
    val mbId: String
) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        mbId = checkNotNull(savedStateHandle[ARG_STRING_MB_ID])
    )
}

//sealed class Screen(val route: String) {
//
//    object Track: Screen("track/{mbId}") {
//        fun routeWithArgs(mbId: String): String {
//            return "track/$mbId"
//        }
//        fun deepLinkWithArgs(mbId: String): String {
//            return "https://www.mrsep.com/track/$mbId"
//        }
//        data class Args(val mbId: String) {
//            constructor(savedStateHandle: SavedStateHandle) : this(
//                mbId = checkNotNull(savedStateHandle["mbId"])
//            )
//        }
//    }
//
//}
