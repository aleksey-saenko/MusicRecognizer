package com.mrsep.musicrecognizer.feature.library.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed class SearchResultUi {
    abstract val keyword: String

    data class Pending(
        override val keyword: String
    ) : SearchResultUi()

    data class Success(
        override val keyword: String,
        val data: ImmutableList<TrackUi>
    ) : SearchResultUi() {
        val isEmpty get() = data.isEmpty()
    }

}