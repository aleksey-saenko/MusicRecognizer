package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioEncoderDispatcher
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSourceConfig
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingSession
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.UUID
import kotlin.getOrElse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
internal class WavRecordingController(
    private val soundSource: SoundSource,
    private val audioRecordingDataSource: AudioRecordingDataSource,
) : AudioRecordingController {

    override val soundLevel = soundSource.soundLevel

    context(scope: CoroutineScope)
    override fun startRecordingSession(scheme: RecordingScheme) = object : AudioRecordingSession {
        private val sessionId = UUID.randomUUID()
        override val recordings = Channel<AudioRecording>(Channel.UNLIMITED)
        private val job = scope.produceRecordingsToChannel(sessionId, scheme, recordings)

        override suspend fun cancelAndDeleteSessionFiles() {
            job.cancelAndJoin()
            audioRecordingDataSource.deleteSessionFiles(sessionId)
        }
    }

    private fun CoroutineScope.produceRecordingsToChannel(
        sessionId: UUID,
        scheme: RecordingScheme,
        channel: SendChannel<AudioRecording>,
    ): Job = launch(AudioEncoderDispatcher) {
        val fileWrappers: MutableMap<ScheduledRecording, WavWriterWrapper> = mutableMapOf()
        try {
            val soundSourceParams = checkNotNull(soundSource.params)

            val silenceTracker = UnsafeSilenceTracker()
            launch {
                soundSource.soundLevel
                    .map { it == 0f }
                    .distinctUntilChanged()
                    .collect(silenceTracker::onSilenceStateChanged)
            }

            val scheduledRecordings = scheme.flatten()
            var emittedRecordingCount = 0

            lateinit var recorderStartTimestamp: Instant
            var isStartTimestampUpdated = false
            var nextPresentationTimeSec = 0.0

            soundSource.pcmChunkFlow
                .buffer(Channel.UNLIMITED)
                .collect { pcmChunkResult ->
                    val pcmChunk = pcmChunkResult.getOrElse { cause ->
                        channel.close(cause)
                        this@launch.cancel()
                        return@collect
                    }

                    if (!isStartTimestampUpdated) {
                        recorderStartTimestamp = Instant.now()
                            .minusMillis((soundSourceParams.chunkSizeInSeconds * 1_000).toLong())
                        isStartTimestampUpdated = true
                    }

                    val presentationTimestamp = nextPresentationTimeSec.seconds
                    nextPresentationTimeSec += soundSourceParams.chunkSizeInSeconds

                    for (scheduledRecording in scheduledRecordings) {
                        if (presentationTimestamp < scheduledRecording.presentationOffset) continue

                        val fileWrapper = fileWrappers.getOrPut(scheduledRecording) {
                            val startTimestamp = recorderStartTimestamp.plusMillis(presentationTimestamp.inWholeMilliseconds)
                            val outputFile = runBlocking {
                                audioRecordingDataSource.createNewFile(sessionId, RECORDING_FILE_EXT)
                            }.getOrElse { cause ->
                                channel.close(cause)
                                this@launch.cancel()
                                return@collect
                            }
                            WavWriterWrapper(
                                outputFile = outputFile,
                                chunkDuration = soundSourceParams.chunkSizeInSeconds.seconds,
                                startTimestamp = startTimestamp,
                                soundSourceConfig = soundSourceParams
                            )
                        }
                        if (fileWrapper.isReleased) continue

                        val currentFileDuration = fileWrapper.currentDuration
                        if (currentFileDuration < scheduledRecording.minDuration) {
                            try {
                                fileWrapper.writeChunk(pcmChunk)
                            } catch (e: IOException) {
                                channel.close(e)
                                this@launch.cancel()
                                return@collect
                            }
                        } else {
                            val file = fileWrapper.release()
                            val silenceDuration = silenceTracker.querySilenceDuration(
                                startTime = fileWrapper.startTimestamp,
                                endTime = fileWrapper.startTimestamp.plusMillis(currentFileDuration.inWholeMilliseconds)
                            )
                            val recording = AudioRecording(
                                file = file,
                                timestamp = fileWrapper.startTimestamp,
                                source = soundSource.audioSource,
                                duration = currentFileDuration,
                                nonSilenceDuration = currentFileDuration - silenceDuration,
                                sampleRate = soundSourceParams.audioFormat.sampleRate,
                                mimeType = MIME_TYPE,
                                isFallback = scheduledRecording.isFallback
                            )
                            channel.trySendBlocking(recording)
                            emittedRecordingCount++
                        }
                    }

                    if (scheduledRecordings.size == emittedRecordingCount) channel.close()
                }

        } catch (e: Exception) {
            ensureActive()
            channel.close(e)
        } finally {
            fileWrappers.forEach { (_, fileWrapper) -> fileWrapper.release() }
            channel.close()
        }
    }

    companion object {
        private const val MIME_TYPE = "audio/x-wav"
        private const val RECORDING_FILE_EXT = "wav"
    }
}

@UnstableApi
private class WavWriterWrapper(
    private val outputFile: File,
    private val chunkDuration: Duration,
    val startTimestamp: Instant,
    val soundSourceConfig: SoundSourceConfig,
) {
    var isReleased = false
        private set

    private var chunkCount = 0
    val currentDuration get() = chunkDuration * chunkCount
    private var wavWriter: WavWriter? = null

    @Throws(IOException::class)
    fun writeChunk(chunk: ByteArray) {
        check(!isReleased)
        val wavWriter = wavWriter ?: WavWriter(
            outputFile = outputFile,
            sampleRate = soundSourceConfig.audioFormat.sampleRate,
            channelCount = soundSourceConfig.audioFormat.channelCount,
            pcmEncoding = soundSourceConfig.audioFormat.encoding
        ).apply {
            wavWriter = this
        }
        wavWriter.write(chunk)
        chunkCount++
    }

    fun release(): File {
        wavWriter?.release()
        wavWriter = null
        isReleased = true
        return outputFile
    }
}
