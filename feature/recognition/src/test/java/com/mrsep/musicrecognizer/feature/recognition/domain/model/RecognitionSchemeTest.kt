package com.mrsep.musicrecognizer.feature.recognition.domain.model

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme.Companion.recognitionScheme
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme.Companion.splitter
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionScheme.Companion.step
import org.junit.Assert
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class RecognitionSchemeTest {

    @Test(expected = IllegalStateException::class)
    fun `test builder with wrong step order`() {
        recognitionScheme(true) {
            step(5.seconds)
            step(2.seconds)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `test builder with duplicated step`() {
        recognitionScheme(true) {
            step(5.seconds)
            step(5.seconds)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `test builder without any step`() {
        recognitionScheme(true) { }
    }

    @Test
    fun `test normal builder`() {
        recognitionScheme(true) {
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