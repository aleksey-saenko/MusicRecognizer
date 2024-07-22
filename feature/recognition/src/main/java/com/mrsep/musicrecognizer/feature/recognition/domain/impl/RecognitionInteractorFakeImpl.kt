@file:Suppress("unused")

package com.mrsep.musicrecognizer.feature.recognition.domain.impl

import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionInteractor
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackMetadataEnhancerScheduler
import com.mrsep.musicrecognizer.feature.recognition.domain.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionProvider
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
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
    private val enhancerScheduler: TrackMetadataEnhancerScheduler,
) : RecognitionInteractor {

    override val status = MutableStateFlow<RecognitionStatus>(RecognitionStatus.Ready)

    private var recognitionJob: Job? = null

    override fun launchRecognition(scope: CoroutineScope) {
        if (recognitionJob?.isCompleted == false) return
        recognitionJob = scope.launch {
            status.update { RecognitionStatus.Recognizing(false) }
            delay(2_000)
            status.update { RecognitionStatus.Recognizing(true) }
            delay(2_000)
            notifySuccess(launchEnhancer = false)
//            notifyNoMatches(RecognitionTask.Ignored)
//            testAllDoneStatuses()
//            notifyBadRecordingError()
//            notifyUnhandledError(RecognitionTask.Created(123, false))
//            notifyBadConnectionError(RecognitionTask.Created(123, false))
        }.setCancellationHandler()
    }

    override fun launchOfflineRecognition(scope: CoroutineScope) {
        launchRecognition(scope)
    }

    override fun cancelAndResetStatus() {
        if (recognitionJob?.isCompleted == true) {
            status.update { RecognitionStatus.Ready }
        } else {
            recognitionJob?.cancel()
        }
    }

    override fun resetFinalStatus() {
        status.update { oldStatus ->
            if (oldStatus is RecognitionStatus.Done) RecognitionStatus.Ready else oldStatus
        }
    }

    private suspend fun notifySuccess(launchEnhancer: Boolean) {
        val stubUrl = "https://www.google.com/search?q=stub"
        val fakeTrack = Track(
            id = persistentUUID,
            title = "Atom Heart Mother",
            artist = "Pink Floyd",
            album = "Stub",
            releaseDate = LocalDate.parse("1970-10-02", DateTimeFormatter.ISO_DATE),
            duration = Duration.ofSeconds(210),
            recognizedAt = Duration.ofSeconds(157),
            recognizedBy = RecognitionProvider.Audd,
            recognitionDate = Instant.now(),
            lyrics = "lyrics stub",
            artworkThumbUrl = "https://e-cdns-images.dzcdn.net/images/cover/dce2b0c4e6498a136df041df7e85aba3/400x400-000000-80-0-0.jpg",
            artworkUrl = "https://e-cdns-images.dzcdn.net/images/cover/dce2b0c4e6498a136df041df7e85aba3/1500x1500-000000-80-0-0.jpg",
//            artworkUrl = "https://upload.wikimedia.org/wikipedia/ru/1/10/PinkFloyd-album-atomheartmother.jpg",
//            artworkUrl = "https://upload.wikimedia.org/wikipedia/ru/4/46/Obscured_by_Clouds_Pink_Floyd.jpg",
//            artworkUrl = "https://upload.wikimedia.org/wikipedia/ru/1/1e/Meddle_album_cover.jpg",
            trackLinks = listOf(
                TrackLink(stubUrl, MusicService.AppleMusic),
                TrackLink(stubUrl, MusicService.Spotify),
                TrackLink(stubUrl, MusicService.Soundcloud),
                TrackLink(stubUrl, MusicService.Youtube),
                TrackLink(stubUrl, MusicService.Deezer),
                TrackLink(stubUrl, MusicService.Napster),
                TrackLink(stubUrl, MusicService.MusicBrainz),
            ),
            properties = Track.Properties(
                isFavorite = false,
                isViewed = false,
                themeSeedColor = null,
            )
        )
        val trackWithStoredProps = trackRepository.upsertKeepProperties(fakeTrack)[0]
        trackRepository.setViewed(trackWithStoredProps.id, false)
        val updatedTrack = trackWithStoredProps.copy(
            properties = trackWithStoredProps.properties.copy(isViewed = false)
        )
        if (launchEnhancer) {
            enhancerScheduler.enqueue(updatedTrack.id)
        }
        val fakeResult = RecognitionStatus.Done(RecognitionResult.Success(updatedTrack))
        status.update { fakeResult }
    }

    private suspend fun resetToReadyWithDelay() {
        delay(3000)
        status.update { RecognitionStatus.Ready }
        delay(2000)
    }

    private fun notifyBadConnectionError(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.BadConnection,
                recognitionTask = recognitionTask
            )
        )
        status.update { fakeResult }
    }

    private fun notifyBadRecordingError() {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.BadRecording("Err audio"),
                recognitionTask = RecognitionTask.Ignored
            )
        )
        status.update { fakeResult }
    }

    private fun notifyNoMatches(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.NoMatches(recognitionTask = recognitionTask)
        )
        status.update { fakeResult }
    }

    private fun notifyApiUsageLimited(recognitionTask: RecognitionTask) {
        val fakeResult = RecognitionStatus.Done(
            RecognitionResult.Error(
                remoteError = RemoteRecognitionResult.Error.ApiUsageLimited,
                recognitionTask = recognitionTask
            )
        )
        status.update { fakeResult }
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
        status.update { fakeResult }
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
        status.update { fakeResult }
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

        notifyApiUsageLimited(taskLaunched)
        resetToReadyWithDelay()
        notifyApiUsageLimited(taskIdle)
        resetToReadyWithDelay()
        notifyApiUsageLimited(taskIgnored)
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

        notifySuccess(launchEnhancer = false)
    }

    private fun Job.setCancellationHandler() = apply {
        invokeOnCompletion { cause ->
            when (cause) {
                is CancellationException -> {
                    status.update { RecognitionStatus.Ready }
                }
            }
        }
    }

    companion object {
        private val persistentUUID by lazy {
            UUID.randomUUID().toString()
        }
    }
}
