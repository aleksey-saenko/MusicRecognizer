package com.mrsep.musicrecognizer.feature.recognition.widget

import android.content.Context
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
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionResult
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionStatus
import com.mrsep.musicrecognizer.core.ui.theme.darkColorScheme
import com.mrsep.musicrecognizer.core.ui.theme.lightColorScheme
import com.mrsep.musicrecognizer.feature.recognition.service.RecognitionControlService
import com.mrsep.musicrecognizer.feature.recognition.service.RecognitionControlActivity
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
            val artwork = when (status) {
                is RecognitionStatus.Done -> when (val result = status.result) {
                    is RecognitionResult.Success -> result.track.artworkUrl
                        ?.takeIf { widgetLayout.showArtwork }
                        ?.let { artworkUrl ->
                            context.getWidgetArtworkOrNull(
                                url = artworkUrl,
                                widthPx = widgetLayout.artworkSizePx,
                                heightPx = widgetLayout.artworkSizePx,
                                artworkStyle = widgetLayout.artworkStyle
                            )
                    }
                    else -> null
                }
                else -> null
            }
            WidgetUiState(status = status, artwork = artwork)
        }.flowOn(Dispatchers.Default)

        val onLaunchRecognition = actionStartActivity(
            RecognitionControlActivity.startRecognitionWithPermissionRequestIntent(context)
        )
        val onCancelRecognition = actionRunCallback<CancelRecognition>()

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

                is RecognitionStatus.Done -> when (val result = state.result) {
                    is RecognitionResult.Success -> actionStartActivity(
                        intent = router.getDeepLinkIntentToTrack(result.track.id)
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

internal class CancelRecognition : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        RecognitionControlService.cancelRecognition(context)
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
