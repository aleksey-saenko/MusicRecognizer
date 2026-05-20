package com.mrsep.musicrecognizer.core.domain.recognition

interface ResultNotificationManager {

    fun cancelBackgroundMatches(trackIds: Set<String>)
    fun cancelForegroundMatches(trackIds: Set<String>)
    fun cancelScheduledRecognitionsMatches(trackIds: Set<String>)

    fun cancelAllMatches(trackIds: Set<String>)

    fun cancelUnsuccessfulBackgroundAndForegroundResults()
}
