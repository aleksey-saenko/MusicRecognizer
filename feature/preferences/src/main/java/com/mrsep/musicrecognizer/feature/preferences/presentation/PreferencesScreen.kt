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
import com.mrsep.musicrecognizer.feature.preferences.domain.MusicService
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceGroup
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceSwitchItem
import kotlinx.collections.immutable.ImmutableList
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
                .systemBarsPadding()
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
                            subtitle = stringResource(StringsR.string.fallback_policy_pref_subtitle)
                        ) {
                            showPolicyDialog = true
                        }
                        if (showPolicyDialog) {
                            FallbackPolicyDialog(
                                fallbackPolicy = uiState.preferences.fallbackPolicy,
                                onFallbackPolicyChanged = viewModel::setFallbackPolicy,
                                onDismissClick = { showPolicyDialog = false }
                            )
                        }

                        var showTokenDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(title = stringResource(StringsR.string.audd_api_token)) {
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
                            title = stringResource(StringsR.string.theme),
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
                        val requiredMusicServices = uiState.preferences.requiredMusicServices
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.music_services_links),
                            subtitle = requiredMusicServices.getEnumerationForSubtitle(limit = 3),
                            onItemClick = { showServicesDialog = true }
                        )
                        if (showServicesDialog) {
                            RequiredServicesDialog(
                                modifier = Modifier.fillMaxHeight(0.9f),
                                requiredServices = requiredMusicServices,
                                onRequiredServicesChanged = viewModel::setRequiredMusicServices,
                                onDismissClick = { showServicesDialog = false }
                            )
                        }
                        PreferenceSwitchItem(
                            title = stringResource(StringsR.string.use_list_for_library),
                            onCheckedChange = { viewModel.setUseColumnForLibrary(it) },
                            checked = uiState.preferences.useColumnForLibrary
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
                                HapticFeedbackDialog(
                                    hapticFeedback = uiState.preferences.hapticFeedback,
                                    onHapticFeedbackChanged = viewModel::setHapticFeedback,
                                    onDismissClick = { showHapticDialog = false },
                                )
                            }
                        }
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.about),
                            onItemClick = onNavigateToAboutScreen
                        )
                        if (showDeveloperOptions) {
                            PreferenceClickableItem(
                                title = stringResource(StringsR.string.developer_options),
                                onItemClick = onNavigateToDeveloperScreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Stable
@Composable
private fun ImmutableList<MusicService>.getEnumerationForSubtitle(limit: Int) = when (size) {
    0 -> stringResource(StringsR.string.none)
    in 1..limit -> {
        map { service -> stringResource(service.titleId()) }.joinToString(", ")
    }
    else -> {
        take(3).map { service -> stringResource(service.titleId()) }.joinToString(", ")
            .plus(stringResource(StringsR.string.format_more_services, size - limit))
    }
}