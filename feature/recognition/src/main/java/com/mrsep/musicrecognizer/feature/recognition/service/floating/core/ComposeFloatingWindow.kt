package com.mrsep.musicrecognizer.feature.recognition.service.floating.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.core.view.isGone
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class ComposeFloatingWindow internal constructor(
    private val context: Context,
    val windowParams: WindowManager.LayoutParams,
    private val tag: String = "ComposeFloatingWindow",
    private val onWindowPositionChanged: ((Int, Int) -> Unit)? = null,
    private val onWindowLayoutSizeChanged: (() -> Unit)? = null,
    private val onConfigurationChanged: (() -> Unit)? = null,
    private val enterAnimation: WindowEnterAnimation = WindowEnterAnimation(),
    private val exitAnimationHide: WindowExitAnimation = WindowExitAnimation(),
    private val exitAnimationClose: WindowExitAnimation = exitAnimationHide,
) : SavedStateRegistryOwner, ViewModelStoreOwner, AutoCloseable {

    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    // --- Lifecycle, ViewModel, SavedState ---

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(tag, "Coroutine Exception: ${throwable.message}", throwable)
    }
    internal val coroutineContext = AndroidUiDispatcher.Main
    internal val lifecycleCoroutineScope = CoroutineScope(
        SupervisorJob() + coroutineContext + coroutineExceptionHandler,
    )

    override val viewModelStore: ViewModelStore = ViewModelStore()

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val savedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // --- Window State ---

    private val _isShowing = MutableStateFlow(false)
    val isShowing: StateFlow<Boolean> = _isShowing.asStateFlow()

    private val _isDestroyed = MutableStateFlow(false)
    val isDestroyed: StateFlow<Boolean> = _isDestroyed.asStateFlow()

    val display: DisplayHelper = DisplayHelper(context, windowManager)

    /** The root view container for the floating window's content. */
    var decorView: ViewGroup = FloatingWindowDecorView(
        context = context,
        displayHelper = display,
        onDecorViewLayoutFinished = ::onDecorViewLayoutFinished,
        onConfigurationChanged = { onConfigurationChanged?.invoke() },
    ).apply {
        // Important: Prevent clipping so shadows or elements outside bounds can be drawn
        clipChildren = false
        clipToPadding = false
    }
        private set

    val currentWindowWidth: Int
        get() = decorView.width.takeIf { it > 0 }
            ?: decorView.measuredWidth.takeIf { it > 0 }
            ?: 0

    val currentWindowHeight: Int
        get() = decorView.height.takeIf { it > 0 }
            ?: decorView.measuredHeight.takeIf { it > 0 }
            ?: 0

    val windowMaxAvailableWidth: Int get() = display.metrics.widthPixels
    val windowMaxAvailableHeight: Int get() = display.metrics.heightPixels

    private var composeView: ComposeView? = null
    private var parentComposition: Recomposer? = null

    private var coordinateUpdateScheduled = false

    private val windowDisplayId: Int
        get() = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.display.displayId
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.displayId
        }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}

        override fun onDisplayChanged(displayId: Int) {
            if (displayId != windowDisplayId) return
            val display = displayManager.getDisplay(displayId) ?: return
            when (display.state) {
                Display.STATE_OFF,
                Display.STATE_DOZE,
                Display.STATE_ON_SUSPEND,
                Display.STATE_DOZE_SUSPEND -> {
                    Log.d(tag, "Display $displayId went to inactive/suspend state: ${display.state}")
                    handleScreenOff()
                }

                Display.STATE_ON -> {
                    Log.d(tag, "Display $displayId woke up")
                    handleScreenOn()
                }

                Display.STATE_VR,
                Display.STATE_UNKNOWN -> Unit
            }
        }
    }

    private fun handleScreenOff() {
        if (_isShowing.value &&
            lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)
        ) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }

    private fun handleScreenOn() {
        if (_isShowing.value && decorView.isVisible &&
            !lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)
        ) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    init {
        // Restore state early in the lifecycle
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        // Enable SavedStateHandles for ViewModels
        enableSavedStateHandles()
        displayManager.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
        Log.d(tag, "Floating window initialized")
    }

    /** Sets the Jetpack Compose content for the floating window. */
    fun setContent(content: @Composable () -> Unit) {
        checkDestroyed()

        disposeCompositionIfNeeded()

        val currentComposeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@ComposeFloatingWindow)
            setViewTreeViewModelStoreOwner(this@ComposeFloatingWindow)
            setViewTreeSavedStateRegistryOwner(this@ComposeFloatingWindow)
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycle),
            )

            // ComposeView is hosted directly by WindowManager, so install a lifecycle-aware
            // recomposer instead of relying on an Activity window recomposer.
            val recomposer = createLifecycleAwareWindowRecomposer(
                coroutineContext = this@ComposeFloatingWindow.coroutineContext,
                lifecycle = lifecycle,
            )
            setParentCompositionContext(recomposer)
            parentComposition = recomposer

            setContent {
                CompositionLocalProvider(LocalFloatingWindow provides this@ComposeFloatingWindow) {
                    content()
                }
            }
        }

        this.composeView = currentComposeView

        // Replace the content view in the decorView
        if (decorView.isNotEmpty()) {
            decorView.removeAllViews()
        }

        decorView.addView(currentComposeView)

        // If already showing, update the layout immediately
        if (isShowing.value) {
            update()
        }
    }

    private var lastNotifiedX = Int.MIN_VALUE
    private var lastNotifiedY = Int.MIN_VALUE

    private fun notifyWindowPositionChangedIfNeeded() {
        if (onWindowPositionChanged == null) return
        val x = windowParams.x
        val y = windowParams.y

        if (x == lastNotifiedX && y == lastNotifiedY) return

        lastNotifiedX = x
        lastNotifiedY = y
        onWindowPositionChanged.invoke(x, y)
    }


    // State for two-stage attach
    private var pendingSnapState: WindowSnapState? = null
    private var pendingHidden: Boolean = false
    private var pendingShowRequest: Boolean = false

    /**
     * Shows the floating window.
     *
     * @param snapState Optional. The physical state (side and fraction) to position the window at.
     * If null, the window will retain its current coordinates (or 0,0 if not previously positioned).
     * @param hidden If true, forces the window to be attached (reserving its z-order) but switches it to [View.GONE]
     * after coordinates are calculated. If false, makes it visible with enter animation.
     */
    fun show(snapState: WindowSnapState? = null, hidden: Boolean = false) {
        checkDestroyed()

        require(composeView != null) {
            "Content must be set using setContent() before showing the window"
        }

        if (!Settings.canDrawOverlays(context)) {
            Log.w(tag, "Overlay permission is not granted. Cannot show the window")
            return
        }

        Log.d(tag, "Showing floating window")

        pendingSnapState = snapState
        pendingHidden = hidden
        pendingShowRequest = true

        if (decorView.parent == null) {
            // Attach to WindowManager in INVISIBLE state to allow measuring pass
            decorView.visibility = View.INVISIBLE

            if (!hidden) {
                decorView.alpha = enterAnimation.initialAlpha
                decorView.scaleX = enterAnimation.initialScale
                decorView.scaleY = enterAnimation.initialScale
            } else {
                decorView.alpha = 0f
            }

            windowManager.addView(decorView, windowParams)
        } else {
            // Window is already attached
            if (decorView.isGone) {
                // Wake it up to trigger onMeasure and onLayout
                if (!hidden) {
                    decorView.alpha = enterAnimation.initialAlpha
                    decorView.scaleX = enterAnimation.initialScale
                    decorView.scaleY = enterAnimation.initialScale
                }
                decorView.visibility = View.INVISIBLE
            } else {
                // If it's already VISIBLE or INVISIBLE with known dimensions, apply immediately
                if (currentWindowWidth > 0 && currentWindowHeight > 0) {
                    processPendingUpdates()
                }
            }
        }
    }

    /**
     * Processes pending coordinate changes and visibility states.
     * This separates coordinate math from visibility and lifecycle management,
     * allowing them to be updated independently based on arguments passed to [show].
     */
    private fun processPendingUpdates() {
        if (!pendingShowRequest) return

        val width = currentWindowWidth
        val height = currentWindowHeight

        // Prevent calculation with zero dimensions
        if (width <= 0 || height <= 0) return

        // 1 - Calculate and apply coordinates only if a new snapState is provided
        pendingSnapState?.let { snapState ->
            val (x, y) = calculateWindowPositionForState(snapState)
            windowParams.x = x
            windowParams.y = y
            pendingSnapState = null
        }

        val isHidden = pendingHidden
        pendingShowRequest = false

        try {
            windowManager.updateViewLayout(decorView, windowParams)
            notifyWindowPositionChangedIfNeeded()
        } catch (e: Exception) {
            Log.e(tag, "Error updating window layout: ${e.message}", e)
            return
        }

        // 2 - Apply final visibility and lifecycle states
        if (isHidden) {
            Log.d(tag, "Window positioned and forced to hidden (GONE)")
            decorView.visibility = View.GONE
            _isShowing.update { false }

            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        } else {
            if (!_isShowing.value || decorView.visibility != View.VISIBLE) {
                Log.d(tag, "Window positioned and showing")
                decorView.visibility = View.VISIBLE

                if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
                }
                if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                }

                decorView.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(enterAnimation.durationMs)
                    .start()

                _isShowing.update { true }
            }
        }
    }

    fun updateCoordinate(x: Int, y: Int) {
        if (windowParams.x == x && windowParams.y == y) return
        windowParams.x = x
        windowParams.y = y
        try {
            scheduleCoordinateUpdate()
        } catch (e: Exception) {
            Log.w(tag, "Failed to update window position: ${e.message}")
        }
    }

    private fun scheduleCoordinateUpdate() {
        if (coordinateUpdateScheduled) return
        coordinateUpdateScheduled = true

        decorView.postOnAnimation {
            coordinateUpdateScheduled = false
            if (_isDestroyed.value) return@postOnAnimation
            update()
        }
    }

    private fun onDecorViewLayoutFinished(changed: Boolean) {
        if (_isDestroyed.value || decorView.parent == null) return
        // Process internal two-stage attach logic if requested
        if (pendingShowRequest && currentWindowWidth > 0 && currentWindowHeight > 0) {
            processPendingUpdates()
        }
        if (changed) onWindowLayoutSizeChanged?.invoke()
    }

    /**
     * Updates the layout of the floating window using the current [windowParams].
     * Call this after modifying [windowParams] (e.g., position or size) while the window is showing.
     */
    private fun update() {
        checkDestroyed()
        if (decorView.parent == null) {
            Log.w(tag, "Update called but window is not attached")
            return
        }
        try {
            windowManager.updateViewLayout(decorView, windowParams)
            notifyWindowPositionChangedIfNeeded()
        } catch (e: Exception) {
            Log.e(tag, "Error updating window layout: ${e.message}", e)
        }
    }

    fun setTouchable(isTouchable: Boolean) {
        val currentFlags = windowParams.flags
        if (isTouchable) {
            windowParams.flags =
                currentFlags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        } else {
            windowParams.flags = currentFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        if (windowParams.flags == currentFlags) return
        if (decorView.parent != null) {
            try {
                windowManager.updateViewLayout(decorView, windowParams)
            } catch (e: Exception) {
                Log.e(tag, "Failed to update window touchable state: ${e.message}")
            }
        }
    }

    /**
     * Hides the floating window.
     * Transitions visibility to [View.GONE] to stop Compose layouts and moves lifecycle to STOPPED.
     */
    fun hide() {
        checkDestroyed()

        val wasPending = pendingShowRequest
        pendingShowRequest = false

        if (!_isShowing.value && !wasPending) {
            Log.d(tag, "Hide called but window is already hidden")
            return
        }
        Log.d(tag, "Hiding floating window")

        _isShowing.update { false }
        try {
            if (decorView.parent != null) {
                // If the window is still in the measuring phase (INVISIBLE), skip animation and snap to GONE
                if (decorView.visibility != View.VISIBLE) {
                    decorView.animate().cancel()
                    decorView.visibility = View.GONE
                    if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                    }
                    return
                }

                // Move ON_PAUSE immediately when animation starts
                if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                }

                decorView.animate()
                    .alpha(exitAnimationHide.targetAlpha)
                    .scaleX(exitAnimationHide.targetScale)
                    .scaleY(exitAnimationHide.targetScale)
                    .setDuration(exitAnimationHide.durationMs)
                    .withEndAction {
                        decorView.visibility = View.GONE
                        // Reset properties to default state to avoid layout jumps on next show()
                        decorView.alpha = 1f
                        decorView.scaleX = 1f
                        decorView.scaleY = 1f

                        // Move to ON_STOP after animation finishes, so Compose animations don't freeze abruptly
                        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                        }
                    }
                    .start()
            } else {
                Log.w(tag, "Hide called but DecorView has no parent")
                if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error hiding window: ${e.message}", e)
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        }
    }

    fun checkDestroyed() {
        check(!_isDestroyed.value) { "FloatingWindow has been destroyed and cannot be used" }
    }

    /** Disposes the Compose composition and clears the reference. */
    private fun disposeCompositionIfNeeded() {
        composeView?.let {
            it.disposeComposition()
            parentComposition?.cancel()
            parentComposition = null
            decorView.removeView(it)
            composeView = null
        }
    }

    /** Destroys the floating window, releasing all associated resources. */
    override fun close() {
        if (_isDestroyed.value) {
            Log.w(tag, "Destroy called but window is already destroyed")
            return
        }

        try {
            displayManager.unregisterDisplayListener(displayListener)
        } catch (e: Exception) {
            Log.e(tag, "Error unregistering display listener: ${e.message}")
        }

        Log.d(tag, "Destroying window")

        pendingShowRequest = false // Cancel any pending operations

        fun finishDestroy() {
            if (_isDestroyed.value) return

            // Hide the window if showing (ensures view is removed from WindowManager)
            if (_isShowing.value) {
                try {
                    _isShowing.update { false }
                    decorView.animate().cancel()

                    if (decorView.parent != null) {
                        windowManager.removeViewImmediate(decorView)
                    } else {
                        Log.w(tag, "Destroy called but DecorView has no parent")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error hiding window during destruction: ${e.message}", e)
                }
            }

            _isDestroyed.update { true }

            disposeCompositionIfNeeded()
            lifecycleCoroutineScope.cancel()
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            viewModelStore.clear()
            Log.d(tag, "FloatingWindow destroyed")
        }

        if (_isShowing.value && decorView.isVisible) {
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }
            decorView.animate()
                .alpha(exitAnimationClose.targetAlpha)
                .scaleX(exitAnimationClose.targetScale)
                .scaleY(exitAnimationClose.targetScale)
                .setDuration(exitAnimationClose.durationMs)
                .withEndAction {
                    if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                    }
                    finishDestroy()
                }
                .start()
        } else {
            finishDestroy()
        }
    }

    // --- Window positioning & Snap state ---

    val currentWindowSide: WindowSide
        get() = calculateWindowSide(
            currentX = windowParams.x,
            currentGravity = windowParams.gravity,
            displayWidth = windowMaxAvailableWidth,
            windowWidth = currentWindowWidth
        )

    val currentWindowSnapState: WindowSnapState
        get() = calculateWindowSnapState(
            currentX = windowParams.x,
            currentY = windowParams.y,
            currentGravity = windowParams.gravity,
            displayWidth = windowMaxAvailableWidth,
            displayHeight = windowMaxAvailableHeight,
            windowWidth = currentWindowWidth,
            windowHeight = currentWindowHeight,
        )

    fun calculateWindowPositionForState(snapState: WindowSnapState): Pair<Int, Int> {
        return calculateWindowPosition(
            snapState = snapState,
            targetGravity = windowParams.gravity,
            displayWidth = windowMaxAvailableWidth,
            displayHeight = windowMaxAvailableHeight,
            windowWidth = currentWindowWidth,
            windowHeight = currentWindowHeight
        )
    }
}

@SuppressLint("ViewConstructor")
private class FloatingWindowDecorView(
    context: Context,
    private val displayHelper: DisplayHelper,
    private val onDecorViewLayoutFinished: (changed: Boolean) -> Unit,
    private val onConfigurationChanged: () -> Unit,
) : FrameLayout(context) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Ensure metrics are refreshed as soon as the view attaches to the window hierarchy
        displayHelper.refresh(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val windowParams = layoutParams as? WindowManager.LayoutParams

        val adjustedWidthMeasureSpec =
            if (windowParams?.width == WindowManager.LayoutParams.WRAP_CONTENT) {
                MeasureSpec.makeMeasureSpec(
                    displayHelper.metrics.widthPixels,
                    MeasureSpec.AT_MOST
                )
            } else {
                widthMeasureSpec
            }

        val adjustedHeightMeasureSpec =
            if (windowParams?.height == WindowManager.LayoutParams.WRAP_CONTENT) {
                MeasureSpec.makeMeasureSpec(
                    displayHelper.metrics.heightPixels,
                    MeasureSpec.AT_MOST
                )
            } else {
                heightMeasureSpec
            }

        super.onMeasure(
            adjustedWidthMeasureSpec,
            adjustedHeightMeasureSpec
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        onDecorViewLayoutFinished(changed)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        // Refresh metrics on rotation or other config changes
        displayHelper.refresh(this)
        onConfigurationChanged()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        // Refresh metrics when becoming visible, as GONE views miss config changes
        if (changedView == this && visibility != GONE) {
            displayHelper.refresh(this)
        }
    }
}

internal data class WindowEnterAnimation(
    val initialAlpha: Float = 0f,
    val initialScale: Float = 0.7f,
    val durationMs: Long = 200L
)

internal data class WindowExitAnimation(
    val targetAlpha: Float = 0f,
    val targetScale: Float = 0.7f,
    val durationMs: Long = 200L
)
