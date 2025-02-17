package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.domain.preferences.HapticFeedback
import com.mrsep.musicrecognizer.core.ui.components.DialogSwitch
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun HapticFeedbackDialog(
    hapticFeedback: HapticFeedback,
    onHapticFeedbackChanged: (HapticFeedback) -> Unit,
    onDismissClick: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.vibration_feedback_dialog_title))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column {
                Text(text = stringResource(StringsR.string.vibration_feedback_dialog_message))
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    DialogSwitch(
                        title = stringResource(StringsR.string.vibration_feedback_dialog_vibrate_on_tap),
                        checked = hapticFeedback.vibrateOnTap,
                        onClick = {
                            val newOptions = hapticFeedback.copy(
                                vibrateOnTap = !hapticFeedback.vibrateOnTap
                            )
                            onHapticFeedbackChanged(newOptions)
                        }
                    )
                    DialogSwitch(
                        title = stringResource(StringsR.string.vibration_feedback_dialog_vibrate_on_result),
                        checked = hapticFeedback.vibrateOnResult,
                        onClick = {
                            val newOptions = hapticFeedback.copy(
                                vibrateOnResult = !hapticFeedback.vibrateOnResult
                            )
                            onHapticFeedbackChanged(newOptions)
                        }
                    )
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}
