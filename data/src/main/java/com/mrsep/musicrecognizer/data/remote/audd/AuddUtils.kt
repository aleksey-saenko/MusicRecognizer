package com.mrsep.musicrecognizer.data.remote.audd

import com.mrsep.musicrecognizer.UserPreferencesProto

fun UserPreferencesProto.RequiredServicesProto.toAuddReturnParameter(): String {
    return "lyrics"
        .plusIf(this.spotify, "spotify")
        .plusIf(this.appleMusic, "apple_music")
        .plusIf(this.deezer, "deezer")
        .plusIf(this.napster, "napster")
        .plusIf(this.musicbrainz, "musicbrainz")
}

private fun String.plusIf(conditional: Boolean, adjunct: String, separator: String = ",") =
    if (conditional) "$this$separator$adjunct" else this