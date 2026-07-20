package com.mrsep.musicrecognizer.feature.recognition.service.floating

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
internal class DismissWindowState {
    var isDismissWindowVisible: Boolean by mutableStateOf(false)

    var trashIconCenterX: Float by mutableFloatStateOf(0f)
    var trashIconCenterY: Float by mutableFloatStateOf(0f)

    var magnetRadius: Float by mutableFloatStateOf(0f)
    var breakoutRadius: Float by mutableFloatStateOf(0f)

    var isTargetMagnetized: Boolean by mutableStateOf(false)
    var shouldRemoveTarget: Boolean by mutableStateOf(false)
}
