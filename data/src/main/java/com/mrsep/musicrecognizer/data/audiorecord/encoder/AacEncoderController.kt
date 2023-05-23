package com.mrsep.musicrecognizer.data.audiorecord.encoder

import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingController
import com.mrsep.musicrecognizer.data.audiorecord.AudioRecordingDataStrategy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformWhile
import java.nio.ByteBuffer
import javax.inject.Inject

class AacEncoderController @Inject constructor(
    private val aacEncoder: AacEncoder,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : AudioRecordingController {

    override fun audioRecordingFlow(strategy: AudioRecordingDataStrategy) = flow {
        var currentRecStepIndex = 0
        var nextPacketIndexAfterSplitter = 0
        val packetsList = mutableListOf<ByteArray>()

        aacEncoder.aacPacketsFlow
            .transformWhile { aacPacketResult ->
                var takeWhile = false
                aacPacketResult.onSuccess { aacPacket ->
                    this.emit(aacPacket)
                    takeWhile = currentRecStepIndex <= strategy.steps.lastIndex
                }.onFailure { cause ->
                    this@flow.emit(Result.failure<ByteArray>(cause))
                }
                takeWhile
            }
            .collect { aacPacket ->
                packetsList.add(aacPacket.data)
                val currentRecStep = strategy.steps[currentRecStepIndex]
                val currentThreshold = currentRecStep.timestamp.inWholeMicroseconds
                if (aacPacket.timestampUs >= currentThreshold) {
                    val selectedPackets =
                        packetsList.subList(nextPacketIndexAfterSplitter, packetsList.size)
                    combineAndEmit(selectedPackets)
                    if (currentRecStep.splitter) {
                        if (strategy.sendTotalAtEnd)
                            nextPacketIndexAfterSplitter = packetsList.lastIndex + 1
                        else
                            packetsList.clear()
                    }
                    if (currentRecStepIndex == strategy.steps.lastIndex && strategy.sendTotalAtEnd) {
                        combineAndEmit(packetsList)
                    }
                    currentRecStepIndex++
                }
            }

    }
        .flowOn(defaultDispatcher)

    private suspend fun FlowCollector<Result<ByteArray>>.combineAndEmit(packets: List<ByteArray>) {
        val totalSize = packets.fold(0) { acc, array -> acc + array.size }
        val totalData = packets
            .fold(ByteBuffer.allocate(totalSize)) { acc, array -> acc.put(array) }.array()
        emit(Result.success(totalData))
    }

}
