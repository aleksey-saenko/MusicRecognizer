package com.mrsep.musicrecognizer.feature.recognition.service.floating.core

import android.view.Gravity

internal enum class HorizontalAnchor {
    LEFT,
    CENTER,
    RIGHT,
}

internal enum class VerticalAnchor {
    TOP,
    CENTER,
    BOTTOM,
}

internal fun resolveHorizontalAnchor(gravity: Int): HorizontalAnchor {
    val horizontalGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
    return when (horizontalGravity) {
        Gravity.RIGHT -> HorizontalAnchor.RIGHT
        Gravity.CENTER_HORIZONTAL -> HorizontalAnchor.CENTER
        else -> HorizontalAnchor.LEFT
    }
}

internal fun resolveVerticalAnchor(gravity: Int): VerticalAnchor {
    val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
    return when (verticalGravity) {
        Gravity.BOTTOM -> VerticalAnchor.BOTTOM
        Gravity.CENTER_VERTICAL -> VerticalAnchor.CENTER
        else -> VerticalAnchor.TOP
    }
}

internal fun convertXForGravityChange(
    x: Int,
    fromGravity: Int,
    toGravity: Int,
    displayWidth: Int,
    windowWidth: Int,
): Int {
    val fromAnchor = resolveHorizontalAnchor(fromGravity)
    val toAnchor = resolveHorizontalAnchor(toGravity)

    val absoluteLeft = when (fromAnchor) {
        HorizontalAnchor.LEFT -> x
        HorizontalAnchor.RIGHT -> displayWidth - windowWidth - x
        HorizontalAnchor.CENTER -> (displayWidth - windowWidth) / 2 + x
    }

    return when (toAnchor) {
        HorizontalAnchor.LEFT -> absoluteLeft
        HorizontalAnchor.RIGHT -> displayWidth - windowWidth - absoluteLeft
        HorizontalAnchor.CENTER -> absoluteLeft - (displayWidth - windowWidth) / 2
    }
}

internal fun convertYForGravityChange(
    y: Int,
    fromGravity: Int,
    toGravity: Int,
    displayHeight: Int,
    windowHeight: Int,
): Int {
    val fromAnchor = resolveVerticalAnchor(fromGravity)
    val toAnchor = resolveVerticalAnchor(toGravity)

    val absoluteTop = when (fromAnchor) {
        VerticalAnchor.TOP -> y
        VerticalAnchor.BOTTOM -> displayHeight - windowHeight - y
        VerticalAnchor.CENTER -> (displayHeight - windowHeight) / 2 + y
    }

    return when (toAnchor) {
        VerticalAnchor.TOP -> absoluteTop
        VerticalAnchor.BOTTOM -> displayHeight - windowHeight - absoluteTop
        VerticalAnchor.CENTER -> absoluteTop - (displayHeight - windowHeight) / 2
    }
}
