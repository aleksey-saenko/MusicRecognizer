package com.mrsep.musicrecognizer.feature.preferences.presentation

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.DialogSwitch
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

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
                Spacer(Modifier.height(4.dp))
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(StringsR.string.theme_mode_short),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = themeMode.title(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.alpha(0.8f)
                            )
                        }
                        FilledIconToggleButton(
                            checked = themeMode == ThemeMode.FollowSystem,
                            onCheckedChange = { onThemeModeSelected(ThemeMode.FollowSystem) }
                        ) {
                            Icon(
                                painter = painterResource(UiR.drawable.outline_auto_mode_24),
                                contentDescription = stringResource(StringsR.string.theme_follow_system_short)
                            )
                        }
                        FilledIconToggleButton(
                            checked = themeMode == ThemeMode.AlwaysLight,
                            onCheckedChange = { onThemeModeSelected(ThemeMode.AlwaysLight) }
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (themeMode == ThemeMode.AlwaysLight) {
                                        UiR.drawable.outline_light_mode_fill1_24
                                    } else {
                                        UiR.drawable.outline_light_mode_24
                                    }
                                ),
                                contentDescription = stringResource(StringsR.string.theme_light_short)
                            )
                        }
                        FilledIconToggleButton(
                            checked = themeMode == ThemeMode.AlwaysDark,
                            onCheckedChange = { onThemeModeSelected(ThemeMode.AlwaysDark) }
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (themeMode == ThemeMode.AlwaysDark) {
                                        UiR.drawable.outline_dark_mode_fill1_24
                                    } else {
                                        UiR.drawable.outline_dark_mode_24
                                    }
                                ),
                                contentDescription = stringResource(StringsR.string.theme_dark_short)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Column {
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
                        DialogSwitch(
                            title = stringResource(StringsR.string.theme_use_pure_black),
                            checked = usePureBlackForDarkTheme,
                            onClick = { onPureBlackEnabled(!usePureBlackForDarkTheme) },
                            enabled = themeMode != ThemeMode.AlwaysLight
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}

@Stable
@Composable
private fun ThemeMode.title() = when (this) {
    ThemeMode.FollowSystem -> stringResource(StringsR.string.theme_follow_system_short)
    ThemeMode.AlwaysLight -> stringResource(StringsR.string.theme_light_short)
    ThemeMode.AlwaysDark -> stringResource(StringsR.string.theme_dark_short)
}
