package com.mrsep.musicrecognizer.feature.recognition.presentation.model

import androidx.compose.runtime.Immutable
import com.mrsep.musicrecognizer.feature.recognition.domain.model.Track

@Immutable
internal data class TrackUi(
    val id: String,
)

internal fun Track.toUi() = TrackUi(id = id)
