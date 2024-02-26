package com.mrsep.musicrecognizer.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.feature.library.presentation.library.LibraryScreen
import com.mrsep.musicrecognizer.feature.preferences.presentation.PreferencesScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen.RecognitionQueueScreen
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.RecognitionScreen

enum class TopLevelDestination(
    val route: String,
    @StringRes val titleResId: Int,
    @DrawableRes val selectedIconResId: Int,
    @DrawableRes val unselectedIconResId: Int
) {
    Recognition(
        route = RecognitionScreen.ROUTE,
        titleResId = StringsR.string.recognition,
        selectedIconResId = UiR.drawable.ic_lines_24,
        unselectedIconResId = UiR.drawable.ic_lines_24
    ),
    Library(
        route = LibraryScreen.ROUTE,
        titleResId = StringsR.string.library,
        selectedIconResId = UiR.drawable.outline_library_music_fill1_24,
        unselectedIconResId = UiR.drawable.outline_library_music_24
    ),
    Queue(
        route = RecognitionQueueScreen.ROUTE,
        titleResId = StringsR.string.queue,
        selectedIconResId = UiR.drawable.outline_overview_fill1_24,
        unselectedIconResId = UiR.drawable.outline_overview_24
    ),
    Preferences(
        route = PreferencesScreen.ROUTE,
        titleResId = StringsR.string.preferences,
        selectedIconResId = UiR.drawable.outline_settings_fill1_24,
        unselectedIconResId = UiR.drawable.outline_settings_24
    )
}