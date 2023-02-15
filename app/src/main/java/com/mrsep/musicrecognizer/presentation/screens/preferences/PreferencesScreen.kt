package com.mrsep.musicrecognizer.presentation.screens.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import com.mrsep.musicrecognizer.presentation.common.LoadingStub
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceGroup
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceSwitchItem

@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val uiFlowState by viewModel.uiFlow.collectAsStateWithLifecycle(PreferencesUiState.Loading)
    when (val uiState = uiFlowState) {
        is PreferencesUiState.Loading -> LoadingStub()
        is PreferencesUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.preferences).toUpperCase(Locale.current),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                PreferenceGroup(title = "UI") {
                    PreferenceSwitchItem(
                        title = "Should show onboarding",
                        subtitle = "Only for dev purpose",
                        onCheckedChange = { viewModel.setOnboardingCompleted(!it) },
                        checked = !uiState.preferences.onboardingCompleted
                    )
                }
                PreferenceGroup(title = "DATA", modifier = Modifier.padding(top = 16.dp)) {
                    var showDialog by rememberSaveable { mutableStateOf(false) }
                    PreferenceClickableItem(
                        title = "Show links to music services",
                        subtitle = uiState.preferences.visibleLinks.getNames()
                    ) {
                        showDialog = true
                    }
                    if (showDialog) {

                        val dialogState = rememberVisibleLinksDialogState(
                            visibleLinks = uiState.preferences.visibleLinks
                        )
                        VisibleLinksDialog(
                            onConfirmClick = {
                                showDialog = false
                                viewModel.setVisibleLinks(dialogState.currentState)
                            },
                            onDismissClick = { showDialog = false },
                            dialogState = dialogState
                        )

                    }
                }
            }
        }
    }
}

@Composable
private fun UserPreferences.VisibleLinks.getNames() =
    listOf(
        stringResource(R.string.spotify) to spotify,
        stringResource(R.string.apple_music) to appleMusic,
        stringResource(R.string.deezer) to deezer,
        stringResource(R.string.napster) to napster,
        stringResource(R.string.musicbrainz) to musicbrainz
    ).filter { it.second }
        .joinToString(", ") { it.first }
        .ifEmpty { stringResource(R.string.none) }