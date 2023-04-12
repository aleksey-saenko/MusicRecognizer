package com.mrsep.musicrecognizer.core.ui.util

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.platform.debugInspectorInfo
import kotlinx.coroutines.delay

@Stable
fun Modifier.recompositionCounter(callingName: String): Modifier = this.then(
    Modifier.composed(
        inspectorInfo = debugInspectorInfo { name = "recompositionCounter: + $callingName" }
    ) {
        // The total number of compositions that have occurred. We're not using a State<> here be
        // able to read/write the value without invalidating (which would cause infinite
        // recomposition).
        val totalCompositions = remember { arrayOf(0L) }
        totalCompositions[0]++

        // The value of totalCompositions at the last timeout.
        val totalCompositionsAtLastTimeout = remember { mutableStateOf(0L) }

        // Start the timeout, and reset everytime there's a recomposition. (Using totalCompositions
        // as the key is really just to cause the timer to restart every composition).
        LaunchedEffect(totalCompositions[0]) {
            delay(3000)
            totalCompositionsAtLastTimeout.value = totalCompositions[0]
        }


        Modifier.drawWithCache {
            onDrawWithContent {
                // Below is to draw the highlight, if necessary. A lot of the logic is copied from
                // Modifier.border
                val numCompositionsSinceTimeout =
                    totalCompositions[0] - totalCompositionsAtLastTimeout.value
                if (numCompositionsSinceTimeout > 0) {
                    println("recompositionCounter: $callingName - $numCompositionsSinceTimeout")
                }

                // Draw actual content.
                drawContent()
            }
        }
    }
)