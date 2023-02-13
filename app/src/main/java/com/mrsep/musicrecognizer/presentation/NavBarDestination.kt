package com.mrsep.musicrecognizer.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mrsep.musicrecognizer.R

enum class NavBarDestination(
    val route: String,
    @StringRes val titleId: Int,
    @DrawableRes val selectedIconId: Int,
    @DrawableRes val unselectedIconId: Int
) {
    HISTORY(
        route = "recently",
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
        route = "preferences",
        titleId = R.string.settings,
        selectedIconId = R.drawable.baseline_settings_24,
        unselectedIconId = R.drawable.baseline_settings_24
    ),
}

val navBarDestList = NavBarDestination.values().asList()