package com.mrsep.musicrecognizer.core.domain.usecase

import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataFetchManager
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import javax.inject.Inject

class DeleteTrack @Inject constructor(
    private val trackRepository: TrackRepository,
    private val trackMetadataFetchManager: TrackMetadataFetchManager,
) {
    suspend operator fun invoke(trackId: String) {
        trackMetadataFetchManager.cancelAllForTrack(trackId)
        trackRepository.delete(listOf(trackId))
    }

    suspend operator fun invoke(trackIds: List<String>) {
        trackIds.forEach(trackMetadataFetchManager::cancelAllForTrack)
        trackRepository.delete(trackIds)
    }
}
