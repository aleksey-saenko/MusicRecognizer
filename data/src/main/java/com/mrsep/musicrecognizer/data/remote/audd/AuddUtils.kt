package com.mrsep.musicrecognizer.data.remote.audd

import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo


internal const val auddReturnParameterFull =
    "lyrics, spotify, apple_music, deezer, napster, musicbrainz"

//FIXME: consider removing building parameter (full json is more useful, and is not so heavy)
internal fun UserPreferencesDo.RequiredServicesDo.toAuddReturnParameter(): String {
    return auddReturnParameterFull
//    return "lyrics"
//        .plusIf(this.spotify, "spotify")
//        .plusIf(this.appleMusic, "apple_music")
//        .plusIf(this.deezer, "deezer")
//        .plusIf(this.napster, "napster")
//        .plusIf(this.musicbrainz, "musicbrainz")
}

private fun String.plusIf(conditional: Boolean, adjunct: String, separator: String = ",") =
    if (conditional) "$this$separator$adjunct" else this