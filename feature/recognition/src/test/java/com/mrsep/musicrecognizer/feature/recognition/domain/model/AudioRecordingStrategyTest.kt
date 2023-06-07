package com.mrsep.musicrecognizer.feature.recognition.domain.model

import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy.Companion.audioRecognitionStrategy
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy.Companion.splitter
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AudioRecordingStrategy.Companion.step
import org.junit.Assert
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class AudioRecordingStrategyTest {

    @Test(expected = IllegalStateException::class)
    fun `test builder with wrong step order`() {
        audioRecognitionStrategy(true) {
            step(5.seconds)
            step(2.seconds)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `test builder with duplicated step`() {
        audioRecognitionStrategy(true) {
            step(5.seconds)
            step(5.seconds)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `test builder without any step`() {
        audioRecognitionStrategy(true) { }
    }

    @Test
    fun `test normal builder`() {
        audioRecognitionStrategy(true) {
            step(5.seconds)
            step(7.seconds)
            splitter(true)
            step(9.seconds)
        }.run {
            Assert.assertTrue(steps.size == 3)
            Assert.assertFalse(steps[0].splitter)
            Assert.assertTrue(steps[1].splitter)
            Assert.assertFalse(steps[2].splitter)
            Assert.assertTrue(sendTotalAtEnd)
        }
    }

}