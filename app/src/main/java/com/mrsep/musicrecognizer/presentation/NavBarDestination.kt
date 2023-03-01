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
    Recently(
        route = "recently",
        titleId = R.string.recently,
        selectedIconId = R.drawable.baseline_recently_24,
        unselectedIconId = R.drawable.baseline_recently_24
    ),
    HOME(
        route = "home",
        titleId = R.string.home,
        selectedIconId = R.drawable.baseline_home_24,
        unselectedIconId = R.drawable.baseline_home_24
    ),
    Preferences(
        route = "preferences",
        titleId = R.string.preferences,
        selectedIconId = R.drawable.baseline_preferences_24,
        unselectedIconId = R.drawable.baseline_preferences_24
    ),
}

val navBarDestList = NavBarDestination.values().asList()