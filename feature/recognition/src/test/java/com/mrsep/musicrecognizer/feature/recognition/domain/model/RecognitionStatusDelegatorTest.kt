package com.mrsep.musicrecognizer.feature.recognition.domain.model

import app.cash.turbine.test
import com.mrsep.musicrecognizer.feature.recognition.domain.impl.RecognitionStatusDelegator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecognitionStatusDelegatorTest {

    private val readyStatus = RecognitionStatus.Ready
    private val ongoingStatus = RecognitionStatus.Recognizing(true)
    private val finalStatus = RecognitionStatus.Done(
        RecognitionResult.ScheduledOffline(RecognitionTask.Ignored)
    )

    @Test()
    fun `screen and service observers are active`() = runTest {
        val delegator = RecognitionStatusDelegator()
        launch {
            delegator.screenState.test {
                assertEquals(readyStatus, awaitItem()) //initial status for delegator
                assertEquals(ongoingStatus, awaitItem())
                assertEquals(finalStatus, awaitItem())
                assertEquals(ongoingStatus, awaitItem())
                delay(60_000)
                expectNoEvents()
            }
        }
        launch {
            delegator.serviceState.test {
                assertEquals(readyStatus, awaitItem()) //initial status for delegator
                assertEquals(ongoingStatus, awaitItem())
                assertEquals(readyStatus, awaitItem()) //receive ready as screen is observed
                assertEquals(ongoingStatus, awaitItem())
                delay(60_000)
                expectNoEvents()
            }
        }
        delay(3000)
        delegator.notify(ongoingStatus)
        delay(3000)
        delegator.notify(finalStatus)
        delay(3000)
        delegator.notify(ongoingStatus)
        advanceUntilIdle()
    }

    @Test()
    fun `only service observer are active`() = runTest {
        val delegator = RecognitionStatusDelegator()
        launch {
            delegator.serviceState.test {
                assertEquals(readyStatus, awaitItem()) //initial status for delegator
                assertEquals(ongoingStatus, awaitItem())
                assertEquals(finalStatus, awaitItem()) //receive done as screen is not observed
                assertEquals(ongoingStatus, awaitItem())
                delay(60_000)
                expectNoEvents()
            }
        }
        delay(3000)
        delegator.notify(ongoingStatus)
        delay(3000)
        delegator.notify(finalStatus)
        delay(3000)
        delegator.notify(ongoingStatus)
        advanceUntilIdle()
    }

}