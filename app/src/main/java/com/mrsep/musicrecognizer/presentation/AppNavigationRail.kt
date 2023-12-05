package com.mrsep.musicrecognizer.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
fun AppNavigationRail(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .fillMaxHeight()
            .width(64.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopLevelDestination.entries.forEach { destination ->

            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            val iconSize by animateDpAsState(if (selected) 32.dp else 26.dp, label = "IconSize")

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
                        contentDescription = stringResource(
                            StringsR.string.format_navigate_to_screen,
                            stringResource(destination.titleResId)
                        ),
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        },
                        modifier = Modifier
                            .size(iconSize)
                            .offset(y = (-8).dp * bounceHeightAnimatable.value)
                    )
                },
                line = { RailLine(visible = selected) },
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
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

        }
    }

}


@Composable
private fun BoxScope.RailLine(
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .width(4.dp)
            .height(64.dp)
            .align(Alignment.CenterStart)
    ) {
        val lineColorBack = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        Canvas(
            modifier = Modifier
        ) {
            val halfStrokeWidth = size.width / 2
            drawLine(
                brush = Brush.radialGradient(
                    listOf(
                        lineColor, lineColorBack
                    ),
                    radius = size.height
                ),
                start = Offset(x = halfStrokeWidth, y = halfStrokeWidth),
                end = Offset(x = halfStrokeWidth, y = size.height - halfStrokeWidth),
                cap = StrokeCap.Round,
                strokeWidth = size.width
            )
        }
    }
}