package com.mrsep.musicrecognizer.presentation.navigationbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.feature.library.presentation.LibraryScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.RecognitionScreen

enum class NavBarDestination(
    val route: String,
    @StringRes val titleId: Int,
    @DrawableRes val selectedIconId: Int,
    @DrawableRes val unselectedIconId: Int
) {
    Recently(
        route = LibraryScreen.ROUTE,
        titleId = StringsR.string.library,
        selectedIconId = UiR.drawable.baseline_library_music_24,
        unselectedIconId = UiR.drawable.baseline_library_music_24
    ),
    HOME(
        route = RecognitionScreen.ROUTE,
        titleId = StringsR.string.recognition,
        selectedIconId = UiR.drawable.baseline_home_24,
        unselectedIconId = UiR.drawable.baseline_home_24
    ),
    Preferences(
        route = PreferencesScreen.ROUTE,
        titleId = StringsR.string.preferences,
        selectedIconId = UiR.drawable.baseline_preferences_24,
        unselectedIconId = UiR.drawable.baseline_preferences_24
    )
}

val navBarDestList = NavBarDestination.values().asList()