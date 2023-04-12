package com.mrsep.musicrecognizer.presentation.navigationbar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavigationBarDefault(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
//        containerColor = Color.Unspecified,
//        contentColor = MaterialTheme.colorScheme.onBackground,
//        tonalElevation = 0.dp,
//            modifier = modifier.height(64.dp),
            modifier = modifier
                .height(64.dp)
//                .width(256.dp)
//                .background(
//                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
//                    shape = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50)
//                ),
    ) {
        navBarDestList.forEach { destination ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == destination.route } == true
            val iconSize by animateDpAsState(if (selected) 32.dp else 26.dp)
            NavigationBarItem(
                modifier = Modifier,
//                label = { Text(stringResource(id = destination.titleId)) },
//                alwaysShowLabel = false,
                icon = {
                    Icon(
                        painter = painterResource(
                            if (selected) destination.selectedIconId else destination.unselectedIconId
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
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
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.surfaceColorAtElevation(LocalAbsoluteTonalElevation.current),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

        }
    }

}