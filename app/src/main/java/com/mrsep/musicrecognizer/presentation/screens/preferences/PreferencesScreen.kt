package com.mrsep.musicrecognizer.presentation.screens.preferences

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import com.mrsep.musicrecognizer.presentation.common.LoadingStub
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceGroup
import com.mrsep.musicrecognizer.presentation.screens.preferences.common.PreferenceSwitchItem
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PreferencesScreen(
    modifier: Modifier = Modifier,
    viewModel: PreferencesViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiStateInFlow by viewModel.uiFlow.collectAsStateWithLifecycle(PreferencesUiState.Loading)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    when (val uiState = uiStateInFlow) {
        is PreferencesUiState.Loading -> LoadingStub()
        is PreferencesUiState.Success -> {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                Column(
                    modifier = modifier
//                        .fillMaxSize()
                        .weight(weight = 1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = stringResource(R.string.preferences).toUpperCase(Locale.current),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    PreferenceGroup(title = "Developer options") {
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
                            onItemClick = { }
                        )
                    }
                }
//                AppNavigationBar(
//                    visible = true,
//                    navController = navController
//                )
            }

        }
    }
}

@Composable
private fun UserPreferences.RequiredServices.getNames() =
    listOf(
        stringResource(R.string.spotify) to spotify,
        stringResource(R.string.apple_music) to appleMusic,
        stringResource(R.string.deezer) to deezer,
        stringResource(R.string.napster) to napster,
        stringResource(R.string.musicbrainz) to musicbrainz
    ).filter { it.second }
        .joinToString(", ") { it.first }
        .ifEmpty { stringResource(R.string.none) }