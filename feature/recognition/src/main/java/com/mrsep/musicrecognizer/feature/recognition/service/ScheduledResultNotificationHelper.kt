package com.mrsep.musicrecognizer.feature.recognition.service

import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognition

internal interface ScheduledResultNotificationHelper {

    suspend fun notify(enqueuedRecognition: EnqueuedRecognition)
}
