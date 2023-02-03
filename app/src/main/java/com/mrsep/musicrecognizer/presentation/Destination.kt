package com.mrsep.musicrecognizer.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mrsep.musicrecognizer.R

enum class Destination(
    val route: String,
    @StringRes val titleId: Int,
    @DrawableRes val selectedIconId: Int,
    @DrawableRes val unselectedIconId: Int
) {
    HISTORY(
        route = "history",
        titleId = R.string.history,
        selectedIconId = R.drawable.baseline_history_24,
        unselectedIconId = R.drawable.baseline_history_24
    ),
    HOME(
        route = "home",
        titleId = R.string.home,
        selectedIconId = R.drawable.baseline_home_24,
        unselectedIconId = R.drawable.baseline_home_24
    ),
    SETTINGS(
        route = "settings",
        titleId = R.string.settings,
        selectedIconId = R.drawable.baseline_settings_24,
        unselectedIconId = R.drawable.baseline_settings_24
    ),
}
val destinationList = Destination.values().asList()