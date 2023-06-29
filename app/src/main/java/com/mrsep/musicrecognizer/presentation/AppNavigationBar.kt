package com.mrsep.musicrecognizer.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .fillMaxWidth()
            .height(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        navBarDestList.forEach { destination ->

            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            val iconSize by animateDpAsState(if (selected) 32.dp else 26.dp)

            val bounceHeightAnimatable = remember { Animatable(0f) }

            LaunchedEffect(selected) {
                if (selected) {
                    bounceHeightAnimatable.animateTo(
                        targetValue = 0.5f,
                        animationSpec = tween(durationMillis = 200)
                    )
                    bounceHeightAnimatable.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 800, easing = EaseOutBounce)
                    )
                } else {
                    bounceHeightAnimatable.animateTo(
                        targetValue = 0.0f,
                        animationSpec = tween(durationMillis = 200)
                    )
                }
            }

            BarIconWithLine(
                icon = {
                    Icon(
                        painter = painterResource(destination.iconResId),
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.85f
                        ),
                        modifier = Modifier
                            .size(iconSize)
                            .offset(y = (-8).dp * bounceHeightAnimatable.value)
                    )
                },
                line = { BarLine(visible = selected) },
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.fillMaxHeight().weight(1f)
            )

        }
    }

}


@Composable
fun BarIconWithLine(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    line: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Box(
        modifier
            .clip(MaterialTheme.shapes.large)
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = rememberRipple(
                    bounded = false,
                    radius = 64.dp,
                    color = MaterialTheme.colorScheme.background
                ),
            ),
        contentAlignment = Alignment.Center
    ) {
        line()
        icon()
    }

}

@Composable
private fun BoxScope.BarLine(
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .width(64.dp)
            .height(4.dp)
            .align(Alignment.TopCenter)
    ) {
        val lineColorBack = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        Canvas(
            modifier = Modifier
        ) {
            val halfStrokeWidth = size.height / 2
            drawLine(
                brush = Brush.radialGradient(
                    listOf(
                        lineColor, lineColorBack
                    ),
                    radius = size.width
                ),
                start = Offset(x = halfStrokeWidth, y = halfStrokeWidth),
                end = Offset(x = size.width - halfStrokeWidth, y = halfStrokeWidth),
                cap = StrokeCap.Round,
                strokeWidth = size.height
            )
        }
    }
}