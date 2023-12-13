package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.DialogSwitch
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.feature.track.domain.model.FontSize
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences

@Composable
internal fun FontStyleDialog(
    fontStyle: UserPreferences.LyricsFontStyle,
    onFontStyleChanged: (UserPreferences.LyricsFontStyle) -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        title = {
            Text(text = stringResource(StringsR.string.lyrics_text_style))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(StringsR.string.font_size),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(StringsR.string.make_text_bigger_or_smaller),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .alpha(0.9f)
                        .padding(top = 8.dp)
                )
                Slider(
                    value = fontStyle.fontSize.ordinal.toFloat(),
                    onValueChange = { sliderValue ->
                        val fontSize = FontSize.entries[sliderValue.toInt()]
                        onFontStyleChanged(fontStyle.copy(fontSize = fontSize))
                    },
                    valueRange = 0f..3f,
                    steps = 2
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    DialogSwitch(
                        title = stringResource(StringsR.string.bold_text),
                        checked = fontStyle.isBold,
                        onClick = {
                            val style = fontStyle.copy(isBold = !fontStyle.isBold)
                            onFontStyleChanged(style)
                        }
                    )
                    DialogSwitch(
                        title = stringResource(StringsR.string.high_contrast_text),
                        checked = fontStyle.isHighContrast,
                        onClick = {
                            val style = fontStyle.copy(isHighContrast = !fontStyle.isHighContrast)
                            onFontStyleChanged(style)
                        }
                    )
                }
            }
        }
    )
}