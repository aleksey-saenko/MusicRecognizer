package com.mrsep.musicrecognizer.presentation.screens.track

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.mrsep.musicrecognizer.presentation.screens.preferences.PREFERENCES_ROUTE
import com.mrsep.musicrecognizer.presentation.screens.preferences.PreferencesScreen

//const val URI = "https://www.mrsep.musicrecognizer.com/track"
//private const val ARG_STRING_MB_ID = "mbId"
//const val TRACK_ROUTE = "track"
//
//fun NavGraphBuilder.trackScreen(onBackPressed: () -> Unit) {
//    composable(
//        route = "$TRACK_ROUTE/{$ARG_STRING_MB_ID}",
//        deepLinks = listOf(navDeepLink { uriPattern = "$URI/{$ARG_STRING_MB_ID}" })
//    ) {
//        TrackScreen(onBackPressed = onBackPressed)
//    }
//}
//
//fun NavController.navigateToTrackScreen(
//    mbId: String,
//    navOptions: NavOptions? = null
//) {
//    this.navigate(route = "$TRACK_ROUTE/$mbId", navOptions = navOptions)
//}
//
//fun createTrackScreenDeepLink(mbId: String) = "$URI/$mbId"
//
//data class TrackScreenArguments(
//    val mbId: String
//) {
//    constructor(savedStateHandle: SavedStateHandle) : this(
//        mbId = checkNotNull(savedStateHandle[ARG_STRING_MB_ID])
//    )
//}

sealed class Screen(val route: String) {

    object Track : Screen("track/{mbId}") {

        data class Args(val mbId: String) {
            constructor(savedStateHandle: SavedStateHandle) : this(
                mbId = checkNotNull(savedStateHandle["mbId"])
            )
        }

        fun NavGraphBuilder.trackScreen(onBackPressed: () -> Unit) {
            composable(
                route = "track/{mbId}",
                deepLinks = listOf(navDeepLink {
                    uriPattern = "https://www.mrsep.musicrecognizer.com/track/{mbId}"
                })
            ) {
                TrackScreen(onBackPressed = onBackPressed)
            }
        }

        fun NavController.navigateToTrackScreen(
            mbId: String,
            navOptions: NavOptions? = null
        ) {
            this.navigate(route = "track/$mbId", navOptions = navOptions)
        }

        fun createDeepLink(mbId: String): String {
            return "https://www.mrsep.musicrecognizer.com/track/$mbId"
        }
    }


    object Preferences : Screen("preferences") {
        fun NavGraphBuilder.preferencesScreen(navController: NavController) {
            composable(route = this@Preferences.route) {
                PreferencesScreen(navController = navController)
            }
        }

        fun NavController.navigateToPreferencesScreen(
            navOptions: NavOptions? = null
        ) {
            this.navigate(route = this@Preferences.route, navOptions = navOptions)
        }
    }



}