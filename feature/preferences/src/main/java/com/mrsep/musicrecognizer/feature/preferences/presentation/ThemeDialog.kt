package com.mrsep.musicrecognizer.feature.preferences.presentation

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.DialogRadioButton
import com.mrsep.musicrecognizer.core.ui.components.DialogSwitch
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun ThemeDialog(
    onDismissClick: () -> Unit,
    themeMode: ThemeMode,
    useDynamicColors: Boolean,
    useArtworkBasedTheme: Boolean,
    usePureBlackForDarkTheme: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorsEnabled: (Boolean) -> Unit,
    onArtworkBasedThemeEnabled: (Boolean) -> Unit,
    onPureBlackEnabled: (Boolean) -> Unit,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.theme))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column {
                Text(text = stringResource(StringsR.string.theme_dialog_message))
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    DialogRadioButton(
                        title = stringResource(StringsR.string.theme_follow_system),
                        selected = themeMode == ThemeMode.FollowSystem,
                        onClick = { onThemeModeSelected(ThemeMode.FollowSystem) }
                    )
                    DialogRadioButton(
                        title = stringResource(StringsR.string.theme_light),
                        selected = themeMode == ThemeMode.AlwaysLight,
                        onClick = { onThemeModeSelected(ThemeMode.AlwaysLight) }
                    )
                    DialogRadioButton(
                        title = stringResource(StringsR.string.theme_dark),
                        selected = themeMode == ThemeMode.AlwaysDark,
                        onClick = { onThemeModeSelected(ThemeMode.AlwaysDark) }
                    )
                    Column(
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            DialogSwitch(
                                title = stringResource(StringsR.string.theme_use_dynamic_colors),
                                checked = useDynamicColors,
                                onClick = { onDynamicColorsEnabled(!useDynamicColors) }
                            )
                        }
                        DialogSwitch(
                            title = stringResource(StringsR.string.theme_use_artwork_colors),
                            checked = useArtworkBasedTheme,
                            onClick = { onArtworkBasedThemeEnabled(!useArtworkBasedTheme) }
                        )
                        if (themeMode != ThemeMode.AlwaysLight) {
                            DialogSwitch(
                                modifier = Modifier,
                                title = stringResource(StringsR.string.theme_use_pure_black),
                                checked = usePureBlackForDarkTheme,
                                onClick = { onPureBlackEnabled(!usePureBlackForDarkTheme) }
                            )
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}