package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class RecognitionInteractorFakeImpl @Inject constructor() : RecognitionInteractor {

    private val _statusFlow = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)
    override val statusFlow = _statusFlow.asStateFlow()

    private var recognitionJob: Job? = null

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    override fun launchRecognitionOrCancel(scope: CoroutineScope) {
        if (recognitionJob?.isActive == true) {
            cancelRecognition()
        } else {
            recognitionJob = scope.launch { launchRecognition() }
        }
    }

    override fun cancelRecognition() {
        recognitionJob?.cancel()
        _statusFlow.update { RecognitionStatus.Ready }
    }

    override suspend fun resetStatusToReady(addLastRecordToQueue: Boolean) {
        check(recognitionJob?.isCompleted == true) {
            "resetStatusToReady() must be invoked after recognitionJob completion"
        }
        _statusFlow.update { RecognitionStatus.Ready }
    }

    private suspend fun launchRecognition() {
        withContext(defaultDispatcher) {
            _statusFlow.update { RecognitionStatus.Listening }
            delay(2000)
            _statusFlow.update { RecognitionStatus.Recognizing }
            delay(2000)
            _statusFlow.update { RecognitionStatus.Success(fakeTrack) }
//            _statusFlow.update { RecognitionStatus.NoMatches(File("")) }
        }
    }

}

private val fakeTrack = Track(
    mbId = UUID.randomUUID().toString(),
    title = "Echoes",
    artist = "Pink Floyd",
    album = "Meddle",
    releaseDate = LocalDate.of(1971, 10, 30),
    lyrics = "lyrics stub",
    links = Track.Links(
        artwork = "https://upload.wikimedia.org/wikipedia/ru/1/1e/Meddle_album_cover.jpg",
        spotify = null,
        youtube = null,
        soundCloud = null,
        appleMusic = null,
        musicBrainz = null,
        deezer = null,
        napster = null
    ),
    metadata = Track.Metadata(
        lastRecognitionDate = Instant.now(),
        isFavorite = false
    )
)