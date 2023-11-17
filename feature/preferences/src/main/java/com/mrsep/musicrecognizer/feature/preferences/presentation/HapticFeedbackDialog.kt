package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences

@Stable
internal class HapticFeedbackDialogState(
    initialState: UserPreferences.HapticFeedback,
) {
    var vibrateOnTap by mutableStateOf(initialState.vibrateOnTap)
    var vibrateOnResult by mutableStateOf(initialState.vibrateOnResult)

    val currentState: UserPreferences.HapticFeedback
        get() = UserPreferences.HapticFeedback(
            vibrateOnTap = vibrateOnTap,
            vibrateOnResult = vibrateOnResult
        )

    companion object {
        val Saver: Saver<HapticFeedbackDialogState, *> = listSaver(
            save = {
                listOf(
                    it.vibrateOnTap,
                    it.vibrateOnResult
                )
            },
            restore = {
                HapticFeedbackDialogState(
                    initialState = UserPreferences.HapticFeedback(
                        vibrateOnTap = it[0],
                        vibrateOnResult = it[1]
                    )
                )
            }
        )
    }

}

@Composable
internal fun rememberHapticFeedbackDialogState(
    hapticFeedback: UserPreferences.HapticFeedback,
): HapticFeedbackDialogState {
    return rememberSaveable(
        inputs = arrayOf(hapticFeedback),
        saver = HapticFeedbackDialogState.Saver
    ) {
        HapticFeedbackDialogState(
            initialState = hapticFeedback
        )
    }
}

@Composable
internal fun HapticFeedbackDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    dialogState: HapticFeedbackDialogState,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.vibration_feedback_dialog_title))
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = stringResource(StringsR.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(StringsR.string.vibration_feedback_dialog_message)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = stringResource(StringsR.string.vibrate_on_tap),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = dialogState.vibrateOnTap,
                        onCheckedChange = { dialogState.vibrateOnTap = it }
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(StringsR.string.vibrate_on_result),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = dialogState.vibrateOnResult,
                        onCheckedChange = { dialogState.vibrateOnResult = it }
                    )
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}