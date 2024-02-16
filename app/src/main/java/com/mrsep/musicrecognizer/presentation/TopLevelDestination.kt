package com.mrsep.musicrecognizer.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.feature.library.presentation.library.LibraryScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.RecognitionScreen

enum class TopLevelDestination(
    val route: String,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int,
    @DrawableRes val iconSelectedResId: Int
) {
    Library(
        route = LibraryScreen.ROUTE,
        titleResId = StringsR.string.library,
        iconResId = UiR.drawable.outline_library_music_24,
        iconSelectedResId = UiR.drawable.outline_library_music_fill1_24
    ),
    Recognition(
        route = RecognitionScreen.ROUTE,
        titleResId = StringsR.string.recognition,
        iconResId = UiR.drawable.ic_lines_wave,
        iconSelectedResId = UiR.drawable.ic_lines_wave
    ),
    Preferences(
        route = PreferencesScreen.ROUTE,
        titleResId = StringsR.string.preferences,
        iconResId = UiR.drawable.outline_settings_24,
        iconSelectedResId = UiR.drawable.outline_settings_fill1_24
    )
}