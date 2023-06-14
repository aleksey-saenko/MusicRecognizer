package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.feature.preferences.domain.ScheduleAction
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal class SchedulePolicyDialogState(
    initialState: UserPreferences.SchedulePolicy,
) {
    var noMatches by mutableStateOf(initialState.noMatches)
    var badConnection by mutableStateOf(initialState.badConnection)
    var anotherFailure by mutableStateOf(initialState.anotherFailure)

    val currentState: UserPreferences.SchedulePolicy
        get() = UserPreferences.SchedulePolicy(
            noMatches = noMatches,
            badConnection = badConnection,
            anotherFailure = anotherFailure
        )

    companion object {
        val Saver: Saver<SchedulePolicyDialogState, *> = listSaver(
            save = {
                listOf(
                    it.noMatches.ordinal,
                    it.badConnection.ordinal,
                    it.anotherFailure.ordinal
                )
            },
            restore = {
                SchedulePolicyDialogState(
                    initialState = UserPreferences.SchedulePolicy(
                        noMatches = ScheduleAction.values()[it[0]],
                        badConnection = ScheduleAction.values()[it[1]],
                        anotherFailure = ScheduleAction.values()[it[2]]
                    )
                )
            }
        )
    }

}

@Composable
internal fun rememberSchedulePolicyDialogState(
    schedulePolicy: UserPreferences.SchedulePolicy,
): SchedulePolicyDialogState {
    return rememberSaveable(
        inputs = arrayOf(schedulePolicy),
        saver = SchedulePolicyDialogState.Saver
    ) {
        SchedulePolicyDialogState(
            initialState = schedulePolicy
        )
    }
}


@Composable
internal fun SchedulePolicyDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    dialogState: SchedulePolicyDialogState,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.schedule_policy))
        },
        confirmButton = {
            Button(onClick = onConfirmClick) {
                Text(text = stringResource(StringsR.string.apply))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.cancel))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(StringsR.string.schedule_policy_dialog_message)
                )
                ScheduleActionsDropdownMenu(
                    options = allOptions,
                    label = stringResource(StringsR.string.bad_internet_connection),
                    selectedOption = dialogState.badConnection,
                    onSelectOption = { option -> dialogState.badConnection = option },
                    modifier = Modifier.padding(top = 16.dp)
                )
                ScheduleActionsDropdownMenu(
                    options = ignoreOrSaveOptions,
                    label = stringResource(StringsR.string.no_matches_found),
                    selectedOption = dialogState.noMatches,
                    onSelectOption = { option -> dialogState.noMatches = option },
                    modifier = Modifier.padding(top = 16.dp)
                )
                ScheduleActionsDropdownMenu(
                    options = ignoreOrSaveOptions,
                    label = stringResource(StringsR.string.other_failures),
                    selectedOption = dialogState.anotherFailure,
                    onSelectOption = { option -> dialogState.anotherFailure = option },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        onDismissRequest = onDismissClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleActionsDropdownMenu(
    modifier: Modifier = Modifier,
    label: String,
    options: ImmutableList<ScheduleAction>,
    selectedOption: ScheduleAction,
    onSelectOption: (ScheduleAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = selectedOption.getTitle(),
            onValueChange = {},
            label = { Text(text = label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.getTitle()) },
                    onClick = {
                        onSelectOption(selectionOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

private val allOptions = ScheduleAction.values().asList().toImmutableList()
private val ignoreOrSaveOptions =
    listOf(ScheduleAction.Ignore, ScheduleAction.Save).toImmutableList()

@Composable
private fun ScheduleAction.getTitle(): String {
    return when (this) {
        ScheduleAction.Ignore -> stringResource(StringsR.string.ignore)
        ScheduleAction.Save -> stringResource(StringsR.string.save_the_recording)
        ScheduleAction.SaveAndLaunch -> stringResource(StringsR.string.save_the_recording_and_launch_retry)
    }
}