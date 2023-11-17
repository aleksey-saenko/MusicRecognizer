package com.mrsep.musicrecognizer.feature.preferences.presentation

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Text(text = stringResource(StringsR.string.theme_dialog_title))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(StringsR.string.theme_dialog_message)
                )
                Spacer(modifier = Modifier.height(16.dp))
                ChooserRow(
                    text = stringResource(StringsR.string.theme_follow_system),
                    selected = themeMode == ThemeMode.FollowSystem,
                    onClick = { onThemeModeSelected(ThemeMode.FollowSystem) }
                )
                ChooserRow(
                    text = stringResource(StringsR.string.theme_always_light),
                    selected = themeMode == ThemeMode.AlwaysLight,
                    onClick = { onThemeModeSelected(ThemeMode.AlwaysLight) }
                )
                ChooserRow(
                    text = stringResource(StringsR.string.theme_always_dark),
                    selected = themeMode == ThemeMode.AlwaysDark,
                    onClick = { onThemeModeSelected(ThemeMode.AlwaysDark) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    DialogSwitch(
                        title = stringResource(StringsR.string.theme_use_dynamic_colors),
                        checked = useDynamicColors,
                        onCheckedChange = { onDynamicColorsEnabled(it) }
                    )
                }
                DialogSwitch(
                    title = stringResource(StringsR.string.theme_use_artwork_colors),
                    checked = useArtworkBasedTheme,
                    onCheckedChange = onArtworkBasedThemeEnabled
                )
                if (themeMode != ThemeMode.AlwaysLight) {
                    DialogSwitch(
                        modifier = Modifier,
                        title = stringResource(StringsR.string.theme_use_pure_black),
                        checked = usePureBlackForDarkTheme,
                        onCheckedChange = onPureBlackEnabled
                    )
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}

@Composable
private fun DialogSwitch(
    modifier: Modifier = Modifier,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(vertical = 8.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp)
        )
    }
}