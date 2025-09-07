package com.mrsep.musicrecognizer.feature.recognition

import android.content.Intent

interface DeeplinkRouter {

    fun getDeepLinkIntentToTrack(trackId: String): Intent

    fun getDeepLinkIntentToLyrics(trackId: String): Intent

    fun getDeepLinkIntentToQueue(): Intent
}
