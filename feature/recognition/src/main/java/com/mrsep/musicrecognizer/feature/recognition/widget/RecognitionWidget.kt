package com.mrsep.musicrecognizer.feature.recognition.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.material3.ColorProviders
import com.mrsep.musicrecognizer.core.ui.theme.darkColorScheme
import com.mrsep.musicrecognizer.core.ui.theme.lightColorScheme
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionResult
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import com.mrsep.musicrecognizer.feature.recognition.presentation.ext.getCachedImageOrNull
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationService
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.NotificationServiceActivity
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetContent
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.coroutines.coroutineContext

class RecognitionWidget : GlanceAppWidget() {

    /* Unlike responsive mode, it makes building flex layouts easier
    * and does not keep bitmaps in memory for each size */
    override val sizeMode = SizeMode.Exact

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context, RecognitionWidgetEntryPoint::class.java
        )
        val router = hiltEntryPoint.serviceRouter()
        val widgetStatusHolder = hiltEntryPoint.widgetStatusHolder()
        val glanceCoroutineContext = coroutineContext + Dispatchers.Default

        val statusFlow = widgetStatusHolder.status.onEach { status ->
            when (status) {
                RecognitionStatus.Ready -> {} // NO-OP

                is RecognitionStatus.Recognizing -> ResetWidgetStatusWorker.cancel(context)
                is RecognitionStatus.Done -> ResetWidgetStatusWorker.enqueue(context)
            }
        }.stateIn(scope = CoroutineScope(glanceCoroutineContext))

        /* Glance can't round image corners prior to API 31
        * so we must do it on-demand based on the required image size */
        fun widgetUiFlow(widgetLayout: RecognitionWidgetLayout) = statusFlow.mapLatest { status ->
            WidgetUiState(
                status = status,
                artwork = if (widgetLayout.showArtwork &&
                    status is RecognitionStatus.Done
                    && status.result is RecognitionResult.Success
                ) {
                    status.result.track.artworkUrl?.let { artworkUrl ->
                        context.getCachedImageOrNull(
                            url = artworkUrl,
                            widthPx = widgetLayout.artworkSizePx,
                            heightPx = widgetLayout.artworkSizePx,
                            cornerRadiusPx = widgetLayout.artworkCornerRadiusPx,
                        )
                    }
                } else {
                    null
                }
            )
        }.flowOn(Dispatchers.Default)

        provideContent {
            val widgetLayout = RecognitionWidgetLayout.fromLocalSize()
            val widgetUiState by widgetUiFlow(widgetLayout).collectAsState(
                initial = WidgetUiState(
                    status = RecognitionStatus.Ready,
                    artwork = null
                )
            )
            GlanceTheme(
                // enable dynamic colors if available
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    GlanceTheme.colors
                } else {
                    ColorProviders(light = lightColorScheme, dark = darkColorScheme)
                }
            ) {
                RecognitionWidgetContent(
                    layout = widgetLayout,
                    uiState = widgetUiState,
                    onLaunchRecognition = launchRecognitionAction(context),
                    onCancelRecognition = cancelRecognitionAction(context),
                    onWidgetClick = when (val state = widgetUiState.status) {
                        RecognitionStatus.Ready -> launchRecognitionAction(context)

                        is RecognitionStatus.Recognizing -> cancelRecognitionAction(context)

                        is RecognitionStatus.Done -> when (state.result) {
                            is RecognitionResult.Success -> actionStartActivity(
                                intent = router.getDeepLinkIntentToTrack(state.result.track.id)
                            )

                            is RecognitionResult.NoMatches -> launchRecognitionAction(context)

                            is RecognitionResult.ScheduledOffline,
                            is RecognitionResult.Error -> actionRunCallback<ResetWidgetFinalState>()
                        }
                    }

                )
            }
        }
    }

    private fun launchRecognitionAction(context: Context) = actionStartActivity(
        intent = Intent(context, NotificationServiceActivity::class.java)
            .setAction(NotificationService.LAUNCH_RECOGNITION_ACTION)
            .putExtra(NotificationService.KEY_FOREGROUND_REQUESTED, true)
    )

    private fun cancelRecognitionAction(context: Context) = actionStartActivity(
        intent = Intent(context, NotificationServiceActivity::class.java)
            .setAction(NotificationService.CANCEL_RECOGNITION_ACTION)
    )
}

internal class ResetWidgetFinalState : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context, RecognitionWidgetEntryPoint::class.java
        )
        val widgetStatusHolder = hiltEntryPoint.widgetStatusHolder()
        widgetStatusHolder.resetFinalStatus()
        RecognitionWidget().updateAll(context)
    }
}