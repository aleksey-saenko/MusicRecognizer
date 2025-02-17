package com.mrsep.musicrecognizer.feature.recognition.widget

import android.graphics.Bitmap
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus

internal data class WidgetUiState(
    val status: RecognitionStatus,
    val artwork: Bitmap?
)
