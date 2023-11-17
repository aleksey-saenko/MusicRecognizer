package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
                        val fontSize = FontSize.values()[sliderValue.toInt()]
                        onFontStyleChanged(fontStyle.copy(fontSize = fontSize))
                    },
                    valueRange = 0f..3f,
                    steps = 2
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(StringsR.string.bold_text),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = fontStyle.isBold,
                        onCheckedChange = { checked ->
                            onFontStyleChanged(fontStyle.copy(isBold = checked))
                        }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(StringsR.string.high_contrast_text),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = fontStyle.isHighContrast,
                        onCheckedChange = { checked ->
                            onFontStyleChanged( fontStyle.copy(isHighContrast = checked))
                        }
                    )
                }
            }
        }
    )
}