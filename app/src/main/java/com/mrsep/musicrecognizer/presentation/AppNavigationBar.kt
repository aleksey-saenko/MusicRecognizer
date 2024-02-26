package com.mrsep.musicrecognizer.presentation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
    NavigationBar(
        modifier = modifier
    ) {
        TopLevelDestination.entries.forEach { destinationEntry ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == destinationEntry.route } == true
            NavigationBarItem(
                selected = selected,
                icon = {
                    if (selected) {
                        Icon(
                            painter = painterResource(destinationEntry.selectedIconResId),
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            painter = painterResource(destinationEntry.unselectedIconResId),
                            contentDescription = null
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(destinationEntry.titleResId),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = false,
                onClick = {
                    navController.navigate(destinationEntry.route) {
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