package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.common.util.getDefaultVibrator
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceGroup
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceSwitchItem
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreferencesScreen(
    viewModel: PreferencesViewModel = hiltViewModel(),
    showDeveloperOptions: Boolean,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToQueueScreen: () -> Unit,
    onNavigateToDeveloperScreen: () -> Unit
) {
    val context = LocalContext.current
    val uiStateInFlow by viewModel.uiFlow.collectAsStateWithLifecycle()
    val topBarBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    when (val uiState = uiStateInFlow) {
        is PreferencesUiState.Loading -> LoadingStub(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        )

        is PreferencesUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                PreferencesTopBar(topAppBarScrollBehavior = topBarBehaviour)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(topBarBehaviour.nestedScrollConnection)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                ) {
                    PreferenceGroup(title = stringResource(StringsR.string.recognition)) {
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.recognition_queue),
                            subtitle = stringResource(StringsR.string.recognition_queue_pref_subtitle),
                            onItemClick = onNavigateToQueueScreen
                        )

                        var showPolicyDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.fallback_policy),
                            subtitle = stringResource(StringsR.string.fallback_policy_pref_subtitle),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            showPolicyDialog = true
                        }
                        if (showPolicyDialog) {
                            val dialogState = rememberFallbackPolicyDialogState(
                                fallbackPolicy = uiState.preferences.fallbackPolicy
                            )
                            FallbackPolicyDialog(
                                onConfirmClick = {
                                    viewModel.setFallbackPolicy(dialogState.currentState)
                                    showPolicyDialog = false
                                },
                                onDismissClick = { showPolicyDialog = false },
                                dialogState = dialogState
                            )

                        }

                        var showTokenDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.audd_api_token),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            showTokenDialog = true
                        }
                        if (showTokenDialog) {
                            ApiTokenDialog(
                                onConfirmClick = { newToken ->
                                    viewModel.setApiToken(newToken)
                                    showTokenDialog = false
                                },
                                onDismissClick = { showTokenDialog = false },
                                initialToken = uiState.preferences.apiToken
                            )
                        }
                    }

                    PreferenceGroup(
                        title = stringResource(StringsR.string.notifications),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        NotificationServiceSwitch(
                            serviceEnabled = uiState.preferences.notificationServiceEnabled,
                            toggleServiceState = viewModel::setNotificationServiceEnabled
                        )
                    }

                    PreferenceGroup(
                        title = stringResource(StringsR.string.appearance),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        var showThemeDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.theme_preference_title),
                            onItemClick = { showThemeDialog = true }
                        )
                        if (showThemeDialog) {
                            ThemeDialog(
                                onDismissClick = { showThemeDialog = false },
                                themeMode = uiState.preferences.themeMode,
                                useDynamicColors = uiState.preferences.dynamicColorsEnabled,
                                useArtworkBasedTheme = uiState.preferences.artworkBasedThemeEnabled,
                                usePureBlackForDarkTheme = uiState.preferences.usePureBlackForDarkTheme,
                                onThemeModeSelected = viewModel::setThemeMode,
                                onDynamicColorsEnabled = viewModel::setDynamicColorsEnabled,
                                onPureBlackEnabled = viewModel::setUsePureBlackForDarkTheme,
                                onArtworkBasedThemeEnabled = viewModel::setArtworkBasedThemeEnabled
                            )
                        }
                        var showServicesDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.show_links_to_music_services),
                            subtitle = uiState.preferences.requiredServices.getNames(),
                            onItemClick = { showServicesDialog = true },
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        if (showServicesDialog) {
                            val dialogState = rememberRequiredServicesDialogState(
                                requiredServices = uiState.preferences.requiredServices
                            )
                            RequiredServicesDialog(
                                onConfirmClick = {
                                    viewModel.setRequiredServices(dialogState.currentState)
                                    showServicesDialog = false
                                },
                                onDismissClick = { showServicesDialog = false },
                                dialogState = dialogState
                            )
                        }
                        PreferenceSwitchItem(
                            title = stringResource(StringsR.string.use_grid_for_library),
                            onCheckedChange = { viewModel.setUseGridForLibrary(it) },
                            checked = uiState.preferences.useGridForLibrary,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    PreferenceGroup(
                        title = stringResource(StringsR.string.misc),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        val vibratorAvailable = remember {
                            context.getDefaultVibrator().hasVibrator()
                        }
                        if (vibratorAvailable) {
                            var showHapticDialog by rememberSaveable { mutableStateOf(false) }
                            PreferenceClickableItem(
                                title = stringResource(StringsR.string.vibration_feedback),
                                onItemClick = { showHapticDialog = true }
                            )
                            if (showHapticDialog) {
                                val dialogState = rememberHapticFeedbackDialogState(
                                    hapticFeedback = uiState.preferences.hapticFeedback
                                )
                                HapticFeedbackDialog(
                                    onConfirmClick = {
                                        viewModel.setHapticFeedback(dialogState.currentState)
                                        showHapticDialog = false
                                    },
                                    onDismissClick = { showHapticDialog = false },
                                    dialogState = dialogState
                                )
                            }
                        }
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.about),
                            onItemClick = onNavigateToAboutScreen,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        if (showDeveloperOptions) {
                            PreferenceClickableItem(
                                title = stringResource(StringsR.string.developer_options),
                                onItemClick = onNavigateToDeveloperScreen,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserPreferences.RequiredServices.getNames() =
    listOf(
        stringResource(StringsR.string.spotify) to spotify,
        stringResource(StringsR.string.youtube) to youtube,
        stringResource(StringsR.string.soundcloud) to soundCloud,
        stringResource(StringsR.string.apple_music) to appleMusic,
        stringResource(StringsR.string.deezer) to deezer,
        stringResource(StringsR.string.napster) to napster,
        stringResource(StringsR.string.musicbrainz) to musicbrainz
    ).filter { it.second }
        .joinToString(", ") { it.first }
        .ifEmpty { stringResource(StringsR.string.none) }