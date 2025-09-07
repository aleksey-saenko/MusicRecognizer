package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.os.Build
import android.util.TypedValue
import androidx.annotation.Px
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import com.mrsep.musicrecognizer.core.ui.util.dpToPx
import com.mrsep.musicrecognizer.core.ui.util.pxToDp
import com.mrsep.musicrecognizer.feature.recognition.R
import com.mrsep.musicrecognizer.feature.recognition.widget.util.FontUtils.measureTextViewHeight
import com.mrsep.musicrecognizer.feature.recognition.widget.util.ImageUtils
import com.mrsep.musicrecognizer.feature.recognition.widget.util.ImageUtils.getMaxWidgetMemoryAllowedSizeInBytes

/**
 * Calculate sizes manually due to the lack of flexible size and weight modifiers in Glance
 * and need in rounded corner artwork on API less than 31 (runtime bitmap transformation).
 * Open for refactoring.
 */
internal sealed class RecognitionWidgetLayout {

    abstract val artworkSize: Dp

    @get:Px
    abstract val artworkSizePx: Int
    abstract val artworkStyle: WidgetArtworkStyle

    data class Circle(
        override val artworkSize: Dp,
        @Px override val artworkSizePx: Int,
        val widgetSize: Dp,
        val recognitionButtonMaxSize: Dp,
    ) : RecognitionWidgetLayout() {
        override val artworkStyle = WidgetArtworkStyle.CircleCrop
    }

    data class Square(
        override val artworkSize: Dp,
        @Px override val artworkSizePx: Int,
        override val artworkStyle: WidgetArtworkStyle.RoundedCorners,
        val widgetSize: Dp,
        val recognitionButtonMaxSize: Dp,
    ) : RecognitionWidgetLayout()

    data class Horizontal(
        override val artworkSize: Dp,
        @Px override val artworkSizePx: Int,
        override val artworkStyle: WidgetArtworkStyle.RoundedCorners,
        val recognitionButtonMaxSize: Dp,
        val isNarrow: Boolean,
    ) : RecognitionWidgetLayout()

    data class Vertical(
        override val artworkSize: Dp,
        @Px override val artworkSizePx: Int,
        override val artworkStyle: WidgetArtworkStyle.RoundedCorners,
        val recognitionButtonMaxSize: Dp,
    ) : RecognitionWidgetLayout()

    val showArtwork get() = artworkSize >= 40.dp

    companion object {

        @Composable
        fun fromLocalSize(): RecognitionWidgetLayout = with(LocalSize.current) {
            val context = LocalContext.current

            if (width < 150.dp || (width in 150.dp..250.dp && height < 150.dp)) {
                // Circle layout
                val minWidgetDimension = width.coerceAtMost(height)
                val artworkSize = minWidgetDimension - circleWidgetBorderWidth * 2
                val artworkSizePx = requiredArtworkSizePx(artworkSize)
                Circle(
                    artworkSize = artworkSize,
                    artworkSizePx = artworkSizePx,
                    widgetSize = minWidgetDimension,
                    recognitionButtonMaxSize = (minWidgetDimension.value * 0.75f).toInt().dp
                        .coerceAtLeast(56.dp)
                )
            } else if (width in 150.dp..250.dp && height > 150.dp) {
                // Square layout
                val minWidgetDimension = width.coerceAtMost(height)
                val artworkSize = minWidgetDimension - squareWidgetBorderWidth * 2
                val artworkSizePx = requiredArtworkSizePx(artworkSize)
                val artworkCornerRadiusPx = artworkCornerRadiusPx(
                    artworkSize,
                    artworkSizePx,
                    widgetOuterRadius() - squareWidgetBorderWidth
                )
                Square(
                    artworkSize = artworkSize,
                    artworkSizePx = artworkSizePx,
                    artworkStyle = WidgetArtworkStyle.RoundedCorners(
                        artworkCornerRadiusPx = artworkCornerRadiusPx,
                        bottomFading = true,
                    ),
                    widgetSize = minWidgetDimension,
                    recognitionButtonMaxSize = (minWidgetDimension.value * 0.33f).toInt().dp
                )
            } else if (width / height > 2) {
                // Horizontal layout
                val isNarrow = height < 80.dp
                val contentMaxHeight = height - widgetPadding * 2 - contentPadding * 2
                val buttonSize = contentMaxHeight.coerceIn(32.dp..56.dp)
                val textBlockReservedWidth = 150.dp
                val imageMaxWidth = width - widgetPadding * 2 - contentPadding * 2 -
                        artworkToTextPadding - textBlockReservedWidth -
                        (dividerHorizontalPadding * 2 + dividerWidth) -
                        (buttonSize + buttonHorizontalPadding(buttonSize) * 2)
                            .coerceAtLeast(0.dp)
                            .coerceAtMost(width / 4)
                val imageSize = contentMaxHeight.coerceAtMost(imageMaxWidth).value.toInt().dp
                val artworkSizePx = requiredArtworkSizePx(imageSize)
                val artworkCornerRadiusPx = artworkCornerRadiusPx(
                    imageSize,
                    artworkSizePx,
                    widgetInnerRadius()
                )
                Horizontal(
                    artworkSize = imageSize,
                    artworkSizePx = artworkSizePx,
                    artworkStyle = WidgetArtworkStyle.RoundedCorners(
                        artworkCornerRadiusPx = artworkCornerRadiusPx,
                    ),
                    recognitionButtonMaxSize = buttonSize,
                    isNarrow = isNarrow,
                )
            } else {
                // Vertical layout
                val buttonSize = 56.dp
                val titleHeight = measureTextViewHeight(context, titleTextSize, 2, shouldIncludeFontPadding)
                val subtitleHeight = measureTextViewHeight(context, subtitleTextSize, 1, shouldIncludeFontPadding)
                val textBlockHeight =
                    context.pxToDp((titleHeight + subtitleHeight).toFloat()).dp + subtitleTopPadding
                val imageMaxHeight = height - widgetPadding * 2 - contentPadding * 2 -
                        artworkToTextPadding - textBlockHeight
                val imageMaxWidth = width - widgetPadding * 2 - contentPadding * 2 -
                        (dividerHorizontalPadding * 2 + dividerWidth) -
                        (buttonSize + buttonHorizontalPadding(buttonSize) * 2)
                val imageSize = imageMaxHeight.coerceAtMost(imageMaxWidth).value.toInt().dp
                val artworkSizePx = requiredArtworkSizePx(imageSize)
                val artworkCornerRadiusPx =
                    artworkCornerRadiusPx(imageSize, artworkSizePx, widgetInnerRadius())
                Vertical(
                    artworkSize = imageSize,
                    artworkSizePx = artworkSizePx,
                    artworkStyle = WidgetArtworkStyle.RoundedCorners(
                        artworkCornerRadiusPx = artworkCornerRadiusPx,
                    ),
                    recognitionButtonMaxSize = buttonSize,
                )
            }
        }

        @Composable
        fun widgetInnerRadius(): Dp = with(LocalContext.current) {
            pxToDp(resources.getDimension(R.dimen.widget_inner_radius)).dp
        }

        @Composable
        fun widgetOuterRadius(): Dp = with(LocalContext.current) {
            pxToDp(resources.getDimension(R.dimen.widget_background_radius)).dp
        }

        @Composable
        fun buttonScaleFactor(): Float = with(LocalContext.current) {
            val outValue = TypedValue()
            resources.getValue(R.dimen.widget_button_scale_factor, outValue, true)
            return outValue.float
        }

        @Composable
        private fun requiredArtworkSizePx(imageSize: Dp): Int {
            val context = LocalContext.current
            val artworkSizePx = context.dpToPx(imageSize.value).toInt()
            val imageSizeLimitPx = ImageUtils.getMaxPossibleImageSize(
                aspectRatio = 1.0 / 1.0,
                memoryLimitBytes = context.getMaxWidgetMemoryAllowedSizeInBytes(),
                maxImages = 1
            )
            return artworkSizePx.coerceAtMost(imageSizeLimitPx.width)
        }

        @Composable
        private fun artworkCornerRadiusPx(
            imageSize: Dp,
            imageSizePx: Int,
            cornerRadius: Dp,
        ): Int {
            val imageDensity = imageSizePx / imageSize.value
            return (imageDensity * cornerRadius.value).toInt()
        }

        // From API 33, includeFontPadding becomes redundant for TextView
        // https://medium.com/androiddevelopers/fixing-font-padding-in-compose-text-768cd232425b
        val shouldIncludeFontPadding = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

        val circleWidgetBorderWidth = 3.dp
        val squareWidgetBorderWidth = 0.dp

        // Vertical and horizontal layouts
        val widgetPadding = 8.dp
        val contentPadding = 8.dp
        val artworkToTextPadding = 12.dp

        val titleTextSize = 16.sp
        val subtitleTextSize = 14.sp
        val subtitleTopPadding = if (shouldIncludeFontPadding) 4.dp else 6.dp

        val dividerWidth = 1.dp
        val dividerHorizontalPadding = 8.dp
        val dividerVerticalPadding = 4.dp

        fun buttonHorizontalPadding(buttonSize: Dp) = if (buttonSize <= 36.dp) 4.dp else 8.dp
    }
}

internal sealed interface WidgetArtworkStyle {

    data object CircleCrop : WidgetArtworkStyle

    data class RoundedCorners(
        @Px val artworkCornerRadiusPx: Int,
        val bottomFading: Boolean = false,
    ) : WidgetArtworkStyle
}
