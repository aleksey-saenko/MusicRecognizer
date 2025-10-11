package com.mrsep.musicrecognizer.core.domain.preferences

data class FallbackPolicy(
    val noMatches: FallbackAction,
    val badConnection: FallbackAction,
    val anotherFailure: FallbackAction,
) {
    val isFallbackRequired get() = noMatches.save || badConnection.save || anotherFailure.save
}
