package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

import android.util.Log
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.PcmChunk
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSourceConfig
import com.mrsep.musicrecognizer.core.domain.recognition.AudioSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

private const val TAG = "PrerecordingSoundSourceImpl"

internal class PrerecordingSoundSourceImpl(
    override val key: SoundSourceKey,
    private val delegate: SoundSource,
) : PrerecordingSoundSource {

    override val audioSource: AudioSource get() = delegate.audioSource
    override val params: SoundSourceConfig? get() = delegate.params
    override val soundLevel: StateFlow<Float> = delegate.soundLevel

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()

    private val _state = MutableStateFlow<PrerecordingSoundSourceState>(PrerecordingSoundSourceState.Idle)
    override val state: StateFlow<PrerecordingSoundSourceState> = _state.asStateFlow()

    private val buffer = PrerecordingRingBuffer(Duration.ZERO)
    private val sessionChannels = mutableSetOf<Channel<PcmChunk>>()

    private var collectorJob: Job? = null
    private var prerecordingEnabled = false

    override suspend fun startPrerecording(bufferDuration: Duration): Boolean {
        require(!bufferDuration.isNegative()) { "bufferDuration must not be negative" }

        mutex.withLock {
            if (isClosedLocked()) return false
            prerecordingEnabled = true
            buffer.resize(bufferDuration)
            ensureCollectorStartedLocked()
            return !isClosedLocked()
        }
    }

    override suspend fun stopPrerecording() {
        mutex.withLock {
            if (isClosedLocked()) return
            prerecordingEnabled = false
            buffer.clear()
            stopCollectorIfIdleLocked()
        }
    }

    override suspend fun clearBuffer() {
        mutex.withLock {
            if (isClosedLocked()) return
            buffer.clear()
        }
    }

    override fun captureFlow(includeBuffered: Boolean): Flow<Result<PcmChunk>> = channelFlow {

        val sessionChannel = Channel<PcmChunk>(Channel.UNLIMITED)

        mutex.withLock {
            ensureCollectorStartedLocked()
            val currentState = _state.value
            if (currentState is PrerecordingSoundSourceState.Closed) {
                send(Result.failure(currentState.cause
                    ?: IllegalStateException("PrerecordingSoundSource is closed")))
                return@channelFlow
            }

            sessionChannels.add(sessionChannel)
            if (prerecordingEnabled && includeBuffered) {
                val bufferSnapshot = buffer.snapshot()
                if (bufferSnapshot.isNotEmpty()) {
                    val duration = bufferSnapshot.run { last().endInstant - first().startInstant }
                    Log.d(TAG, "Forwarding prerecorded buffer, duration = $duration")
                }
                for (chunk in bufferSnapshot) {
                    if (!sessionChannel.trySend(chunk).isSuccess) break
                }
            }
        }

        try {
            for (chunk in sessionChannel) {
                send(Result.success(chunk))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            send(Result.failure(e))
        } finally {
            mutex.withLock {
                sessionChannels.remove(sessionChannel)
                stopCollectorIfIdleLocked()
            }
            sessionChannel.close()
        }
    }
        .buffer(Channel.UNLIMITED)

    override suspend fun close() {
        mutex.withLock {
            closeLocked(null)
        }
    }

    private suspend fun ensureCollectorStartedLocked() {
        if (collectorJob?.isActive == true || isClosedLocked()) return

        collectorJob = coroutineScope.launch {
            delegate.pcmChunkFlow
                .buffer(Channel.UNLIMITED)
                .collect { result ->
                    mutex.withLock {
                        if (isClosedLocked()) return@withLock

                        result.onSuccess { chunk ->
                            if (prerecordingEnabled) {
                                buffer.append(chunk)
                            }
                            for (channel in sessionChannels) {
                                // Fast with UNLIMITED channels
                                channel.trySend(chunk)
                            }
                        }.onFailure { cause ->
                            closeLocked(cause)
                        }
                    }
            }
        }

        updateStateLocked()
    }

    private suspend fun stopCollectorIfIdleLocked() {
        if (isClosedLocked()) return
        if (!prerecordingEnabled && sessionChannels.isEmpty()) {
            collectorJob?.cancelAndJoin()
            collectorJob = null
        }
        updateStateLocked()
    }

    private suspend fun closeLocked(cause: Throwable?) {
        if (isClosedLocked()) return

        prerecordingEnabled = false
        buffer.clear()
        sessionChannels.forEach { it.close(cause) }
        sessionChannels.clear()

        collectorJob?.cancel()
        collectorJob = null

        _state.value = PrerecordingSoundSourceState.Closed(cause)

        coroutineScope.cancel()
    }

    private suspend fun updateStateLocked() {
        val currentState = _state.value
        _state.value = when {
            currentState is PrerecordingSoundSourceState.Closed -> currentState

            collectorJob?.isActive == true -> {
                val bufferDuration = if (prerecordingEnabled) buffer.currentMaxDuration() else null
                PrerecordingSoundSourceState.Recording(bufferDuration)
            }

            else -> PrerecordingSoundSourceState.Idle
        }
    }

    private fun isClosedLocked(): Boolean = _state.value is PrerecordingSoundSourceState.Closed
}
