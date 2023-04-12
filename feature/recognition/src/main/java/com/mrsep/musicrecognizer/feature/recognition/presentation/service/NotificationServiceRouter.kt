package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import android.content.Intent

interface NotificationServiceRouter {

    fun getDeepLinkIntentToTrack(mbId: String): Intent

    fun getDeepLinkIntentToRecognitionQueue(): Intent

}