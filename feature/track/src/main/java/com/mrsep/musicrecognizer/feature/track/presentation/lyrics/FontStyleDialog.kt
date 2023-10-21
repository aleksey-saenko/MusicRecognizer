package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.feature.track.domain.model.FontSize
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences

internal class FontStyleDialogState(
    initialState: UserPreferences.LyricsFontStyle,
) {
    var fontSize by mutableStateOf(initialState.fontSize)
    var isBold by mutableStateOf(initialState.isBold)
    var isHighContrast by mutableStateOf(initialState.isHighContrast)

    val currentState: UserPreferences.LyricsFontStyle
        get() = UserPreferences.LyricsFontStyle(
            fontSize = fontSize,
            isBold = isBold,
            isHighContrast = isHighContrast
        )

    companion object {
        val Saver: Saver<FontStyleDialogState, *> = listSaver(
            save = {
                listOf<Any>(
                    it.fontSize.ordinal,
                    it.isBold,
                    it.isHighContrast
                )
            },
            restore = {
                FontStyleDialogState(
                    initialState = UserPreferences.LyricsFontStyle(
                        fontSize = FontSize.values()[it[0] as Int],
                        isBold = it[1] as Boolean,
                        isHighContrast = it[2] as Boolean
                    )
                )
            }
        )
    }

}

@Composable
internal fun rememberFontStyleDialogState(
    initialState: UserPreferences.LyricsFontStyle,
): FontStyleDialogState {
    return rememberSaveable(
        inputs = arrayOf(initialState),
        saver = FontStyleDialogState.Saver
    ) {
        FontStyleDialogState(
            initialState = initialState
        )
    }
}


@Composable
internal fun FontStyleDialog(
    fontStyleDialogState: FontStyleDialogState,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        confirmButton = {
            Button(onClick = onConfirmClick) {
                Text(text = stringResource(StringsR.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissClick) {
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
                    value = fontStyleDialogState.fontSize.ordinal.toFloat(),
                    onValueChange = {
                        fontStyleDialogState.fontSize = FontSize.values()[it.toInt()]
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
                        checked = fontStyleDialogState.isBold,
                        onCheckedChange = { fontStyleDialogState.isBold = it }
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
                        checked = fontStyleDialogState.isHighContrast,
                        onCheckedChange = { fontStyleDialogState.isHighContrast = it }
                    )
                }
            }
        }
    )
}