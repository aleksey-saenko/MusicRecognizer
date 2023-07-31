package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import com.mrsep.musicrecognizer.feature.recognition.domain.model.EnqueuedRecognition

internal interface ScheduledResultNotificationHelper {

    suspend fun notify(enqueuedRecognition: EnqueuedRecognition)

}