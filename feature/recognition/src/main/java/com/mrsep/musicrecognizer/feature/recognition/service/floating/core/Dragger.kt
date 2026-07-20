package com.mrsep.musicrecognizer.feature.recognition.service.floating.core

import android.view.Gravity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import com.mrsep.musicrecognizer.feature.recognition.service.floating.DismissWindowState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

internal data class WindowPhysicsConfig(
    val dampingRatio: Float = 1f,
    val stiffness: Float = 250f,
    val flingVelocityThreshold: Float = 2000f,
    val frictionMultiplier: Float = 1.5f
)

internal suspend fun PointerInputScope.detectFloatingWindowDrag(
    floatingWindow: ComposeFloatingWindow,
    config: WindowPhysicsConfig = WindowPhysicsConfig(),
    dismissWindowState: DismissWindowState,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: ((x: Int, y: Int) -> Unit)? = null,
    onPositionChanged: ((x: Int, y: Int) -> Unit)? = null
) {
    val velocityTracker = VelocityTracker()
    val touchSlop = viewConfiguration.touchSlop

    val animX = Animatable(0f)
    val animY = Animatable(0f)
    val magnetAnimatable = Animatable(0f)

    // Cache the display dimensions locally to detect orientation changes mid-animation
    var isRotationInterrupted = false
    var lastKnownDisplayWidth = floatingWindow.display.metrics.widthPixels
    var lastKnownDisplayHeight = floatingWindow.display.metrics.heightPixels

    coroutineScope {
        val animationScope = this

        // Source of truth for window coordinates (avoids async snapTo race conditions)
        var windowAbsX = 0f
        var windowAbsY = 0f

        // ----------------------------------------
        // CORE: UPDATE COORDINATES (HOT PATH)
        // ----------------------------------------
        fun updateWindowPosition(isDragEvent: Boolean) {
            if (isRotationInterrupted) return

            val displayWidth = floatingWindow.display.metrics.widthPixels
            val displayHeight = floatingWindow.display.metrics.heightPixels

            // --- ROTATION GUARD ---
            // If the screen size changed mid-animation or mid-drag, a rotation occurred.
            // We must instantly abort the animation loop to prevent overwriting the
            // newly calculated coordinates set by onConfigurationChanged callback
            if (displayWidth != lastKnownDisplayWidth || displayHeight != lastKnownDisplayHeight) {
                isRotationInterrupted = true
                // Force stop all active animations
                animationScope.launch {
                    animX.stop()
                    animY.stop()
                    magnetAnimatable.stop()
                }
                // Intercept this frame and exit early without updating WindowManager
                return
            }

            val weight = magnetAnimatable.value
            val gravity = floatingWindow.windowParams.gravity

            val windowWidth = floatingWindow.currentWindowWidth
            val windowHeight = floatingWindow.currentWindowHeight

            var targetCenterX = windowAbsX + windowWidth / 2f
            var targetCenterY = windowAbsY + windowHeight / 2f

            // --- TRASH: Hard Magnetic Snap ---
            if (dismissWindowState.isDismissWindowVisible) {
                targetCenterX += (dismissWindowState.trashIconCenterX - targetCenterX) * weight
                targetCenterY += (dismissWindowState.trashIconCenterY - targetCenterY) * weight
            }

            val finalTargetX = targetCenterX - windowWidth / 2f
            val finalTargetY = targetCenterY - windowHeight / 2f

            val maxOffsetX = (displayWidth - windowWidth).coerceAtLeast(0)
            val maxOffsetY = (displayHeight - windowHeight).coerceAtLeast(0)

            val isAffectedByMagnet = dismissWindowState.isTargetMagnetized || magnetAnimatable.isRunning

            // During active drag/magnetism, only physical screen bounds act as constraints
            val clampedAbsoluteX = if (isAffectedByMagnet) finalTargetX.roundToInt()
            else finalTargetX.roundToInt().coerceIn(0, maxOffsetX)

            val clampedAbsoluteY = if (isAffectedByMagnet) finalTargetY.roundToInt()
            else finalTargetY.roundToInt().coerceIn(0, maxOffsetY)

            val newX = convertXForGravityChange(
                x = clampedAbsoluteX,
                fromGravity = Gravity.LEFT,
                toGravity = gravity,
                displayWidth = displayWidth,
                windowWidth = windowWidth,
            )
            val newY = convertYForGravityChange(
                y = clampedAbsoluteY,
                fromGravity = Gravity.TOP,
                toGravity = gravity,
                displayHeight = displayHeight,
                windowHeight = windowHeight,
            )

            floatingWindow.updateCoordinate(newX, newY)
            onPositionChanged?.invoke(newX, newY)

            // Trigger the gesture callback only during manual drag, not during spring animations
            if (isDragEvent) {
                onDrag?.invoke(newX, newY)
            }
        }

        awaitPointerEventScope {
            while (true) {
                val down = awaitFirstDown(requireUnconsumed = false)

                // Reset rotation lock on new touch to resume physics in the new orientation
                isRotationInterrupted = false
                lastKnownDisplayWidth = floatingWindow.display.metrics.widthPixels
                lastKnownDisplayHeight = floatingWindow.display.metrics.heightPixels

                val startMotionEvent = currentEvent.motionEvent ?: continue

                val startRawX = startMotionEvent.rawX
                val startRawY = startMotionEvent.rawY
                // Cache the start event time immediately into a primitive
                // Android aggressively recycles MotionEvent instances to avoid allocations
                // Reading startMotionEvent.eventTime later inside the drag loop would return
                // the time of the *current* move event, resulting in dt=0 and infinite fling velocity
                val startEventTime = startMotionEvent.eventTime

                // Force stop any ongoing physics animations on touch down
                animationScope.launch { animX.stop() }
                animationScope.launch { animY.stop() }
                animationScope.launch { magnetAnimatable.stop() }

                animX.updateBounds(null, null)
                animY.updateBounds(null, null)

                val startWindowAbsX = convertXForGravityChange(
                    x = floatingWindow.windowParams.x,
                    fromGravity = floatingWindow.windowParams.gravity,
                    toGravity = Gravity.LEFT,
                    displayWidth = lastKnownDisplayWidth,
                    windowWidth = floatingWindow.currentWindowWidth,
                ).toFloat()

                val startWindowAbsY = convertYForGravityChange(
                    y = floatingWindow.windowParams.y,
                    fromGravity = floatingWindow.windowParams.gravity,
                    toGravity = Gravity.TOP,
                    displayHeight = lastKnownDisplayHeight,
                    windowHeight = floatingWindow.currentWindowHeight,
                ).toFloat()

                val grabOffsetX = startRawX - startWindowAbsX
                val grabOffsetY = startRawY - startWindowAbsY

                windowAbsX = startWindowAbsX
                windowAbsY = startWindowAbsY

                val pointerId = down.id
                var isNormalEnd = false
                var passedSlop = false

                // ----------------------------------------
                // DRAG LOOP: PROCESS FINGER MOVEMENT
                // ----------------------------------------
                try {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                        if (!change.pressed) {
                            // If it was a confirmed drag, consume the UP event so onClick doesn't fire
                            if (passedSlop) change.consume()
                            isNormalEnd = true
                            break
                        }

                        val mEvent = event.motionEvent ?: continue

                        val currentRawX = mEvent.rawX
                        val currentRawY = mEvent.rawY

                        // Prevents micro-movements during a click from triggering a drag
                        if (!passedSlop) {
                            val dx = currentRawX - startRawX
                            val dy = currentRawY - startRawY
                            val distance = hypot(dx, dy)

                            if (distance > touchSlop) {
                                passedSlop = true

                                onDragStart(Offset(down.position.x, down.position.y))

                                dismissWindowState.isDismissWindowVisible = true
                                dismissWindowState.isTargetMagnetized = false
                                animationScope.launch { magnetAnimatable.snapTo(0f) }

                                velocityTracker.resetTracking()
                                velocityTracker.addPosition(startEventTime, Offset(startRawX, startRawY))
                            }
                        }

                        if (passedSlop) {
                            change.consume()
                            velocityTracker.addPosition(mEvent.eventTime, Offset(currentRawX, currentRawY))

                            windowAbsX = currentRawX - grabOffsetX
                            windowAbsY = currentRawY - grabOffsetY

                            // --- TRASH: Check snap-in and breakout zones ---
                            if (dismissWindowState.isDismissWindowVisible) {
                                val windowWidth = floatingWindow.currentWindowWidth
                                val windowHeight = floatingWindow.currentWindowHeight

                                val rawWindowCenterX = windowAbsX + windowWidth / 2f
                                val rawWindowCenterY = windowAbsY + windowHeight / 2f

                                val dx = rawWindowCenterX - dismissWindowState.trashIconCenterX
                                val dy = rawWindowCenterY - dismissWindowState.trashIconCenterY
                                val distanceToTrash = hypot(dx, dy)

                                if (dismissWindowState.isTargetMagnetized) {
                                    if (distanceToTrash > dismissWindowState.breakoutRadius) {
                                        dismissWindowState.isTargetMagnetized = false
                                        animationScope.launch {
                                            magnetAnimatable.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(dampingRatio = 0.55f, stiffness = 450f)
                                            ) { updateWindowPosition(false) }
                                        }
                                    }
                                } else {
                                    if (distanceToTrash < dismissWindowState.magnetRadius) {
                                        dismissWindowState.isTargetMagnetized = true
                                        animationScope.launch {
                                            magnetAnimatable.animateTo(
                                                targetValue = 1f,
                                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                                            ) { updateWindowPosition(false) }
                                        }
                                    }
                                }
                            }

                            if (!magnetAnimatable.isRunning) {
                                updateWindowPosition(true)
                            }
                        }
                    }
                } catch (_: Exception) {
                    isNormalEnd = false
                }

                // ----------------------------------------
                // END DRAG: LAUNCH RELEASE / FLING PHYSICS
                // ----------------------------------------
                if (isNormalEnd) {
                    var vx = 0f
                    var vy = 0f

                    // If it was a real drag, process velocity and callbacks
                    if (passedSlop) {
                        val velocity = velocityTracker.calculateVelocity()
                        vx = velocity.x
                        vy = velocity.y

                        if (dismissWindowState.isTargetMagnetized) {
                            // Animate to full magnetization (centering in trash) before removal
                            animationScope.launch {
                                try {
                                    // Bring magnet weight to 1f for exact centering
                                    magnetAnimatable.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                                    ) {
                                        updateWindowPosition(false)
                                    }
                                } finally {
                                    // Trigger removal only after the animation completely finishes or gets canceled
                                    dismissWindowState.shouldRemoveTarget = true
                                    dismissWindowState.isDismissWindowVisible = false
                                    dismissWindowState.isTargetMagnetized = false
                                    onDragEnd()
                                }
                            }
                            continue // Skip fling/bounce physics since the window is captured by trash
                        }

                        // If released outside the magnet zone, hide trash and stop magnetization
                        dismissWindowState.isDismissWindowVisible = false
                        dismissWindowState.isTargetMagnetized = false
                        animationScope.launch { magnetAnimatable.stop() }
                        onDragEnd()
                    }

                    val displayWidth = floatingWindow.display.metrics.widthPixels
                    val displayHeight = floatingWindow.display.metrics.heightPixels
                    val windowWidth = floatingWindow.currentWindowWidth
                    val windowHeight = floatingWindow.currentWindowHeight

                    val physicalMaxX = (displayWidth - windowWidth).coerceAtLeast(0).toFloat()
                    val physicalMaxY = (displayHeight - windowHeight).coerceAtLeast(0).toFloat()

                    val realInsets = floatingWindow.display.safeInsets
                    val safeMinX = realInsets.left.toFloat()
                    val safeMaxX = (displayWidth - windowWidth - realInsets.right).coerceAtLeast(safeMinX.toInt()).toFloat()
                    val safeMinY = realInsets.top.toFloat()
                    val safeMaxY = (displayHeight - windowHeight - realInsets.bottom).coerceAtLeast(safeMinY.toInt()).toFloat()

                    // Strictly synchronize start coordinates with physical screen boundaries
                    windowAbsX = windowAbsX.coerceIn(0f, physicalMaxX)
                    windowAbsY = windowAbsY.coerceIn(0f, physicalMaxY)

                    val currentAbsX = windowAbsX

                    // Target X coordinate (always snaps to a safe edge)
                    val rawTargetX = when {
                        vx > config.flingVelocityThreshold -> safeMaxX
                        vx < -config.flingVelocityThreshold -> safeMinX
                        else -> if (currentAbsX > (safeMinX + safeMaxX) / 2f) safeMaxX else safeMinX
                    }
                    val targetAbsX = rawTargetX.coerceIn(safeMinX, safeMaxX)

                    val decay = exponentialDecay<Float>(frictionMultiplier = config.frictionMultiplier)

                    // --- LAUNCH X PHYSICS ---
                    animationScope.launch {
                        try {
                            // Phase 1: Strict physical boundaries for realistic bouncing
                            animX.updateBounds(0f, physicalMaxX)
                            animX.snapTo(windowAbsX)

                            var currentVx = vx
                            var xPhase1Finished = false

                            while (!xPhase1Finished) {
                                val res = animX.animateTo(
                                    targetValue = targetAbsX,
                                    initialVelocity = currentVx,
                                    animationSpec = spring(config.dampingRatio, config.stiffness)
                                ) {
                                    windowAbsX = value
                                    updateWindowPosition(false)
                                }

                                if (res.endReason == AnimationEndReason.BoundReached) {
                                    // Hit the physical edge -> hard bounce (lose 50% energy)
                                    currentVx = -res.endState.velocity * 0.5f
                                    if (abs(currentVx) < 150f) {
                                        xPhase1Finished = true // Energy depleted, move to phase 2
                                    }
                                } else {
                                    currentVx = 0f
                                    xPhase1Finished = true
                                }
                            }

                            // Phase 2: Remove bounds and let the spring smoothly pull into the Safe Zone (Insets)
                            animX.updateBounds(null, null)
                            if (abs(animX.value - targetAbsX) > 0.5f || abs(currentVx) > 10f) {
                                animX.animateTo(
                                    targetValue = targetAbsX,
                                    initialVelocity = currentVx,
                                    animationSpec = spring(config.dampingRatio, config.stiffness)
                                ) {
                                    windowAbsX = value
                                    updateWindowPosition(false)
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Suppress internal animation exceptions
                        }
                    }

                    // --- LAUNCH Y PHYSICS ---
                    animationScope.launch {
                        try {
                            // Predict target using standard decay based on throw velocity
                            val predictedY = decay.calculateTargetValue(windowAbsY, vy)

                            // Target must always be strictly within the safe zone
                            // If thrown too hard, the spring will just pull it back to the edge
                            val targetAbsY = predictedY.coerceIn(safeMinY, safeMaxY)

                            // Phase 1: Physical screen bounds for hard bounce
                            animY.updateBounds(0f, physicalMaxY)
                            animY.snapTo(windowAbsY)

                            var currentVy = vy
                            var yPhase1Finished = false

                            while (!yPhase1Finished) {
                                val res = animY.animateTo(
                                    targetValue = targetAbsY,
                                    initialVelocity = currentVy,
                                    animationSpec = spring(config.dampingRatio, config.stiffness)
                                ) {
                                    windowAbsY = value
                                    updateWindowPosition(false)
                                }

                                if (res.endReason == AnimationEndReason.BoundReached) {
                                    // Hit physical top/bottom edge -> hard bounce
                                    currentVy = -res.endState.velocity * 0.5f
                                    if (abs(currentVy) < 150f) {
                                        yPhase1Finished = true
                                    }
                                } else {
                                    currentVy = 0f
                                    yPhase1Finished = true
                                }
                            }

                            // Phase 2: Remove physical constraints so the spring can seamlessly
                            // pull the window out of the insets and into the safe zone
                            animY.updateBounds(null, null)

                            if (abs(animY.value - targetAbsY) > 0.5f || abs(currentVy) > 10f) {
                                animY.animateTo(
                                    targetValue = targetAbsY,
                                    initialVelocity = currentVy,
                                    animationSpec = spring(config.dampingRatio, config.stiffness)
                                ) {
                                    windowAbsY = value
                                    updateWindowPosition(false)
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // Suppress internal animation exceptions
                        }
                    }
                } else {
                    if (passedSlop) {
                        dismissWindowState.isDismissWindowVisible = false
                        dismissWindowState.isTargetMagnetized = false
                        animationScope.launch { magnetAnimatable.stop() }
                        onDragCancel()
                    }
                }
            }
        }
    }
}

@Composable
internal fun Modifier.draggableFloatingWindow(
    config: WindowPhysicsConfig = WindowPhysicsConfig(),
    dismissWindowState: DismissWindowState,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: ((Int, Int) -> Unit)? = null,
    onPositionChanged: ((Int, Int) -> Unit)? = null
): Modifier {
    val floatingWindow = LocalFloatingWindow.current
    return this.pointerInput(floatingWindow) {
        detectFloatingWindowDrag(
            floatingWindow = floatingWindow,
            config = config,
            dismissWindowState = dismissWindowState,
            onDragStart = onDragStart,
            onDragEnd = onDragEnd,
            onDragCancel = onDragCancel,
            onDrag = onDrag,
            onPositionChanged = onPositionChanged
        )
    }
}