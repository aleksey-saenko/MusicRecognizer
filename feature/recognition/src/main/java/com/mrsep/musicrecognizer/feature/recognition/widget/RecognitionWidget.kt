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
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlService
import com.mrsep.musicrecognizer.feature.recognition.presentation.service.RecognitionControlActivity
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.CircleLayoutContent
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.HorizontalLayoutContent
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.RecognitionWidgetLayout
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.SquareLayoutContent
import com.mrsep.musicrecognizer.feature.recognition.widget.ui.VerticalLayoutContent
import com.mrsep.musicrecognizer.feature.recognition.widget.util.ImageUtils.getWidgetArtworkOrNull
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
            context,
            RecognitionWidgetEntryPoint::class.java
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
                    status is RecognitionStatus.Done &&
                    status.result is RecognitionResult.Success
                ) {
                    status.result.track.artworkUrl?.let { artworkUrl ->
                        context.getWidgetArtworkOrNull(
                            url = artworkUrl,
                            widthPx = widgetLayout.artworkSizePx,
                            heightPx = widgetLayout.artworkSizePx,
                            artworkStyle = widgetLayout.artworkStyle
                        )
                    }
                } else {
                    null
                }
            )
        }.flowOn(Dispatchers.Default)

        val onLaunchRecognition = actionStartActivity(
            intent = Intent(context, RecognitionControlActivity::class.java)
                .setAction(RecognitionControlService.ACTION_LAUNCH_RECOGNITION)
                .putExtra(RecognitionControlService.KEY_FOREGROUND_REQUESTED, true)
        )
        val onCancelRecognition = actionStartActivity(
            intent = Intent(context, RecognitionControlActivity::class.java)
                .setAction(RecognitionControlService.ACTION_CANCEL_RECOGNITION)
        )

        provideContent {
            val widgetLayout = RecognitionWidgetLayout.fromLocalSize()
            val widgetUiState by widgetUiFlow(widgetLayout).collectAsState(
                initial = WidgetUiState(
                    status = RecognitionStatus.Ready,
                    artwork = null
                )
            )
            val onWidgetClick = when (val state = widgetUiState.status) {
                RecognitionStatus.Ready -> onLaunchRecognition

                is RecognitionStatus.Recognizing -> onCancelRecognition

                is RecognitionStatus.Done -> when (state.result) {
                    is RecognitionResult.Success -> actionStartActivity(
                        intent = router.getDeepLinkIntentToTrack(state.result.track.id)
                    )

                    is RecognitionResult.NoMatches -> onLaunchRecognition

                    is RecognitionResult.ScheduledOffline,
                    is RecognitionResult.Error,
                    -> actionRunCallback<ResetWidgetFinalState>()
                }
            }
            GlanceTheme(
                // Enable dynamic colors if available
                colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    GlanceTheme.colors
                } else {
                    ColorProviders(light = lightColorScheme, dark = darkColorScheme)
                }
            ) {
                when (widgetLayout) {
                    is RecognitionWidgetLayout.Circle -> CircleLayoutContent(
                        layout = widgetLayout,
                        uiState = widgetUiState,
                        onLaunchRecognition = onLaunchRecognition,
                        onCancelRecognition = onCancelRecognition,
                        onWidgetClick = onWidgetClick
                    )
                    is RecognitionWidgetLayout.Square -> SquareLayoutContent(
                        layout = widgetLayout,
                        uiState = widgetUiState,
                        onLaunchRecognition = onLaunchRecognition,
                        onCancelRecognition = onCancelRecognition,
                        onWidgetClick = onWidgetClick
                    )

                    is RecognitionWidgetLayout.Horizontal -> HorizontalLayoutContent(
                        layout = widgetLayout,
                        uiState = widgetUiState,
                        onLaunchRecognition = onLaunchRecognition,
                        onCancelRecognition = onCancelRecognition,
                        onWidgetClick = onWidgetClick
                    )

                    is RecognitionWidgetLayout.Vertical -> VerticalLayoutContent(
                        layout = widgetLayout,
                        uiState = widgetUiState,
                        onLaunchRecognition = onLaunchRecognition,
                        onCancelRecognition = onCancelRecognition,
                        onWidgetClick = onWidgetClick
                    )
                }
            }
        }
    }
}

internal class ResetWidgetFinalState : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context,
            RecognitionWidgetEntryPoint::class.java
        )
        val widgetStatusHolder = hiltEntryPoint.widgetStatusHolder()
        widgetStatusHolder.resetFinalStatus()
        RecognitionWidget().updateAll(context)
    }
}
