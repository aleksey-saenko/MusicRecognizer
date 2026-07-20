package com.mrsep.musicrecognizer.feature.recognition.service.floating.core

import android.view.Gravity

internal enum class WindowSide {
    LEFT,
    RIGHT,
}

internal data class WindowSnapState(
    val side: WindowSide,
    val fractionY: Float
) {
    init {
        check(fractionY in 0f..1f)
    }
}

/** Determines whether the window is currently on the left or right half of the screen. */
internal fun calculateWindowSide(
    currentX: Int,
    currentGravity: Int,
    displayWidth: Int,
    windowWidth: Int
): WindowSide {
    val baseX = convertXForGravityChange(
        x = currentX,
        fromGravity = currentGravity,
        toGravity = Gravity.LEFT,
        displayWidth = displayWidth,
        windowWidth = windowWidth
    )
    val maxX = maxOf(1, displayWidth - windowWidth)
    return if (baseX < (maxX / 2)) WindowSide.LEFT else WindowSide.RIGHT
}

/** Calculates the complete snap state of the window (physical side and height fraction). */
internal fun calculateWindowSnapState(
    currentX: Int,
    currentY: Int,
    currentGravity: Int,
    displayWidth: Int,
    displayHeight: Int,
    windowWidth: Int,
    windowHeight: Int
): WindowSnapState {
    val side = calculateWindowSide(
        currentX = currentX,
        currentGravity = currentGravity,
        displayWidth = displayWidth,
        windowWidth = windowWidth
    )
    val baseY = convertYForGravityChange(
        y = currentY,
        fromGravity = currentGravity,
        toGravity = Gravity.TOP,
        displayHeight = displayHeight,
        windowHeight = windowHeight
    )
    val maxY = maxOf(1, displayHeight - windowHeight)
    val fractionY = (baseY.toFloat() / maxY).coerceIn(0f, 1f)
    return WindowSnapState(side, fractionY)
}

/** Calculates the final X and Y coordinates to be set in WindowParams based on the snap state. */
internal fun calculateWindowPosition(
    snapState: WindowSnapState,
    targetGravity: Int,
    displayWidth: Int,
    displayHeight: Int,
    windowWidth: Int,
    windowHeight: Int
): Pair<Int, Int> {
    val newMaxX = maxOf(1, displayWidth - windowWidth)
    val newMaxY = maxOf(1, displayHeight - windowHeight)

    val newBaseX = if (snapState.side == WindowSide.LEFT) 0 else newMaxX
    val newBaseY = (newMaxY * snapState.fractionY).toInt().coerceIn(0, newMaxY)

    val finalX = convertXForGravityChange(
        x = newBaseX,
        fromGravity = Gravity.LEFT,
        toGravity = targetGravity,
        displayWidth = displayWidth,
        windowWidth = windowWidth
    )
    val finalY = convertYForGravityChange(
        y = newBaseY,
        fromGravity = Gravity.TOP,
        toGravity = targetGravity,
        displayHeight = displayHeight,
        windowHeight = windowHeight
    )
    return Pair(finalX, finalY)
}
