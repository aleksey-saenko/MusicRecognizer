package com.mrsep.musicrecognizer.feature.recognition.presentation.model

import androidx.compose.runtime.Immutable
import com.mrsep.musicrecognizer.core.audio.audioplayer.PlayerStatus

@Immutable
internal sealed class PlayerStatusUi {

    data object Idle : PlayerStatusUi()

    data class Started(
        val id: Int,
        val durationMillis: Int
    ) : PlayerStatusUi()

    data class Paused(
        val id: Int,
        val durationMillis: Int
    ) : PlayerStatusUi()

    data class Error(
        val id: Int,
        val message: String
    ) : PlayerStatusUi()
}

internal fun PlayerStatus.toUi() = when (this) {
    PlayerStatus.Idle -> PlayerStatusUi.Idle
    is PlayerStatus.Started -> PlayerStatusUi.Started(
        id = id,
        durationMillis = duration.inWholeMilliseconds.toInt()
    )
    is PlayerStatus.Paused -> PlayerStatusUi.Paused(
        id = id,
        durationMillis = duration.inWholeMilliseconds.toInt()
    )
    is PlayerStatus.Error -> PlayerStatusUi.Error(
        id = id,
        message = message
    )
}
