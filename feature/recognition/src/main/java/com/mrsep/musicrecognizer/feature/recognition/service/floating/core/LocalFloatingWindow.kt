package com.mrsep.musicrecognizer.feature.recognition.service.floating.core

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal that provides access to the current [ComposeFloatingWindow] instance.
 *
 * @throws IllegalStateException if accessed outside of a ComposeFloatingWindow context.
 */
internal val LocalFloatingWindow: ProvidableCompositionLocal<ComposeFloatingWindow> =
    compositionLocalOf {
        error("CompositionLocal not present")
    }