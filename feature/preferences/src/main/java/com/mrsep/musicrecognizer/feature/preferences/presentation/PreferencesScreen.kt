package com.mrsep.musicrecognizer.feature.preferences.presentation

import android.Manifest
import android.os.Build
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceGroup
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceSwitchItem
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel(),
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToQueueScreen: () -> Unit,
    onNavigateToDeveloperScreen: () -> Unit
) {
    val uiStateInFlow by viewModel.uiFlow.collectAsStateWithLifecycle(PreferencesUiState.Loading)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val topBarBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    when (val uiState = uiStateInFlow) {
        is PreferencesUiState.Loading -> LoadingStub()
        is PreferencesUiState.Success -> {
            Column {
                PreferencesTopBar(topAppBarScrollBehavior = topBarBehaviour)
                Column(
                    modifier = modifier
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
                            title = stringResource(StringsR.string.schedule_policy),
                            subtitle = stringResource(StringsR.string.schedule_policy_pref_subtitle),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            showPolicyDialog = true
                        }
                        if (showPolicyDialog) {

                            val dialogState = rememberSchedulePolicyDialogState(
                                schedulePolicy = uiState.preferences.schedulePolicy
                            )
                            SchedulePolicyDialog(
                                onConfirmClick = {
                                    showPolicyDialog = false
                                    viewModel.setSchedulePolicy(dialogState.currentState)
                                },
                                onDismissClick = { showPolicyDialog = false },
                                dialogState = dialogState
                            )

                        }
                    }

                    PreferenceGroup(
                        title = stringResource(StringsR.string.appearance),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        var showServicesDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(
                            title = "Show links to music services",
                            subtitle = uiState.preferences.requiredServices.getNames()
                        ) {
                            showServicesDialog = true
                        }
                        if (showServicesDialog) {

                            val dialogState = rememberRequiredServicesDialogState(
                                requiredServices = uiState.preferences.requiredServices
                            )
                            RequiredServicesDialog(
                                onConfirmClick = {
                                    showServicesDialog = false
                                    viewModel.setRequiredServices(dialogState.currentState)
                                },
                                onDismissClick = { showServicesDialog = false },
                                dialogState = dialogState
                            )

                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            PreferenceSwitchItem(
                                title = stringResource(StringsR.string.dynamic_colors_pref_title),
                                onCheckedChange = { viewModel.setDynamicColorsEnabled(it) },
                                checked = uiState.preferences.dynamicColorsEnabled,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                    PreferenceGroup(
                        title = stringResource(StringsR.string.notifications),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val notificationPermissionState = rememberPermissionState(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            PreferenceSwitchItem(
                                title = stringResource(StringsR.string.notification_service),
                                subtitle = stringResource(StringsR.string.notification_service_pref_subtitle),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (notificationPermissionState.status.isGranted) {
                                            viewModel.setNotificationServiceEnabled(true)
                                        } else {
                                            notificationPermissionState.launchPermissionRequest()
                                            snapshotFlow { notificationPermissionState.status.isGranted }
                                                .filter { it }
                                                .take(1)
                                                .onEach {
                                                    viewModel.setNotificationServiceEnabled(true)
                                                }
                                                .launchIn(scope)
                                        }
                                    } else {
                                        viewModel.setNotificationServiceEnabled(false)
                                    }
                                },
                                checked = uiState.preferences.notificationServiceEnabled
                            )
                        } else {
                            PreferenceSwitchItem(
                                title = stringResource(StringsR.string.notification_service),
                                subtitle = stringResource(StringsR.string.notification_service_pref_subtitle),
                                onCheckedChange = { checked ->
                                    viewModel.setNotificationServiceEnabled(checked)
                                },
                                checked = uiState.preferences.notificationServiceEnabled
                            )
                        }
                    }
                    PreferenceGroup(
                        title = stringResource(StringsR.string.misc),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.about),
                            onItemClick = onNavigateToAboutScreen
                        )
                        PreferenceClickableItem(
                            title = stringResource(StringsR.string.developer_options),
                            onItemClick = onNavigateToDeveloperScreen,
                            modifier = Modifier.padding(top = 16.dp)
                        )
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