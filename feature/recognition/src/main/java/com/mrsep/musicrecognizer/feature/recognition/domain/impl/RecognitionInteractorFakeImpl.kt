@file:Suppress("unused")
package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.ScreenRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.ServiceRecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track
import com.mrsep.musicrecognizer.feature.recognition.domain.model.TrackLink
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

private const val TAG = "RecognitionInteractorFakeImpl"

@Singleton
internal class RecognitionInteractorFakeImpl @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val trackRepository: TrackRepository,
    private val resultDelegator: RecognitionStatusDelegator
) : ScreenRecognitionInteractor, ServiceRecognitionInteractor {

    override val screenRecognitionStatus get() = resultDelegator.screenState
    override val serviceRecognitionStatus get() = resultDelegator.serviceState

    private var recognitionJob: Job? = null

    override fun launchRecognition(scope: CoroutineScope) {
        if (recognitionJob?.isCompleted == false) return
        recognitionJob = scope.launch {
            resultDelegator.notify(RecognitionStatus.Recognizing(false))
            delay(4000)
            notifySuccess()
//            testAllDoneStatuses()
//            notifyUnhandledError(RecognitionTask.Created(123, false))
//            notifyBadConnectionError(RecognitionTask.Created(123, false))
        }.setCancellationHandler()
    }

    override fun launchOfflineRecognition(scope: CoroutineScope) {
        launchRecognition(scope)
//        if (recognitionJob?.isCompleted == false) return
//        recognitionJob = scope.launch {
//            resultDelegator.notify(RecognitionStatus.Recognizing(false))
//            delay(1_000)
//            resultDelegator.notify(RecognitionStatus.Recognizing(true))
//            delay(1_000)
//            notifySuccess()
//        }.setCancellationHandler()
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
            title = "Echoes", //#${kotlin.random.Random.nextInt()}
            artist = "Pink Floyd",
            album = "Meddle",
            releaseDate = LocalDate.parse("1971-10-30", DateTimeFormatter.ISO_DATE),
            lyrics = "lyrics stub",
            artworkUrl = "https://upload.wikimedia.org/wikipedia/ru/1/1e/Meddle_album_cover.jpg",
            trackLinks = MusicService.entries.map { service -> TrackLink("", service) },
            metadata = Track.Metadata(
                lastRecognitionDate = Instant.now(),
                isFavorite = false,
                themeSeedColor = null
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
                remoteError = RemoteRecognitionResult.Error.BadRecording("Err audio"),
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
                remoteError = RemoteRecognitionResult.Error.HttpError(
                    code = 404,
                    message = "Not Found"
                ),
                recognitionTask = recognitionTask
            )
        )
        resultDelegator.notify(fakeResult)
    }

    private fun notifyUnhandledError(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.UnhandledError(
                    message = "Some UnhandledError",
                    cause = RuntimeException("Some UnhandledError")
                ),
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
                    resultDelegator.notify(RecognitionStatus.Ready)
                }
            }
        }
    }

}