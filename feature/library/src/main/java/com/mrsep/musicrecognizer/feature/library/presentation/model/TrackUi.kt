package com.mrsep.musicrecognizer.feature.library.presentation.model

import javax.annotation.concurrent.Immutable

@Immutable
data class TrackUi(
    val mbId: String,
    val title: String,
    val artist: String,
    val albumAndYear: String?,
    val artworkUrl: String?
)