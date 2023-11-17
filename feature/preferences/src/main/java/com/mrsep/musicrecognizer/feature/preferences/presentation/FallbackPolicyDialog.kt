package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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
import com.mrsep.musicrecognizer.feature.preferences.domain.FallbackAction
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
internal class FallbackPolicyDialogState(
    initialState: UserPreferences.FallbackPolicy,
) {
    var noMatches by mutableStateOf(initialState.noMatches)
    var badConnection by mutableStateOf(initialState.badConnection)
    var anotherFailure by mutableStateOf(initialState.anotherFailure)

    val currentState: UserPreferences.FallbackPolicy
        get() = UserPreferences.FallbackPolicy(
            noMatches = noMatches,
            badConnection = badConnection,
            anotherFailure = anotherFailure
        )

    companion object {
        val Saver: Saver<FallbackPolicyDialogState, *> = listSaver(
            save = {
                listOf(
                    it.noMatches.ordinal,
                    it.badConnection.ordinal,
                    it.anotherFailure.ordinal
                )
            },
            restore = {
                FallbackPolicyDialogState(
                    initialState = UserPreferences.FallbackPolicy(
                        noMatches = FallbackAction.values()[it[0]],
                        badConnection = FallbackAction.values()[it[1]],
                        anotherFailure = FallbackAction.values()[it[2]]
                    )
                )
            }
        )
    }

}

@Composable
internal fun rememberFallbackPolicyDialogState(
    fallbackPolicy: UserPreferences.FallbackPolicy,
): FallbackPolicyDialogState {
    return rememberSaveable(
        inputs = arrayOf(fallbackPolicy),
        saver = FallbackPolicyDialogState.Saver
    ) {
        FallbackPolicyDialogState(
            initialState = fallbackPolicy
        )
    }
}

@Composable
internal fun FallbackPolicyDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    dialogState: FallbackPolicyDialogState,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.fallback_policy_dialog_title))
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
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(StringsR.string.fallback_policy_dialog_message),
                )
                FallbackActionsDropdownMenu(
                    options = allOptions,
                    label = stringResource(StringsR.string.bad_internet_connection),
                    selectedOption = dialogState.badConnection,
                    onSelectOption = { option -> dialogState.badConnection = option },
                    modifier = Modifier.padding(top = 16.dp)
                )
                FallbackActionsDropdownMenu(
                    options = ignoreOrSaveOptions,
                    label = stringResource(StringsR.string.no_matches_found),
                    selectedOption = dialogState.noMatches,
                    onSelectOption = { option -> dialogState.noMatches = option },
                    modifier = Modifier.padding(top = 16.dp)
                )
                FallbackActionsDropdownMenu(
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
private fun FallbackActionsDropdownMenu(
    modifier: Modifier = Modifier,
    label: String,
    options: ImmutableList<FallbackAction>,
    selectedOption: FallbackAction,
    onSelectOption: (FallbackAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
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

private val allOptions = FallbackAction.values().asList().toImmutableList()
private val ignoreOrSaveOptions =
    listOf(FallbackAction.Ignore, FallbackAction.Save).toImmutableList()

@Composable
private fun FallbackAction.getTitle(): String {
    return when (this) {
        FallbackAction.Ignore -> stringResource(StringsR.string.ignore)
        FallbackAction.Save -> stringResource(StringsR.string.save_the_recording)
        FallbackAction.SaveAndLaunch -> stringResource(StringsR.string.save_the_recording_and_launch_retry)
    }
}