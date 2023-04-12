package com.mrsep.musicrecognizer.core.ui

abstract class Screen {

    abstract val route: String

    companion object {
        const val ROOT_DEEP_LINK = "https://www.mrsep.musicrecognizer.com"
    }

}