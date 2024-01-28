package com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.PasswordInputField
import com.mrsep.musicrecognizer.feature.preferences.domain.AcrCloudConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun AcrCloudPreferences(
    state: AcrCloudPreferencesState,
    modifier: Modifier = Modifier
) {
    var helpDialogVisible by rememberSaveable { mutableStateOf(false) }
    if (helpDialogVisible) {
        AcrCloudHelpDialog { helpDialogVisible = false }
    }
    Column(modifier = modifier) {
        AuthenticationRow(
            modifier = Modifier.fillMaxWidth(),
            serviceName = stringResource(StringsR.string.acr_cloud),
            onHelpClick = { helpDialogVisible = true }
        )
        val showHostError = state.isEmptyHost && state.shouldShowErrors.value
        AcrCloudHostDropdownMenu(
            label = stringResource(StringsR.string.host),
            options = AcrCloudRegion.entries.toImmutableList(),
            host = state.host.value,
            onHostChanged = { newHost -> state.host.value = newHost },
            error = stringResource(StringsR.string.must_not_be_empty)
                .takeIf { showHostError },
        )
        Spacer(modifier = Modifier.height(12.dp))
        val showAccessKeyError = state.isEmptyAccessKey && state.shouldShowErrors.value
        PasswordInputField(
            value = state.accessKey.value,
            onValueChange = { newAccessKey -> state.accessKey.value = newAccessKey },
            label = stringResource(StringsR.string.access_key),
            error = stringResource(StringsR.string.must_not_be_empty)
                .takeIf { showAccessKeyError },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        val showAccessSecretError = state.isEmptyAccessSecret && state.shouldShowErrors.value
        PasswordInputField(
            value = state.accessSecret.value,
            onValueChange = { newAccessSecret -> state.accessSecret.value = newAccessSecret },
            label = stringResource(StringsR.string.access_secret),
            error = stringResource(StringsR.string.must_not_be_empty)
                .takeIf { showAccessSecretError },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AcrCloudHostDropdownMenu(
    modifier: Modifier = Modifier,
    label: String,
    options: ImmutableList<AcrCloudRegion>,
    host: String,
    onHostChanged: (String) -> Unit,
    error: String? = null
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
                value = host,
                onValueChange = onHostChanged,
                label = { Text(text = label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                singleLine = true,
                supportingText = error?.let { { Text(error) } },
                isError = (error != null),
                shape = MaterialTheme.shapes.small,
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption.title) },
                        onClick = {
                            onHostChanged(selectionOption.host)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

internal enum class AcrCloudRegion(
    val host: String,
    val title: String
) {
    EUROPE(
        "identify-eu-west-1.acrcloud.com",
        "Europe"
    ),
    US_WEST(
        "identify-us-west-2.acrcloud.com",
        "US West"
    ),
    ASIA_PACIFIC(
        "identify-ap-southeast-1.acrcloud.com",
        "Asia Pacific"
    )
}

@Stable
internal class AcrCloudPreferencesState(config: AcrCloudConfig) {
    val host = mutableStateOf(config.host)
    val accessKey = mutableStateOf(config.accessKey)
    val accessSecret = mutableStateOf(config.accessSecret)

    var shouldShowErrors = mutableStateOf(false)

    val currentConfig
        get() = AcrCloudConfig(
            host = host.value,
            accessKey = accessKey.value,
            accessSecret = accessSecret.value,
        )

    val isConfigValid
        get() = host.value.isNotBlank() &&
                accessKey.value.isNotBlank() &&
                accessSecret.value.isNotBlank()

    val isEmptyHost
        get() = host.value.isBlank()

    val isEmptyAccessKey
        get() = accessKey.value.isBlank()

    val isEmptyAccessSecret
        get() = accessSecret.value.isBlank()

    fun showErrors() { shouldShowErrors.value = true }

    companion object {
        val Saver: Saver<AcrCloudPreferencesState, *> = listSaver(
            save = { state ->
                listOf(
                    state.host.value,
                    state.accessKey.value,
                    state.accessSecret.value,
                )
            },
            restore = { saved ->
                AcrCloudPreferencesState(
                    config = AcrCloudConfig(
                        host = saved[0],
                        accessKey = saved[1],
                        accessSecret = saved[2],
                    )
                )
            }
        )
    }
}

@Composable
internal fun rememberAcrCloudPreferencesState(config: AcrCloudConfig): AcrCloudPreferencesState {
    return rememberSaveable(
        inputs = arrayOf(config),
        saver = AcrCloudPreferencesState.Saver
    ) {
        AcrCloudPreferencesState(config)
    }
}