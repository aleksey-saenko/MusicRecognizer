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
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel(),
    onNavigateToAboutScreen: () -> Unit,
    onNavigateToQueueScreen: () -> Unit
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
                    PreferenceGroup(title = "Recognition") {
                        PreferenceClickableItem(
                            title = "Recognition queue",
                            subtitle = "Manage your saved recognitions",
                            onItemClick = onNavigateToQueueScreen
                        )
                    }
                    PreferenceGroup(title = "Developer options",
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        PreferenceSwitchItem(
                            title = "Enable developer mode",
                            subtitle = "Only for dev purpose",
                            onCheckedChange = { viewModel.setDeveloperModeEnabled(it) },
                            checked = uiState.preferences.developerModeEnabled
                        )
                        PreferenceSwitchItem(
                            title = "Should show onboarding",
                            subtitle = "Only for dev purpose",
                            onCheckedChange = { viewModel.setOnboardingCompleted(!it) },
                            checked = !uiState.preferences.onboardingCompleted,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                    }
                    PreferenceGroup(title = "UI", modifier = Modifier.padding(top = 16.dp)) {
                        var showDialog by rememberSaveable { mutableStateOf(false) }
                        PreferenceClickableItem(
                            title = "Show links to music services",
                            subtitle = uiState.preferences.requiredServices.getNames()
                        ) {
                            showDialog = true
                        }
                        if (showDialog) {

                            val dialogState = rememberRequiredServicesDialogState(
                                requiredServices = uiState.preferences.requiredServices
                            )
                            RequiredServicesDialog(
                                onConfirmClick = {
                                    showDialog = false
                                    viewModel.setRequiredServices(dialogState.currentState)
                                },
                                onDismissClick = { showDialog = false },
                                dialogState = dialogState
                            )

                        }
                        PreferenceSwitchItem(
                            title = "Use dynamic colors",
                            onCheckedChange = { viewModel.setDynamicColorsEnabled(it) },
                            checked = uiState.preferences.dynamicColorsEnabled,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    PreferenceGroup(
                        title = "Notifications",
                        modifier = Modifier.padding(top = 16.dp)
                    ) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val notificationPermissionState = rememberPermissionState(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            PreferenceSwitchItem(
                                title = "Notification service",
                                subtitle = "Allow to control recognition from notifications",
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (notificationPermissionState.status.isGranted) {
                                            viewModel.setNotificationServiceEnabled(true)
                                        } else {
                                            notificationPermissionState.launchPermissionRequest()
                                            scope.launch {
                                                snapshotFlow { notificationPermissionState.status.isGranted }
                                                    .filter { it }
                                                    .take(1)
                                                    .collect {
                                                        viewModel.setNotificationServiceEnabled(true)
                                                    }
                                            }
                                        }
                                    } else {
                                        viewModel.setNotificationServiceEnabled(false)
                                    }
                                },
                                checked = uiState.preferences.notificationServiceEnabled
                            )
                        } else {
                            PreferenceSwitchItem(
                                title = "Notification service",
                                subtitle = "Allow to control recognition from notifications",
                                onCheckedChange = { checked ->
                                    viewModel.setNotificationServiceEnabled(checked)
                                },
                                checked = uiState.preferences.notificationServiceEnabled
                            )
                        }
                    }
                    PreferenceGroup(title = "Misc", modifier = Modifier.padding(top = 16.dp)) {
                        PreferenceClickableItem(
                            title = "About",
                            onItemClick = onNavigateToAboutScreen
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