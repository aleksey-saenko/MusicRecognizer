package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.ScreenRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("unused")
private const val TAG = "RecognitionInteractorFakeImpl"

@Singleton
internal class RecognitionInteractorFakeImpl @Inject constructor(
//    private val recorderController: AudioRecorderController,
//    private val recognitionService: RemoteRecognitionService,
    private val preferencesRepository: PreferencesRepository,
    private val trackRepository: TrackRepository,
    private val resultDelegator: RecognitionResultDelegator
) : ScreenRecognitionInteractor, ServiceRecognitionInteractor {

    override val screenRecognitionStatus get() = resultDelegator.screenState
    override val serviceRecognitionStatus get() = resultDelegator.serviceState

    private var recognitionJob: Job? = null

    override fun launchRecognition(scope: CoroutineScope) {
        if (recognitionJob?.isCompleted == false) return
        recognitionJob = scope.launch {
//            val userPreferences = preferencesRepository.userPreferencesFlow.first()
            resultDelegator.notify(RecognitionStatus.Recognizing(false))
            delay(1000)
            resultDelegator.notify(RecognitionStatus.Recognizing(true))
            delay(1000)
            notifySuccess()
//            notifyNoMatches(RecognitionTask.Created(123, false))
//            testAllDoneStatuses()
//            notifyUnhandledError(RecognitionTask.Created(123, false))
//            notifyBadConnectionError(RecognitionTask.Created(123, false))
        }.setCancellationHandler()
    }

    override fun launchOfflineRecognition(scope: CoroutineScope) {
        if (recognitionJob?.isCompleted == false) return
        recognitionJob = scope.launch {
//            val userPreferences = preferencesRepository.userPreferencesFlow.first()
            resultDelegator.notify(RecognitionStatus.Recognizing(false))
            delay(1000)
            resultDelegator.notify(RecognitionStatus.Recognizing(true))
            delay(1000)
            notifySuccess()
        }.setCancellationHandler()
    }

    override fun cancelAndResetStatus() {
        if (recognitionJob?.isCompleted == true) {
            resultDelegator.notify(RecognitionStatus.Ready)
        } else {
            recognitionJob?.cancel()
        }
    }


    private suspend fun notifySuccess() {
        val fakeTrack = Track(
            mbId = UUID.randomUUID().toString(),
            title = "Echoes",
            artist = "Pink Floyd",
            album = "Meddle",
            releaseDate = LocalDate.parse("1971-10-30", DateTimeFormatter.ISO_DATE),
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
        trackRepository.insertOrReplaceSaveMetadata(fakeTrack)
        val fakeResult = RecognitionStatus.Done(RecognitionResult.Success(fakeTrack))
        resultDelegator.notify(fakeResult)
    }

    private suspend fun resetToReadyWithDelay() {
        delay(3000)
        resultDelegator.notify(RecognitionStatus.Ready)
        delay(2000)
    }



    private fun notifyBadConnectionError(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.BadConnection,
                recognitionTask = recognitionTask
            )
        )
        resultDelegator.notify(fakeResult)
    }

    private fun notifyBadRecordingError() {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.BadRecording("Error audio format"),
                recognitionTask = RecognitionTask.Ignored
            )
        )
        resultDelegator.notify(fakeResult)
    }

    private fun notifyNoMatches(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.NoMatches(recognitionTask = recognitionTask)
        )
        resultDelegator.notify(fakeResult)
    }

    private fun notifyWrongToken(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.WrongToken(isLimitReached = true),
                recognitionTask = recognitionTask
            )
        )
        resultDelegator.notify(fakeResult)
    }

    private fun notifyHttpError(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.HttpError(code = 404, message = "Not Found"),
                recognitionTask = recognitionTask
            )
        )
        resultDelegator.notify(fakeResult)
    }

    private fun notifyUnhandledError(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.UnhandledError(message = "Some UnhandledError", cause = RuntimeException("Some UnhandledError")),
                recognitionTask = recognitionTask
            )
        )
        resultDelegator.notify(fakeResult)
    }

    private suspend fun testAllDoneStatuses() {
        val taskLaunched = RecognitionTask.Created(123, true)
        val taskIdle = RecognitionTask.Created(123, false)
        val taskIgnored = RecognitionTask.Ignored

        notifyNoMatches(taskLaunched)
        resetToReadyWithDelay()
        notifyNoMatches(taskIdle)
        resetToReadyWithDelay()
        notifyNoMatches(taskIgnored)
        resetToReadyWithDelay()

        notifyBadConnectionError(taskLaunched)
        resetToReadyWithDelay()
        notifyBadConnectionError(taskIdle)
        resetToReadyWithDelay()
        notifyBadConnectionError(taskIgnored)
        resetToReadyWithDelay()

        notifyBadRecordingError()
        resetToReadyWithDelay()

        notifyWrongToken(taskLaunched)
        resetToReadyWithDelay()
        notifyWrongToken(taskIdle)
        resetToReadyWithDelay()
        notifyWrongToken(taskIgnored)
        resetToReadyWithDelay()

        notifyHttpError(taskLaunched)
        resetToReadyWithDelay()
        notifyHttpError(taskIdle)
        resetToReadyWithDelay()
        notifyHttpError(taskIgnored)
        resetToReadyWithDelay()

        notifyUnhandledError(taskLaunched)
        resetToReadyWithDelay()
        notifyUnhandledError(taskIdle)
        resetToReadyWithDelay()
        notifyUnhandledError(taskIgnored)
        resetToReadyWithDelay()

        notifySuccess()
    }

    private fun Job.setCancellationHandler() = apply {
        invokeOnCompletion { cause ->
            when (cause) {
                is CancellationException -> {
//                    recognitionJob = null
                    resultDelegator.notify(RecognitionStatus.Ready)
                }
            }
        }
    }



}

