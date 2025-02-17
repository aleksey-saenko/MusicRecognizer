package com.mrsep.musicrecognizer.feature.recognition.presentation.model

import androidx.compose.runtime.Immutable
import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatter
import com.mrsep.musicrecognizer.core.domain.recognition.model.EnqueuedRecognitionWithStatus
import com.mrsep.musicrecognizer.core.domain.recognition.model.ScheduledJobStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Immutable
internal data class EnqueuedRecognitionUi(
    val id: Int,
    val title: String,
    val creationDateShort: String,
    val creationDateLong: String,
    val status: ScheduledJobStatus,
    val result: RemoteRecognitionResultUi?,
    val resultDateLong: String?
)

internal fun EnqueuedRecognitionWithStatus.toUi(
    dateTimeFormatter: AppDateTimeFormatter
): EnqueuedRecognitionUi {
    val creationZonedTime = enqueued.creationDate.atZone(ZoneId.systemDefault())
    return EnqueuedRecognitionUi(
        id = enqueued.id,
        title = enqueued.title,
        creationDateShort = dateTimeFormatter.formatRelativeToToday(creationZonedTime),
        creationDateLong = creationZonedTime.format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        ),
        status = status,
        result = enqueued.result?.toUi(),
        resultDateLong = enqueued.resultDate?.atZone(ZoneId.systemDefault())?.format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        ),
    )
}
