package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.PcmChunk
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds

internal class PrerecordingRingBuffer(
    initialMaxDuration: Duration,
) : PrerecordingBuffer {

    init {
        require(!initialMaxDuration.isNegative()) { "maxDuration must not be negative" }
    }

    private val mutex = Mutex()
    private val chunks = ArrayDeque<PcmChunk>()
    private var latestChunkEnd: Instant? = null
    private var maxDuration: Duration = initialMaxDuration

    override suspend fun append(chunk: PcmChunk) {
        mutex.withLock {
            require(chunk.duration >= Duration.ZERO) { "Chunk duration must be non-negative" }
            latestChunkEnd?.let { currentLatestChunkEnd ->
                require(chunk.startInstant >= currentLatestChunkEnd) {
                    "Chunks must be placed in chronological order"
                }
            }
            chunks.addLast(chunk)
            latestChunkEnd = chunk.endInstant
            trim()
        }
    }

    override suspend fun snapshot(): List<PcmChunk> {
        return mutex.withLock { chunks.toList() }
    }

    override suspend fun clear() {
        mutex.withLock {
            chunks.clear()
            latestChunkEnd = null
        }
    }

    override suspend fun resize(maxDuration: Duration) {
        require(!maxDuration.isNegative()) { "maxDuration must not be negative" }
        mutex.withLock {
            this.maxDuration = maxDuration
            trim()
        }
    }

    override suspend fun currentMaxDuration(): Duration {
        return mutex.withLock { maxDuration }
    }

    private fun trim() {
        if (chunks.isEmpty()) return
        if (maxDuration == 0.seconds) {
            chunks.clear()
            latestChunkEnd = null
            return
        }

        val latestEnd = latestChunkEnd ?: return
        val cutoff = latestEnd - maxDuration

        while (chunks.isNotEmpty()) {
            val first = chunks.first()
            if (first.endInstant > cutoff) break
            chunks.removeFirst()
        }
    }
}
