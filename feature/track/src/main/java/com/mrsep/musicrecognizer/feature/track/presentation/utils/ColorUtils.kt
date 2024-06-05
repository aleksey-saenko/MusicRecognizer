package com.mrsep.musicrecognizer.feature.track.presentation.utils

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

internal fun Bitmap.getDominantColor(): Color? {
    val palette = Palette.Builder(this).maximumColorCount(24).generate()
    return palette.dominantSwatch?.rgb?.let { androidColor -> Color(androidColor) }
}

@Composable
internal fun SwitchingMusicRecognizerTheme(
    seedColor: Color?,
    artworkBasedThemeEnabled: Boolean,
    useDarkTheme: Boolean,
    usePureBlack: Boolean = false,
    highContrastMode: Boolean = false,
    style: PaletteStyle = PaletteStyle.Vibrant,
    contrastLevel: Double = 0.0,
    animationSpec: AnimationSpec<Color> = tween(durationMillis = 800),
    content: @Composable () -> Unit,
) {
    val defaultColorScheme = MaterialTheme.colorScheme
    val colorScheme: ColorScheme by remember(
        artworkBasedThemeEnabled,
        seedColor,
        useDarkTheme,
        usePureBlack,
        highContrastMode
    ) {
        derivedStateOf {
            val scheme = if (!artworkBasedThemeEnabled || seedColor == null) {
                defaultColorScheme
            } else {
                dynamicColorScheme(
                    seedColor = seedColor,
                    isDark = useDarkTheme,
                    style = style,
                    contrastLevel = contrastLevel
                )
            }
            val newBackgroundColor = when {
                highContrastMode -> if (useDarkTheme) Color.Black else Color.White
                usePureBlack && useDarkTheme -> Color.Black
                else -> return@derivedStateOf scheme
            }
            scheme.copy(
                background = newBackgroundColor,
                surface = newBackgroundColor
            )
        }
    }
    MaterialTheme(
        colorScheme = colorScheme.switch(animationSpec),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
        content()
    }
}

@Composable
internal fun ColorScheme.switch(animationSpec: AnimationSpec<Color>) = copy(
    primary = animateColorAsState(primary, animationSpec, "").value,
    onPrimary = animateColorAsState(onPrimary, animationSpec, "").value,
    primaryContainer = animateColorAsState(primaryContainer, animationSpec, "").value,
    onPrimaryContainer = animateColorAsState(onPrimaryContainer, animationSpec, "").value,
    secondary = animateColorAsState(secondary, animationSpec, "").value,
    onSecondary = animateColorAsState(onSecondary, animationSpec, "").value,
    secondaryContainer = animateColorAsState(secondaryContainer, animationSpec, "").value,
    onSecondaryContainer = animateColorAsState(onSecondaryContainer, animationSpec, "").value,
    tertiary = animateColorAsState(tertiary, animationSpec, "").value,
    onTertiary = animateColorAsState(onTertiary, animationSpec, "").value,
    tertiaryContainer = animateColorAsState(tertiaryContainer, animationSpec, "").value,
    onTertiaryContainer = animateColorAsState(onTertiaryContainer, animationSpec, "").value,
    error = animateColorAsState(error, animationSpec, "").value,
    errorContainer = animateColorAsState(errorContainer, animationSpec, "").value,
    onError = animateColorAsState(onError, animationSpec, "").value,
    onErrorContainer = animateColorAsState(onErrorContainer, animationSpec, "").value,
    background = animateColorAsState(background, animationSpec, "").value,
    onBackground = animateColorAsState(onBackground, animationSpec, "").value,
    surface = animateColorAsState(surface, animationSpec, "").value,
    onSurface = animateColorAsState(onSurface, animationSpec, "").value,
    surfaceVariant = animateColorAsState(surfaceVariant, animationSpec, "").value,
    onSurfaceVariant = animateColorAsState(onSurfaceVariant, animationSpec, "").value,
    outline = animateColorAsState(outline, animationSpec, "").value,
    inverseOnSurface = animateColorAsState(inverseOnSurface, animationSpec, "").value,
    inverseSurface = animateColorAsState(inverseSurface, animationSpec, "").value,
    inversePrimary = animateColorAsState(inversePrimary, animationSpec, "").value,
    surfaceTint = animateColorAsState(surfaceTint, animationSpec, "").value,
    outlineVariant = animateColorAsState(outlineVariant, animationSpec, "").value,
    scrim = animateColorAsState(scrim, animationSpec, "").value,
    surfaceBright = animateColorAsState(surfaceBright, animationSpec, "").value,
    surfaceDim = animateColorAsState(surfaceDim, animationSpec, "").value,
    surfaceContainer = animateColorAsState(surfaceContainer, animationSpec, "").value,
    surfaceContainerHigh = animateColorAsState(surfaceContainerHigh, animationSpec, "").value,
    surfaceContainerHighest = animateColorAsState(surfaceContainerHighest, animationSpec, "").value,
    surfaceContainerLow = animateColorAsState(surfaceContainerLow, animationSpec, "").value,
    surfaceContainerLowest = animateColorAsState(surfaceContainerLowest, animationSpec, "").value,
)
