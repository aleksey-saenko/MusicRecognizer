package com.mrsep.musicrecognizer.presentation.common

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mrsep.musicrecognizer.presentation.navBarDestList

@Composable
fun NavigationBarCustom(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
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

            val bounceHeightAnimatable by remember { mutableStateOf(Animatable(0f)) }

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

            CustomNavBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            if (selected) destination.selectedIconId else destination.unselectedIconId
                        ),
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        ),
                        modifier = Modifier
                            .size(iconSize)
                            .offset(y = (-8).dp * bounceHeightAnimatable.value)
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

        }
    }

}


@Composable
private fun RowScope.CustomNavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
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
            )
            .weight(1f)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        this@CustomNavBarItem.AnimatedVisibility(
            visible = selected,
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
                val halfWidth = size.height / 2
                drawLine(
                    brush = Brush.radialGradient(
                        listOf(
                            lineColor, lineColorBack
                        ),
                        radius = size.width
                    ),
                    start = Offset(x = 0f + halfWidth, y = halfWidth),
                    end = Offset(x = size.width - halfWidth, y = halfWidth),
                    cap = StrokeCap.Round,
                    strokeWidth = size.height
                )
            }
        }
        icon()

    }

}