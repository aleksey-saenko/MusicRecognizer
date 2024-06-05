package com.mrsep.musicrecognizer.core.common.util

import android.content.Context
import android.text.format.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import javax.inject.Inject

interface AppDateTimeFormatter {

    fun formatRelativeToToday(dateTime: ZonedDateTime): String
}

internal class AppDateTimeFormatterImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : AppDateTimeFormatter {

    override fun formatRelativeToToday(dateTime: ZonedDateTime): String {
        val now = ZonedDateTime.now()
        val isSameYear = now.year == dateTime.year
        val isSameDay = now.dayOfYear == dateTime.dayOfYear
        val format = when {
            isSameYear && isSameDay -> DateUtils.FORMAT_SHOW_TIME
            isSameYear -> DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_NO_YEAR
            else -> DateUtils.FORMAT_ABBREV_ALL
        }
        return DateUtils.formatDateTime(
            appContext,
            dateTime.toInstant().toEpochMilli(),
            format
        )
    }
}
