package com.mrsep.musicrecognizer.feature.recognition.service.floating

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Gravity
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.ComposeFloatingWindow
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.WindowEnterAnimation
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.WindowExitAnimation
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.calculateWindowPosition
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.calculateWindowSnapState
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.defaultLayoutParams
import com.mrsep.musicrecognizer.core.domain.preferences.PreferencesRepository
import com.mrsep.musicrecognizer.core.domain.recognition.TrackMetadataFetchManager
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.feature.recognition.DeeplinkRouter
import com.mrsep.musicrecognizer.feature.recognition.RecognitionStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.di.FloatingButtonStatusHolder
import com.mrsep.musicrecognizer.feature.recognition.platform.VibrationManager
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.WindowSide
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.WindowSnapState
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.convertXForGravityChange
import com.mrsep.musicrecognizer.feature.recognition.service.floating.core.convertYForGravityChange
import com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.DismissWindow
import com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.MainWindow
import com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.ExtraWindow
import com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.GapBetweenWindows
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject
import kotlin.math.roundToInt

private const val MainWindowTag = "MainFloatingWindow"
private const val ExtraWindowTag = "ExtraFloatingWindow"
private const val DismissWindowTag = "DismissFloatingWindow"

internal class FloatingWindowController @Inject constructor(
    @ApplicationContext appContext: Context,
    trackRepository: TrackRepository,
    preferencesRepository: PreferencesRepository,
    metadataFetchManager: TrackMetadataFetchManager,
    @FloatingButtonStatusHolder private val statusHolder: RecognitionStatusHolder,
    private val deeplinkRouter: DeeplinkRouter,
    private val vibrationManager: VibrationManager,
) {
    private val uiContext: Context

    private val mainWindow: ComposeFloatingWindow
    private val extraWindow: ComposeFloatingWindow
    private val dismissWindow: ComposeFloatingWindow

    private val sharedModel: FloatingWindowSharedModel
    private val dismissWindowState: DismissWindowState
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Keep last screen size to update window position on configuration changes
    private var lastScreenWidth: Int
    private var lastScreenHeight: Int

    init {
        val displayManager = appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val targetDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val windowContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            appContext
                .createDisplayContext(targetDisplay)
                .createWindowContext(TYPE_APPLICATION_OVERLAY, null)
        } else {
            appContext.createDisplayContext(targetDisplay)
        }
        uiContext = windowContext

        dismissWindow = createDismissWindow()
        mainWindow = createMainWindow()
        extraWindow = createExtraWindow()

        lastScreenWidth = mainWindow.display.metrics.widthPixels
        lastScreenHeight = mainWindow.display.metrics.heightPixels

        dismissWindowState = DismissWindowState()
        sharedModel = FloatingWindowSharedModel(
            statusHolder,
            preferencesRepository,
            trackRepository,
            metadataFetchManager,
            coroutineScope = coroutineScope,
            vibrationManager = vibrationManager,
        )
    }

    private fun createMainWindow(): ComposeFloatingWindow {
        return ComposeFloatingWindow(
            context = uiContext,
            windowParams = defaultLayoutParams(),
            tag = MainWindowTag,
            onWindowLayoutSizeChanged = {
                syncExtraWindowPosition()
            },
            onWindowPositionChanged = { _, _ ->
                sharedModel.isLeftAnchored.value = when (mainWindow.currentWindowSide) {
                    WindowSide.LEFT -> true
                    WindowSide.RIGHT -> false
                }
                syncExtraWindowPosition()
            },
            onConfigurationChanged = {
                updateMainWindowPositionOnConfigurationChanged()
            },
            enterAnimation = WindowEnterAnimation(),
            exitAnimationHide = WindowExitAnimation(),
            exitAnimationClose = WindowExitAnimation(),
        ).apply {
            setContent {
                MainWindow(
                    sharedModel = sharedModel,
                    deeplinkRouter = deeplinkRouter,
                    dismissWindowState = dismissWindowState,
                    onDragStart = {
                        if (!dismissWindow.isShowing.value) dismissWindow.show()
                    },
                    onDragEnd = {
                        dismissWindow.hide()
                    },
                )
            }
        }
    }

    private fun createExtraWindow(): ComposeFloatingWindow {
        return ComposeFloatingWindow(
            context = uiContext,
            windowParams = defaultLayoutParams(),
            tag = ExtraWindowTag,
            onWindowLayoutSizeChanged = {
                syncExtraWindowPosition()
            },
            enterAnimation = WindowEnterAnimation(),
            exitAnimationHide = WindowExitAnimation(),
            exitAnimationClose = WindowExitAnimation(),
        ).apply {
            setContent {
                ExtraWindow(
                    sharedModel = sharedModel,
                    deeplinkRouter = deeplinkRouter,
                )
            }
        }
    }

    private fun createDismissWindow(): ComposeFloatingWindow {
        return ComposeFloatingWindow(
            context = uiContext,
            windowParams = defaultLayoutParams(),
            tag = DismissWindowTag,
            enterAnimation = WindowEnterAnimation(initialScale = 1f),
            exitAnimationHide = WindowExitAnimation(targetScale = 1f),
            exitAnimationClose = WindowExitAnimation(targetScale = 1f),
        ).apply {
            setContent {
                DismissWindow(dismissWindowState)
            }
        }
    }

    fun show() {
        // Prepare windows with specific z-order
        dismissWindow.show(hidden = true)
        if (!extraWindow.isShowing.value) {
            extraWindow.show()
        }
        if (!mainWindow.isShowing.value) {
            mainWindow.show(snapState = defaultSnapState)
        }
    }

    fun destroy() {
        coroutineScope.cancel()
        dismissWindow.close()
        extraWindow.close()
        mainWindow.close()
        statusHolder.resetFinalStatus()
    }

    private fun updateMainWindowPositionOnConfigurationChanged() {
        val params = mainWindow.windowParams

        val windowWidth = mainWindow.decorView.measuredWidth
        val windowHeight = mainWindow.decorView.measuredHeight

        // Capture the current window state with old screen dimensions
        val currentSnapState = calculateWindowSnapState(
            currentX = params.x,
            currentY = params.y,
            currentGravity = params.gravity,
            displayWidth = lastScreenWidth,
            displayHeight = lastScreenHeight,
            windowWidth = windowWidth,
            windowHeight = windowHeight
        )

        // Calculate the new window coordinates
        val newScreenWidth = mainWindow.display.metrics.widthPixels
        val newScreenHeight = mainWindow.display.metrics.heightPixels
        val safeInsets = mainWindow.display.safeInsets

        val (newX, newY) = calculateWindowPosition(
            snapState = currentSnapState,
            targetGravity = params.gravity,
            displayWidth = newScreenWidth,
            displayHeight = newScreenHeight,
            windowWidth = windowWidth,
            windowHeight = windowHeight
        )

        // Clamp the calculated position to keep the window strictly within the safe insets
        val absX = convertXForGravityChange(
            x = newX,
            fromGravity = params.gravity,
            toGravity = Gravity.LEFT,
            displayWidth = newScreenWidth,
            windowWidth = windowWidth
        )
        val absY = convertYForGravityChange(
            y = newY,
            fromGravity = params.gravity,
            toGravity = Gravity.TOP,
            displayHeight = newScreenHeight,
            windowHeight = windowHeight
        )
        val clampedAbsX = absX.coerceIn(
            minimumValue = safeInsets.left,
            maximumValue = maxOf(safeInsets.left, newScreenWidth - safeInsets.right - windowWidth)
        )
        val clampedAbsY = absY.coerceIn(
            minimumValue = safeInsets.top,
            maximumValue = maxOf(safeInsets.top, newScreenHeight - safeInsets.bottom - windowHeight)
        )

        val finalX = convertXForGravityChange(
            x = clampedAbsX,
            fromGravity = Gravity.LEFT,
            toGravity = params.gravity,
            displayWidth = newScreenWidth,
            windowWidth = windowWidth
        )
        val finalY = convertYForGravityChange(
            y = clampedAbsY,
            fromGravity = Gravity.TOP,
            toGravity = params.gravity,
            displayHeight = newScreenHeight,
            windowHeight = windowHeight
        )

        mainWindow.updateCoordinate(finalX, finalY)

        lastScreenWidth = newScreenWidth
        lastScreenHeight = newScreenHeight
    }

    fun syncExtraWindowPosition() {
        val density = mainWindow.display.metrics.density
        val gapX = (density * GapBetweenWindows.value).roundToInt()

        val mainWindowParams = mainWindow.windowParams
        val extraWindowParams = extraWindow.windowParams

        val displayWidth = mainWindow.display.metrics.widthPixels
        val displayHeight = mainWindow.display.metrics.heightPixels

        val mainWidth = mainWindow.currentWindowWidth
        val mainHeight = mainWindow.currentWindowHeight
        val extraWidth = extraWindow.currentWindowWidth
        val extraHeight = extraWindow.currentWindowHeight

        val mainLeft = convertXForGravityChange(
            x = mainWindowParams.x,
            fromGravity = mainWindowParams.gravity,
            toGravity = Gravity.LEFT,
            displayWidth = displayWidth,
            windowWidth = mainWidth,
        )
        val mainTop = convertYForGravityChange(
            y = mainWindowParams.y,
            fromGravity = mainWindowParams.gravity,
            toGravity = Gravity.TOP,
            displayHeight = displayHeight,
            windowHeight = mainHeight,
        )

        val extraLeftAbs = if (sharedModel.isLeftAnchored.value) {
            mainLeft + mainWidth + gapX
        } else {
            mainLeft - extraWidth - gapX
        }
        val extraTopAbs = mainTop + (mainHeight - extraHeight) / 2

        val targetX = convertXForGravityChange(
            x = extraLeftAbs,
            fromGravity = Gravity.LEFT,
            toGravity = extraWindowParams.gravity,
            displayWidth = displayWidth,
            windowWidth = extraWidth,
        )
        val targetY = convertYForGravityChange(
            y = extraTopAbs,
            fromGravity = Gravity.TOP,
            toGravity = extraWindowParams.gravity,
            displayHeight = displayHeight,
            windowHeight = extraHeight,
        )

        extraWindow.updateCoordinate(targetX, targetY)
    }

    companion object {
        private val defaultSnapState get() = WindowSnapState(side = WindowSide.RIGHT, fractionY = 0.4f)
    }
}
