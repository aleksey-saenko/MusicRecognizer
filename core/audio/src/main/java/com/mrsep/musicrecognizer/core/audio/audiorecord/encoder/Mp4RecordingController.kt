package com.mrsep.musicrecognizer.core.audio.audiorecord.encoder

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import androidx.annotation.OptIn
import androidx.media3.common.util.MediaFormatUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.container.Mp4TimestampData
import androidx.media3.container.Mp4TimestampData.unixTimeToMp4TimeSeconds
import androidx.media3.muxer.Mp4Muxer
import androidx.media3.muxer.Mp4Muxer.LAST_SAMPLE_DURATION_BEHAVIOR_SET_FROM_END_OF_STREAM_BUFFER_OR_DUPLICATE_PREVIOUS
import androidx.media3.muxer.MuxerUtil
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioEncoderDispatcher
import com.mrsep.musicrecognizer.core.audio.audiorecord.AudioEncoderHandler
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingSession
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecordingScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.ByteBuffer
import java.time.Instant
import java.util.UUID
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

@OptIn(UnstableApi::class)
internal class Mp4RecordingController(
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
        var codecRef: MediaCodec? = null
        val muxers: MutableMap<ScheduledRecording, MuxerWrapper> = mutableMapOf()
        try {
            val soundSourceParams = checkNotNull(soundSource.params)
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            codecRef = codec
            // MediaFormat will be updated once after start on receive codec-specific data (csd-0)
            var mediaFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                soundSourceParams.audioFormat.sampleRate,
                soundSourceParams.audioFormat.channelCount
            ).apply {
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, soundSourceParams.chunkSize)
                setInteger(MediaFormat.KEY_PCM_ENCODING, soundSourceParams.audioFormat.encoding)
                setInteger(MediaFormat.KEY_BIT_RATE, CODEC_BIT_RATE)
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            }

            val silenceTracker = UnsafeSilenceTracker()
            launch {
                soundSource.soundLevel
                    .map { it == 0f }
                    .distinctUntilChanged()
                    .collect(silenceTracker::onSilenceStateChanged)
            }

            lateinit var recorderStartTimestamp: Instant
            var isStartTimestampUpdated = false
            val pcmChunkChannel = soundSource.pcmChunkFlow
                .transform { pcmChunkResult ->
                    val pcmChunk = pcmChunkResult.getOrThrow()
                    if (!isStartTimestampUpdated) {
                        recorderStartTimestamp = Instant.now()
                            .minusMillis((soundSourceParams.chunkSizeInSeconds * 1_000).toLong())
                        isStartTimestampUpdated = true
                    }
                    emit(pcmChunk)
                }
                .buffer(Channel.UNLIMITED)
                .produceIn(this)


            val inputBufferIdChannel = Channel<Int>(Channel.UNLIMITED)
            var nextPresentationTimeSec = 0.0
            inputBufferIdChannel.receiveAsFlow().onEach { bufferId ->
                val inputBuffer = codec.getInputBuffer(bufferId) ?: return@onEach
                val pcmChunk = pcmChunkChannel.receive()
                inputBuffer.put(pcmChunk)
                codec.queueInputBuffer(
                    /* index = */ bufferId,
                    /* offset = */ 0,
                    /* size = */ pcmChunk.size,
                    /* presentationTimeUs = */ (nextPresentationTimeSec * 1_000_000).roundToLong(),
                    /* flags = */ 0
                )
                nextPresentationTimeSec += soundSourceParams.chunkSizeInSeconds
            }.launchIn(this)

            val callback = object : MediaCodec.Callback() {

                val scheduledRecordings = scheme.flatten()
                var emittedRecordingCount = 0

                override fun onInputBufferAvailable(codec: MediaCodec, bufferId: Int) {
                    if (bufferId >= 0) inputBufferIdChannel.trySendBlocking(bufferId)
                }

                override fun onOutputBufferAvailable(
                    codec: MediaCodec,
                    bufferId: Int,
                    bufferInfo: MediaCodec.BufferInfo,
                ) {
                    // Skip any non-data buffers, we already get csd from onOutputFormatChanged()
                    if (bufferId < 0 || bufferInfo.flags != 0) {
                        codec.releaseOutputBuffer(bufferId, false)
                        return
                    }
                    val outputBuffer = codec.getOutputBuffer(bufferId) ?: return
                    val bufferPresentationTimestamp = bufferInfo.presentationTimeUs.microseconds

                    for (scheduledRecording in scheduledRecordings) {
                        if (bufferPresentationTimestamp < scheduledRecording.presentationOffset) continue

                        val muxer = muxers.getOrPut(scheduledRecording) {
                            val startTimestamp = recorderStartTimestamp.plusMillis(bufferPresentationTimestamp.inWholeMilliseconds)
                            val outputFile = runBlocking {
                                audioRecordingDataSource.createNewFile(sessionId, RECORDING_FILE_EXT).getOrThrow()
                            }
                            MuxerWrapper(
                                outputFile = outputFile,
                                mediaFormat = mediaFormat,
                                startTimestamp = startTimestamp,
                                startPresentationTimestamp = bufferPresentationTimestamp
                            )
                        }
                        if (muxer.isReleased) continue

                        val currentMuxerDuration = muxer.currentDuration(bufferPresentationTimestamp)
                        if (currentMuxerDuration < scheduledRecording.minDuration) {
                            muxer.writeBuffer(outputBuffer, bufferInfo)
                        } else {
                            val file = muxer.release()
                            val silenceDuration = silenceTracker.querySilenceDuration(
                                startTime = muxer.startTimestamp,
                                endTime = muxer.startTimestamp.plusMillis(currentMuxerDuration.inWholeMilliseconds)
                            )
                            val recording = AudioRecording(
                                file = file,
                                timestamp = muxer.startTimestamp,
                                source = soundSource.audioSource,
                                duration = currentMuxerDuration,
                                nonSilenceDuration = currentMuxerDuration - silenceDuration,
                                sampleRate = soundSourceParams.audioFormat.sampleRate,
                                mimeType = MIME_TYPE,
                                isFallback = scheduledRecording.isFallback
                            )
                            channel.trySendBlocking(recording)
                            emittedRecordingCount++
                        }
                    }
                    codec.releaseOutputBuffer(bufferId, false)
                    if (scheduledRecordings.size == emittedRecordingCount) channel.close()
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                    channel.close(e)
                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                    // Should only happen once after starting when set codec-specific data (csd-0)
                    mediaFormat = format
                }
            }
            codec.setCallback(callback, AudioEncoderHandler)
            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
//            checkFloatEncodingSupport()
            codec.start()
            awaitCancellation()
        } catch (e: Exception) {
            ensureActive()
            channel.close(e)
        } finally {
            codecRef?.run { stop(); release() }
            muxers.forEach { _, muxer -> muxer.release() }
            channel.close()
        }
    }

    // This check is recommended by the documentation
    // But it turned out that sometimes the codec works well, although the check shows no support
    @Suppress("unused")
    private fun checkFloatEncodingSupport(sourceFormat: AudioFormat, codecFormat: MediaFormat) {
        if (sourceFormat.encoding == AudioFormat.ENCODING_PCM_FLOAT) {
            val isCodecConfiguredWithPcmFloat = try {
                codecFormat.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
            } catch (e: NullPointerException) {
                false
            }
            check(isCodecConfiguredWithPcmFloat) {
                "Configured mediaCodec doesn't support float PCM encoding on this device"
            }
        }
    }

    companion object {
        private const val MIME_TYPE = "audio/mp4"
        private const val RECORDING_FILE_EXT = "m4a"
        private const val CODEC_BIT_RATE = 160 * 1024  // 160 kBit/s
    }
}


@UnstableApi
private class MuxerWrapper(
    private val outputFile: File,
    private val mediaFormat: MediaFormat,
    private val startPresentationTimestamp: Duration,
    val startTimestamp: Instant,
) {
    private var muxer: Mp4Muxer? = null
    var isReleased = false
        private set

    fun currentDuration(nextPresentationTimestamp: Duration): Duration {
        return nextPresentationTimestamp - startPresentationTimestamp
    }

    fun writeBuffer(byteBuf: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        check(!isReleased)
        val muxer = muxer ?: Mp4Muxer.Builder(outputFile.outputStream())
            // Avoid to place metadata reserved space to minimize result file size
            .setAttemptStreamableOutputEnabled(false)
            .setSampleBatchingEnabled(false)
            // Allow to reuse buffer with the next muxer
            .setSampleCopyingEnabled(true)
            .setLastSampleDurationBehavior(LAST_SAMPLE_DURATION_BEHAVIOR_SET_FROM_END_OF_STREAM_BUFFER_OR_DUPLICATE_PREVIOUS)
            .build().apply {
                addTrack(MediaFormatUtil.createFormatFromMediaFormat(mediaFormat))
                val time = unixTimeToMp4TimeSeconds(startTimestamp.toEpochMilli())
                addMetadataEntry(Mp4TimestampData(time, time))
                muxer = this
            }
        val initialPosition = byteBuf.position()
        muxer.writeSampleData(0, byteBuf, MuxerUtil.getMuxerBufferInfoFromMediaCodecBufferInfo(bufferInfo))
        // Restore position to allow reuse with the next muxer
        byteBuf.position(initialPosition)
    }

    fun release(): File {
        muxer?.close()
        muxer = null
        isReleased = true
        return outputFile
    }
}

internal fun RecordingScheme.flatten() = buildList {
    fallback?.let { fallbackDuration ->
        add(
            ScheduledRecording(
                presentationOffset = Duration.ZERO,
                minDuration = fallbackDuration,
                isFallback = true
            )
        )
    }
    var stepOffset = Duration.ZERO
    for (step in steps) {
        for (duration in step.recordings) {
            add(
                ScheduledRecording(
                    presentationOffset = stepOffset,
                    minDuration = duration,
                    isFallback = false
                )
            )
        }
        stepOffset += step.recordings.last()
    }
}

internal data class ScheduledRecording(
    val presentationOffset: Duration,
    val minDuration: Duration,
    val isFallback: Boolean,
)
