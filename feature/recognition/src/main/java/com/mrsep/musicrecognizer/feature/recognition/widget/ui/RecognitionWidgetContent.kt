package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import com.mrsep.musicrecognizer.feature.recognition.R
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.widgetDefaultBackgroundRadius
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.widgetPadding
import com.mrsep.musicrecognizer.feature.recognition.widget.WidgetUiState
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout.Companion.dividerHorizontalPadding
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun RecognitionWidgetContent(
    layout: RecognitionWidgetLayout,
    uiState: WidgetUiState,
    onLaunchRecognition: Action,
    onCancelRecognition: Action,
    onWidgetClick: Action,
) {
    // Clickable modifier affects the shape of widget during smooth transition,
    // it's better when it is not placed on root view, so wrap it in box
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val systemBackgroundRadiusDefined = LocalContext.current.resources
                        .getResourceName(android.R.dimen.system_app_widget_background_radius) != null
                    if (systemBackgroundRadiusDefined) {
                        GlanceModifier
                            .background(UiR.color.surface_container_high)
                            .cornerRadius(android.R.dimen.system_app_widget_background_radius)
                    } else {
                        GlanceModifier
                            .background(UiR.color.surface_container_high)
                            .cornerRadius(widgetDefaultBackgroundRadius)
                    }
                } else {
                    GlanceModifier
                        .background(ImageProvider(R.drawable.widget_background_shape))
                }
            )
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(rippleOverride = -1, onClick = onWidgetClick)
                .padding(widgetPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WidgetMessagePart(uiState, layout)
            Spacer(GlanceModifier.width(dividerHorizontalPadding))
            VerticalDivider()
            Spacer(GlanceModifier.width(dividerHorizontalPadding))
            WidgetRecognizeButton(
                isNarrowLayout = layout.isNarrow,
                isRecognizing = uiState.status is RecognitionStatus.Recognizing,
                onLaunchRecognition = onLaunchRecognition,
                onCancelRecognition = onCancelRecognition
            )

        }
    }
}

@Composable
internal fun RowScope.WidgetMessagePart(uiState: WidgetUiState, layout: RecognitionWidgetLayout) {
    val context = LocalContext.current
    when (val recognitionStatus = uiState.status) {
        RecognitionStatus.Ready -> WidgetStatusInfo(
            title = context.getString(StringsR.string.tap_to_recognize_short),
            isNarrowLayout = layout.isNarrow,
        )

        is RecognitionStatus.Recognizing -> WidgetStatusInfo(
            title = context.getString(StringsR.string.listening),
            subtitle = if (recognitionStatus.extraTry) {
                context.getString(StringsR.string.trying_one_more_time)
            } else {
                context.getString(StringsR.string.please_wait)
            },
            isNarrowLayout = layout.isNarrow,
        )

        is RecognitionStatus.Done -> {
            when (recognitionStatus.result) {
                is RecognitionResult.Error -> when (recognitionStatus.result.remoteError) {
                    RemoteRecognitionResult.Error.ApiUsageLimited -> WidgetStatusInfo(
                        title = context.getString(StringsR.string.service_usage_limited),
                        subtitle = context.getSubtitle(recognitionStatus.result.recognitionTask)
                            ?: context.getString(StringsR.string.service_usage_limited_message),
                        isNarrowLayout = layout.isNarrow,
                    )

                    RemoteRecognitionResult.Error.AuthError -> WidgetStatusInfo(
                        title = context.getString(StringsR.string.auth_error),
                        subtitle = context.getSubtitle(recognitionStatus.result.recognitionTask)
                            ?: context.getString(StringsR.string.auth_error_message),
                        isNarrowLayout = layout.isNarrow,
                    )

                    RemoteRecognitionResult.Error.BadConnection -> WidgetStatusInfo(
                        title = context.getString(StringsR.string.bad_internet_connection),
                        subtitle = context.getSubtitle(recognitionStatus.result.recognitionTask)
                            ?: context.getString(StringsR.string.please_check_network_status),
                        isNarrowLayout = layout.isNarrow,
                    )

                    is RemoteRecognitionResult.Error.BadRecording -> WidgetStatusInfo(
                        title = context.getString(StringsR.string.recording_error),
                        subtitle = context.getSubtitle(recognitionStatus.result.recognitionTask)
                            ?: context.getString(StringsR.string.notification_message_recording_error),
                        isNarrowLayout = layout.isNarrow,
                    )

                    is RemoteRecognitionResult.Error.HttpError -> WidgetStatusInfo(
                        title = context.getString(StringsR.string.bad_network_response),
                        subtitle = context.getSubtitle(recognitionStatus.result.recognitionTask)
                            ?: context.getString(StringsR.string.message_http_error),
                        isNarrowLayout = layout.isNarrow,
                    )

                    is RemoteRecognitionResult.Error.UnhandledError -> WidgetStatusInfo(
                        title = context.getString(StringsR.string.internal_error),
                        subtitle = context.getSubtitle(recognitionStatus.result.recognitionTask)
                            ?: context.getString(StringsR.string.notification_message_unhandled_error),
                        isNarrowLayout = layout.isNarrow,
                    )
                }

                is RecognitionResult.NoMatches -> WidgetStatusInfo(
                    title = context.getString(StringsR.string.no_matches_found),
                    subtitle = context.getString(StringsR.string.tap_to_try_again_short),
                    isNarrowLayout = layout.isNarrow,
                )

                is RecognitionResult.ScheduledOffline -> WidgetStatusInfo(
                    title = context.getString(StringsR.string.recognition_scheduled),
                    subtitle = context.getSubtitle(recognitionStatus.result.recognitionTask),
                    isNarrowLayout = layout.isNarrow,
                )

                is RecognitionResult.Success -> WidgetTrackInfo(
                    layout = layout,
                    track = recognitionStatus.result.track,
                    artwork = uiState.artwork
                )
            }
        }
    }
}

private fun Context.getSubtitle(task: RecognitionTask): String? {
    return when (task) {
        is RecognitionTask.Created -> if (task.launched) {
            getString(StringsR.string.saved_recording_message)
        } else {
            getString(StringsR.string.saved_recording_message)
        }

        RecognitionTask.Ignored,
        is RecognitionTask.Error -> null
    }
}