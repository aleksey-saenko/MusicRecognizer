package com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.R
import com.mrsep.musicrecognizer.feature.preferences.domain.RecognitionProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun AuddServiceDialog(
    modifier: Modifier = Modifier,
    state: AuddPreferencesState,
    currentProvider: RecognitionProvider,
    onProviderChanged: (RecognitionProvider) -> Unit,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    RecognitionServiceDialogBase(
        modifier = modifier,
        currentProvider = currentProvider,
        onProviderChanged = onProviderChanged,
        onSaveClick = onSaveClick,
        onDismissClick = onDismissClick,
    ) {
        AuddPreferences(state = state)
    }
}

@Composable
internal fun AcrCloudServiceDialog(
    modifier: Modifier = Modifier,
    state: AcrCloudPreferencesState,
    currentProvider: RecognitionProvider,
    onProviderChanged: (RecognitionProvider) -> Unit,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    RecognitionServiceDialogBase(
        modifier = modifier,
        currentProvider = currentProvider,
        onProviderChanged = onProviderChanged,
        onSaveClick = onSaveClick,
        onDismissClick = onDismissClick,
    ) {
        AcrCloudPreferences(state = state)
    }
}

@Composable
private fun RecognitionServiceDialogBase(
    modifier: Modifier = Modifier,
    currentProvider: RecognitionProvider,
    onProviderChanged: (RecognitionProvider) -> Unit,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit,
    serviceConfiguration: @Composable () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = stringResource(StringsR.string.recognition_provider_preference_title))
        },
        confirmButton = {
            TextButton(onClick = onSaveClick) {
                Text(text = stringResource(StringsR.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(StringsR.string.recognition_service_dialog_message)
                )
                RecognitionProviderDropdownMenu(
                    options = RecognitionProvider.entries.toImmutableList(),
                    label = stringResource(StringsR.string.current_recognition_provider),
                    selectedOption = currentProvider,
                    onSelectOption = onProviderChanged,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                serviceConfiguration()
            }
        },
        onDismissRequest = onDismissClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecognitionProviderDropdownMenu(
    modifier: Modifier = Modifier,
    label: String,
    options: ImmutableList<RecognitionProvider>,
    selectedOption: RecognitionProvider,
    onSelectOption: (RecognitionProvider) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // workaround to change hardcoded shape of menu https://issuetracker.google.com/issues/283654243
    MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.small)) {
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
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                shape = MaterialTheme.shapes.small
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.getTitle()) },
                        onClick = {
                            expanded = false
                            onSelectOption(selectionOption)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
internal fun AuthenticationRow(
    modifier: Modifier = Modifier,
    serviceName: String,
    onHelpClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(StringsR.string.format_service_auth, serviceName))
        IconButton(onClick = onHelpClick) {
            Icon(
                painter = painterResource(R.drawable.baseline_help_24),
                contentDescription = stringResource(StringsR.string.help),
            )
        }
    }
}

@Stable
@Composable
internal fun RecognitionProvider.getTitle() = when (this) {
    RecognitionProvider.Audd -> stringResource(StringsR.string.audd)
    RecognitionProvider.AcrCloud -> stringResource(
        StringsR.string.format_experimantal,
        stringResource(StringsR.string.acr_cloud)
    )
}