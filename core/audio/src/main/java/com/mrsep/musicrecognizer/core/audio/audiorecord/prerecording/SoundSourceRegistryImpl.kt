package com.mrsep.musicrecognizer.core.audio.audiorecord.prerecording

import android.util.Log
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SoundSourceRegistryImpl"

@Singleton
internal class SoundSourceRegistryImpl @Inject constructor() : SoundSourceRegistry {

    private data class SoundSourceWithRefCount(
        val source: PrerecordingSoundSource,
        val refCount: Int,
    )

    private val mutex = Mutex()
    private val sources = mutableMapOf<SoundSourceKey, SoundSourceWithRefCount>()

    override suspend fun getOrCreate(
        key: SoundSourceKey,
        sourceProvider: () -> SoundSource,
    ): PrerecordingSoundSource = mutex.withLock {
        val existing = sources[key]

        if (existing != null && !existing.source.isClosed()) {
            sources[key] = existing.copy(refCount = existing.refCount + 1)
            Log.d(TAG, "Return existing source with key = $key, new refCount = ${sources[key]?.refCount}")
            return@withLock existing.source
        }

        if (existing != null) {
            Log.d(TAG, "Remove closed source with key = $key")
            sources.remove(key)
        }

        val created = PrerecordingSoundSourceImpl(key, sourceProvider())
        sources[key] = SoundSourceWithRefCount(source = created, refCount = 1)
        Log.d(TAG, "Create new source with key = $key, refCount = 1")
        created
    }

    override suspend fun getIfExists(key: SoundSourceKey): PrerecordingSoundSource? = mutex.withLock {
        val existing = sources[key] ?: return@withLock null

        if (existing.source.isClosed()) {
            Log.d(TAG, "Remove closed source with key = $key")
            sources.remove(key)
            return@withLock null
        }

        sources[key] = existing.copy(refCount = existing.refCount + 1)
        Log.d(TAG, "Return existing source for key = $key, new refCount = ${sources[key]?.refCount}")
        existing.source
    }

    override suspend fun close(key: SoundSourceKey) {
        val sourceToClose = mutex.withLock {
            val existing = sources[key] ?: return@withLock null

            if (existing.source.isClosed()) {
                Log.d(TAG, "Remove closed source with key = $key")
                sources.remove(key)
                return@withLock null
            }

            if (existing.refCount <= 1) {
                Log.d(TAG, "Remove and close source with key = $key (no more refs)")
                sources.remove(key)
                existing.source
            } else {
                sources[key] = existing.copy(refCount = existing.refCount - 1)
                Log.d(TAG, "Decrease refCount for key = $key, new refCount = ${sources[key]?.refCount}")
                null
            }
        }

        sourceToClose?.close()
    }

    private fun PrerecordingSoundSource.isClosed(): Boolean {
        return state.value is PrerecordingSoundSourceState.Closed
    }
}