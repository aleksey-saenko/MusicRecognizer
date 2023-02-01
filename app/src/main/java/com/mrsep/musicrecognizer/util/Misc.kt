package com.mrsep.musicrecognizer.util

fun parseYear(date: String) = Regex("(17|18|19|20)\\d{2}").find(date)?.value ?: ""