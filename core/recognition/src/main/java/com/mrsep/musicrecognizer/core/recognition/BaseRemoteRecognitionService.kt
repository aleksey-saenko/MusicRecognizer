package com.mrsep.musicrecognizer.core.recognition

import com.mrsep.musicrecognizer.core.domain.recognition.AudioSample
import com.mrsep.musicrecognizer.core.domain.recognition.RemoteRecognitionService
import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

internal abstract class BaseRemoteRecognitionService(
    private val ioDispatcher: CoroutineDispatcher,
) : RemoteRecognitionService {

    override suspend fun recognizeUntilFirstMatch(samples: Flow<AudioSample>) = withContext(ioDispatcher) {
        var retryCounter = 0
        // Initially bad connection
        // for case when after TIMEOUT_AFTER_LAST_SAMPLE_RECEIVED there is no any response yet
        var lastResult: RemoteRecognitionResult = RemoteRecognitionResult.Error.BadConnection

        val samplesChannel = Channel<AudioSample>(Channel.UNLIMITED)
        val receiveSamplesWithTimeoutJob = launch {
            samples.collect(samplesChannel::send)
            samplesChannel.close()
            delay(TIMEOUT_AFTER_LAST_SAMPLE_RECEIVED)
        }

        val remoteResultJob = samplesChannel.receiveAsFlow()
            .onEmpty {
                lastResult = RemoteRecognitionResult.Error.BadRecording("Empty audio samples flow")
            }
            .transformWhile<AudioSample, Unit> { sample ->
                lastResult = recognize(sample)
                while (lastResult.isRetryRequired() && retryCounter < RETRY_ON_ERROR_LIMIT) {
                    delayForRetry(retryCounter)
                    retryCounter++
                    lastResult = recognize(sample)
                }
                // Continue recognition only if no matches
                lastResult is RemoteRecognitionResult.NoMatches
            }
            .launchIn(this)

        select {
            remoteResultJob.onJoin { receiveSamplesWithTimeoutJob.cancelAndJoin() }
            receiveSamplesWithTimeoutJob.onJoin { remoteResultJob.cancelAndJoin() }
        }

        lastResult
    }

    private suspend fun delayForRetry(retryAttempt: Int) {
        val expDelay = minOf((2.0.pow(retryAttempt) * BASE_RETRY_DELAY).toLong(), MAX_RETRY_DELAY)
        val jitter = Random.nextLong(0, JITTER_MAX_DELAY)
        delay(expDelay + jitter)
    }

    private fun RemoteRecognitionResult.isRetryRequired() = when (this) {
        RemoteRecognitionResult.Error.BadConnection -> true
        is RemoteRecognitionResult.Error.HttpError -> code in 500..599
        else -> false
    }

    companion object {
        private val TIMEOUT_AFTER_LAST_SAMPLE_RECEIVED = 10.seconds
        private const val RETRY_ON_ERROR_LIMIT = 2

        private const val BASE_RETRY_DELAY = 1000L
        private const val MAX_RETRY_DELAY = 5000L
        private const val JITTER_MAX_DELAY = 1000L
    }
}