package com.mrsep.musicrecognizer.core.common.util

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry

/**
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 *
 * This is used to de-duplicate navigation events.
 */
val NavBackStackEntry.lifecycleIsResumed get() =
    this.getLifecycle().currentState == Lifecycle.State.RESUMED