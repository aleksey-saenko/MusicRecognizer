package com.mrsep.musicrecognizer.feature.backup.presentation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.mrsep.musicrecognizer.core.common.util.lifecycleIsResumed

object BackupRestoreScreenNavigation {

    const val ROUTE = "backup_restore"

    fun NavGraphBuilder.backupRestoreScreen(
        onBackPressed: () -> Unit
    ) {
        composable(ROUTE) { backStackEntry ->
            BackupRestoreScreen(
                onBackPressed = onBackPressed,
            )
        }
    }

    fun NavController.navigateToBackupRestoreScreen(
        from: NavBackStackEntry,
        navOptions: NavOptions? = null
    ) {
        if (from.lifecycleIsResumed) {
            this.navigate(route = ROUTE, navOptions = navOptions)
        }
    }
}