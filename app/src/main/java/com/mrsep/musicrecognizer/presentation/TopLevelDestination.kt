package com.mrsep.musicrecognizer.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

enum class TopLevelDestination(
    val route: String,
    @StringRes val titleResId: Int,
    @DrawableRes val selectedIconResId: Int,
    @DrawableRes val unselectedIconResId: Int
) {
    Recognition(
        route = "nav_graph_recognition",
        titleResId = StringsR.string.recognition,
        selectedIconResId = UiR.drawable.ic_lines_24,
        unselectedIconResId = UiR.drawable.ic_lines_24
    ),
    Library(
        route = "nav_graph_library",
        titleResId = StringsR.string.library,
        selectedIconResId = UiR.drawable.outline_library_music_fill1_24,
        unselectedIconResId = UiR.drawable.outline_library_music_24
    ),
    Queue(
        route = "nav_graph_queue",
        titleResId = StringsR.string.queue,
        selectedIconResId = UiR.drawable.outline_overview_fill1_24,
        unselectedIconResId = UiR.drawable.outline_overview_24
    ),
    Preferences(
        route = "nav_graph_preferences",
        titleResId = StringsR.string.preferences,
        selectedIconResId = UiR.drawable.outline_settings_fill1_24,
        unselectedIconResId = UiR.drawable.outline_settings_24
    )
}
