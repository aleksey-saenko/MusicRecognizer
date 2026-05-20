package com.mrsep.musicrecognizer.core.domain.usecase

import com.mrsep.musicrecognizer.core.domain.recognition.ResultNotificationManager
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataFetchManager
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import javax.inject.Inject

class DeleteTrack @Inject constructor(
    private val trackRepository: TrackRepository,
    private val trackMetadataFetchManager: TrackMetadataFetchManager,
    private val resultNotificationManager: ResultNotificationManager,
) {

    suspend operator fun invoke(trackIds: List<String>) {
        resultNotificationManager.cancelAllMatches(trackIds.toSet())
        trackIds.forEach(trackMetadataFetchManager::cancelAllForTrack)
        trackRepository.delete(trackIds)
    }
}
