package com.mrsep.musicrecognizer.feature.recognition.widget.ui

import android.os.Build
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
import com.mrsep.musicrecognizer.feature.recognition.widget.util.FontUtils.measureTextViewHeight
import com.mrsep.musicrecognizer.feature.recognition.widget.util.ImageUtils
import com.mrsep.musicrecognizer.feature.recognition.widget.util.ImageUtils.getMaxWidgetMemoryAllowedSizeInBytes

/* Calculate sizes manually due to the lack of flexible size and weight modifiers in Glance
* and need in rounded corner artwork on API less than 31 (runtime bitmap transformation) */
internal sealed class RecognitionWidgetLayout(
    val artworkSize: Dp,
    @Px val artworkSizePx: Int,
    @Px val artworkCornerRadiusPx: Int,
    val isNarrow: Boolean,
) {

    class Horizontal(
        artworkSize: Dp,
        @Px artworkSizePx: Int,
        @Px artworkCornerRadiusPx: Int,
        isNarrow: Boolean,
    ) : RecognitionWidgetLayout(artworkSize, artworkSizePx, artworkCornerRadiusPx, isNarrow)

    class Vertical(
        artworkSize: Dp,
        @Px artworkSizePx: Int,
        @Px artworkCornerRadiusPx: Int,
    ) : RecognitionWidgetLayout(artworkSize, artworkSizePx, artworkCornerRadiusPx, false)

    val showArtwork = artworkSize >= 40.dp

    companion object {

        @Composable
        fun fromLocalSize(): RecognitionWidgetLayout = with(LocalSize.current) {
            val context = LocalContext.current
            if (width / height > 2) {
                val isNarrow = height < 80.dp
                val imageMaxHeight = height - widgetPadding * 2 - contentPadding * 2
                val textBlockReservedWidth = 150.dp
                val imageMaxWidth = (width - widgetPadding * 2 - contentPadding * 2 -
                        artworkToTextPadding - (dividerHorizontalPadding * 2 + dividerWidth) -
                        getButtonSectionWidth(isNarrow) - textBlockReservedWidth)
                    .coerceAtLeast(0.dp)
                    .coerceAtMost(width / 4)
                val imageSize = imageMaxHeight.coerceAtMost(imageMaxWidth).value.toInt().dp
                val (artworkSizePx, cornerRadiusPx) = calcImageSizePx(imageSize)
                Horizontal(
                    artworkSize = imageSize,
                    artworkSizePx = artworkSizePx,
                    artworkCornerRadiusPx = cornerRadiusPx,
                    isNarrow = isNarrow
                )
            } else {
                val titleHeight = measureTextViewHeight(context, titleTextSize, 2)
                val subtitleHeight = measureTextViewHeight(context, subtitleTextSize, 1)
                val textBlockHeight =
                    context.pxToDp((titleHeight + subtitleHeight).toFloat()).dp + subtitleTopPadding
                val imageMaxHeight = height - widgetPadding * 2 - contentPadding * 2 -
                        artworkToTextPadding - textBlockHeight
                val imageMaxWidth = width - widgetPadding * 2 - contentPadding * 2 -
                        (dividerHorizontalPadding * 2 + dividerWidth) -
                        getButtonSectionWidth(false)
                val imageSize = imageMaxHeight.coerceAtMost(imageMaxWidth).value.toInt().dp
                val (artworkSizePx, cornerRadiusPx) = calcImageSizePx(imageSize)
                Vertical(
                    artworkSize = imageSize,
                    artworkSizePx = artworkSizePx,
                    artworkCornerRadiusPx = cornerRadiusPx
                )
            }
        }

        @Composable
        fun widgetInnerRadius(): Dp {
            val context = LocalContext.current
            var innerRadius = widgetDefaultInnerRadius
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runCatching {
                    context.resources.getDimension(android.R.dimen.system_app_widget_inner_radius)
                }.onSuccess { radiusPx ->
                    innerRadius = context.pxToDp(radiusPx).dp
                }
            }
            return innerRadius
        }

        @Composable
        private fun calcImageSizePx(imageSize: Dp): Pair<Int, Int> {
            val context = LocalContext.current
            val artworkSizePx = context.dpToPx(imageSize.value).toInt()
            val imageSizeLimitPx = ImageUtils.getMaxPossibleImageSize(
                aspectRatio = 1.0 / 1.0,
                memoryLimitBytes = context.getMaxWidgetMemoryAllowedSizeInBytes(),
                maxImages = 1
            )
            val requiredImageSizePx = artworkSizePx.coerceAtMost(imageSizeLimitPx.width)

            val imageDensity = requiredImageSizePx / imageSize.value
            val cornerRadiusPx = (imageDensity * widgetInnerRadius().value).toInt()
            return artworkSizePx to cornerRadiusPx
        }

        private val widgetDefaultInnerRadius = 20.dp
        val widgetDefaultBackgroundRadius = 28.dp

        val widgetPadding = 8.dp
        val contentPadding = 8.dp
        val artworkToTextPadding = 16.dp

        val titleTextSize = 16.sp
        val subtitleTextSize = 14.sp
        val subtitleTopPadding = 4.dp

        val dividerWidth = 1.dp
        val dividerHorizontalPadding = 8.dp
        val dividerVerticalPadding = 4.dp

        fun getButtonSectionWidth(isNarrow: Boolean) = if (isNarrow) 56.dp else 72.dp
    }
}