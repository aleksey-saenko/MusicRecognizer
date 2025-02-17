package com.mrsep.musicrecognizer.core.audio.audiorecord

import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AacEncoder
import com.mrsep.musicrecognizer.core.audio.audiorecord.soundsource.SoundSource
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecording
import com.mrsep.musicrecognizer.core.domain.recognition.AudioRecordingController
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionScheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

// TODO It definitely should be refactored
internal class AudioRecordingControllerImpl(
    private val soundSource: SoundSource,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AudioRecordingController {

    override val soundLevel = soundSource.soundLevel

    private val aacEncoder = AacEncoder(soundSource)

    override fun audioRecordingFlow(scheme: RecognitionScheme) = audioRecordingBytesFlow(scheme)
        .zip(nonSilenceDurationFlow(scheme)) { recordingResult, durations ->
            recordingResult.map { bytes ->
                AudioRecording(
                    data = bytes,
                    duration = durations.total,
                    nonSilenceDuration = durations.nonSilence
                )
            }
        }
        .flowOn(defaultDispatcher)

    private fun audioRecordingBytesFlow(scheme: RecognitionScheme) = flow {
        var currentRecStepIndex = 0
        var nextPacketIndexAfterSplitter = 0
        val packetsList = mutableListOf<ByteArray>()

        aacEncoder.aacPacketsFlow
            .transformWhile { aacPacketResult ->
                var takeWhile = false
                aacPacketResult.onSuccess { aacPacket ->
                    this.emit(aacPacket)
                    takeWhile = currentRecStepIndex <= scheme.steps.lastIndex
                }.onFailure { cause ->
                    this@flow.emit(Result.failure<ByteArray>(cause))
                }
                takeWhile
            }
            .collect { aacPacket ->
                packetsList.add(aacPacket.data)
                val currentRecStep = scheme.steps[currentRecStepIndex]
                val currentThreshold = currentRecStep.timestamp.inWholeMicroseconds
                if (aacPacket.timestampUs >= currentThreshold) {
                    val selectedPackets =
                        packetsList.subList(nextPacketIndexAfterSplitter, packetsList.size)
                    combineAndEmit(selectedPackets)
                    if (currentRecStep.splitter) {
                        if (scheme.sendTotalAtEnd) {
                            nextPacketIndexAfterSplitter = packetsList.lastIndex + 1
                        } else {
                            packetsList.clear()
                        }
                    }
                    if (currentRecStepIndex == scheme.steps.lastIndex && scheme.sendTotalAtEnd) {
                        combineAndEmit(packetsList)
                    }
                    currentRecStepIndex++
                }
            }
    }

    private suspend fun FlowCollector<Result<ByteArray>>.combineAndEmit(packets: List<ByteArray>) {
        val totalSize = packets.fold(0) { acc, array -> acc + array.size }
        val totalData = packets
            .fold(ByteBuffer.allocate(totalSize)) { acc, array -> acc.put(array) }.array()
        emit(Result.success(totalData))
    }

    private fun nonSilenceDurationFlow(scheme: RecognitionScheme) = channelFlow {
        val timeSource = TimeSource.Monotonic
        var stepNonSilenceDuration = 0.milliseconds
        var totalNonSilenceDuration = 0.milliseconds
        val stepsRemainingDurations = scheme.getStepsRemainingDurations()
        val stepsEmittingDurations = scheme.getStepsEmittingDurations()
        scheme.steps.forEachIndexed { index, step ->
            var lastSoundDetectedTimeMark = timeSource.markNow()
            var lastIsSoundDetected = false
            val nonSilenceCollector = launch {
                soundSource.soundLevel
                    .map { it > 0f }
                    .distinctUntilChanged()
                    .collect { isSoundDetected ->
                        if (isSoundDetected) {
                            lastSoundDetectedTimeMark = timeSource.markNow()
                        } else if (lastIsSoundDetected) {
                            stepNonSilenceDuration += lastSoundDetectedTimeMark.elapsedNow()
                        }
                        lastIsSoundDetected = isSoundDetected
                    }
            }
            delay(stepsRemainingDurations[index])
            nonSilenceCollector.cancelAndJoin()
            if (lastIsSoundDetected) {
                stepNonSilenceDuration += lastSoundDetectedTimeMark.elapsedNow()
            }
            send(
                RecordingDurations(
                total = stepsEmittingDurations[index],
                nonSilence = stepNonSilenceDuration.coerceAtMost(stepsEmittingDurations[index])
            )
            )
            if (step.splitter) {
                totalNonSilenceDuration += stepNonSilenceDuration
                stepNonSilenceDuration = 0.milliseconds
            }
        }
        totalNonSilenceDuration += stepNonSilenceDuration
        if (scheme.sendTotalAtEnd) {
            val totalDuration = scheme.steps.last().timestamp
            send(
                RecordingDurations(
                total = totalDuration,
                nonSilence = totalNonSilenceDuration.coerceAtMost(totalDuration)
            )
            )
        }
    }

    private fun RecognitionScheme.getStepsRemainingDurations(): List<Duration> {
        var prevTimestamp = 0.seconds
        return steps.map { step ->
            val duration = step.timestamp - prevTimestamp
            prevTimestamp = step.timestamp
            duration
        }
    }

    private fun RecognitionScheme.getStepsEmittingDurations(): List<Duration> {
        var prevSplitter = 0.seconds
        return steps.map { step ->
            val duration = step.timestamp - prevSplitter
            if (step.splitter) prevSplitter = step.timestamp
            duration
        }
    }
}

private class RecordingDurations(
    val total: Duration,
    val nonSilence: Duration,
)
