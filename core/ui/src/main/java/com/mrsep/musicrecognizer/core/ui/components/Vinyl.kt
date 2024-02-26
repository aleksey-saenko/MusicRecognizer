package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer

// Vinyl record speed: 33 1/3 rpm, 1 rotation ~ 1800ms
private const val animationDuration = 1800

@Composable
fun VinylRotating(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VinylAnimation")
    val degreesAnimated by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "VinylRotation"

    )
    VinylStatic(
        modifier = modifier.graphicsLayer {
            rotationZ = degreesAnimated
        },
        color = color
    )
}

@Composable
fun VinylStatic(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(
        modifier = modifier
    ) {
        val totalSize = size.minDimension
        val surfaceThickness = size.minDimension * 0.33f
        val surfaceRadius = (totalSize - surfaceThickness) / 2

        val arcThickness = totalSize * 0.025f
        fun getArcTopLeft(arcDiameter: Float) = Offset(
            x = (size.width - arcDiameter) / 2,
            y = (size.height - arcDiameter) / 2
        )

        drawCircle(
            //base layer
            color = color.copy(alpha = 0.1f),
            radius = totalSize / 2,
        )
        drawCircle( //record surface
            color = color.copy(alpha = 0.4f),
            radius = surfaceRadius,
            style = Stroke(
                width = surfaceThickness
            )
        )
        drawCircle(
            //center dot
            color = color.copy(alpha = 0.3f),
            radius = totalSize / 25,
        )

        repeat(3) { index ->
            val arcDiameter = size.minDimension * 1f - arcThickness * ((index + 1) * 5)
            val arcTopLeft = getArcTopLeft(arcDiameter)

            val arcParams = when (index) {
                0 -> listOf(0f to 40f, 50f to 40f)
                else -> listOf(0f to 90f)
            }
            arcParams.forEach { (startAngle, sweepAngle) ->
                drawArc(
                    color = color.copy(alpha = 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(width = arcDiameter, height = arcDiameter),
                    style = Stroke(
                        width = arcThickness,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        repeat(3) { index ->
            val arcDiameter = size.minDimension * 1f - arcThickness * ((index + 1) * 5)
            val arcTopLeft = getArcTopLeft(arcDiameter)

            val arcParams = when (index) {
                0 -> listOf(180f to 90f)
                1 -> listOf(180f to 20f, 210f to 60f)
                else -> listOf(180f to 60f, 250f to 20f)
            }
            arcParams.forEach { (startAngle, sweepAngle) ->
                drawArc(
                    color = color.copy(alpha = 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(width = arcDiameter, height = arcDiameter),
                    style = Stroke(
                        width = arcThickness,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

    }
}