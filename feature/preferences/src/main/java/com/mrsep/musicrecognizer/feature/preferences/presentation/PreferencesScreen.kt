package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.mrsep.musicrecognizer.feature.preferences.domain.RecognitionProvider
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceGroup
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceSwitchItem
import com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig.AcrCloudServiceDialog
import com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig.AuddServiceDialog
import com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig.getTitle
import com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig.rememberAcrCloudPreferencesState
import com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig.rememberAuddPreferencesState
import kotlinx.collections.immutable.ImmutableList
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PreferencesScreen(
    viewModel: PreferencesViewModel = hiltViewModel(),
    showDeveloperOptions: Boolean,
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToDeveloperScreen: () -> Unit
) {
    val context = LocalContext.current
    val uiStateInFlow by viewModel.uiFlow.collectAsStateWithLifecycle()
    val topBarBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    when (val uiState = uiStateInFlow) {
        is PreferencesUiState.Loading -> LoadingStub(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .statusBarsPadding()
        )

        is PreferencesUiState.Success -> {
            Column(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            ) {
                PreferencesTopBar(scrollBehavior = topBarBehaviour)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(topBarBehaviour.nestedScrollConnection)
                        .verticalScroll(rememberScrollState())
                ) {
                    PreferenceGroup(title = stringResource(StringsR.string.recognition)) {
                        val currentProvider = uiState.preferences.currentRecognitionProvider
                        var showServiceDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.recognition_provider_preference_title),
                            subtitle = uiState.preferences.currentRecognitionProvider.getTitle(),
                            onItemClick = { showServiceDialog = true }
                        )
                        if (showServiceDialog) {
                            var visibleProvider by rememberSaveable(currentProvider) {
                                mutableStateOf(currentProvider)
                            }
                            when (visibleProvider) {
                                RecognitionProvider.Audd -> {
                                    val state = rememberAuddPreferencesState(
                                        uiState.preferences.auddConfig
                                    )
                                    AuddServiceDialog(
                                        state = state,
                                        currentProvider = visibleProvider,
                                        onProviderChanged = { visibleProvider = it },
                                        onSaveClick = {
                                            viewModel.setAuddConfig(state.currentConfig)
                                            viewModel.setRecognitionProvider(visibleProvider)
                                            showServiceDialog = false
                                        },
                                        onDismissClick = { showServiceDialog = false }
                                    )
                                }

                                RecognitionProvider.AcrCloud -> {
                                    val state = rememberAcrCloudPreferencesState(
                                        uiState.preferences.acrCloudConfig
                                    )
                                    AcrCloudServiceDialog(
                                        state = state,
                                        currentProvider = visibleProvider,
                                        onProviderChanged = { visibleProvider = it },
                                        onSaveClick = {
                                            if (state.isConfigValid) {
                                                viewModel.setAcrCloudConfig(state.currentConfig)
                                                viewModel.setRecognitionProvider(visibleProvider)
                                                showServiceDialog = false
                                            } else {
                                                state.showErrors()
                                            }
                                        },
                                        onDismissClick = { showServiceDialog = false }
                                    )
                                }
                            }
                        }
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
                        PreferenceSwitchItem(
                            title = stringResource(StringsR.string.recognize_on_startup),
                            onClick = {
                                viewModel.setRecognizeOnStartup(!uiState.preferences.recognizeOnStartup)
                            },
                            checked = uiState.preferences.recognizeOnStartup
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    PreferenceGroup(title = stringResource(StringsR.string.notifications)) {
                        NotificationServiceSwitch(
                            serviceEnabled = uiState.preferences.notificationServiceEnabled,
                            setServiceEnabled = viewModel::setNotificationServiceEnabled
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    PreferenceGroup(title = stringResource(StringsR.string.appearance)) {
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
                        val useGridForLibrary = uiState.preferences.useGridForLibrary
                        PreferenceSwitchItem(
                            title = stringResource(StringsR.string.use_grid_for_library),
                            onClick = { viewModel.setUseGridForLibrary(!useGridForLibrary) },
                            checked = useGridForLibrary
                        )
                        val useGridForQueue = uiState.preferences.useGridForRecognitionQueue
                        PreferenceSwitchItem(
                            title = stringResource(StringsR.string.use_grid_for_queue),
                            onClick = { viewModel.setUseGridForQueue(!useGridForQueue) },
                            checked = useGridForQueue
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    PreferenceGroup(title = stringResource(StringsR.string.misc)) {
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
                    Spacer(Modifier.height(16.dp))
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