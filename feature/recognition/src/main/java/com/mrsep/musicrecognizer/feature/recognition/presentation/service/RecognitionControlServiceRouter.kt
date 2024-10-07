package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.content.Intent

interface RecognitionControlServiceRouter {

    fun getDeepLinkIntentToTrack(trackId: String): Intent

    fun getDeepLinkIntentToLyrics(trackId: String): Intent
}
