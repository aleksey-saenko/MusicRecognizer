package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.track.domain.model.FontSize
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FontStyleBottomSheet(
    sheetState: SheetState,
    fontStyle: UserPreferences.LyricsFontStyle,
    onFontStyleChanged: (UserPreferences.LyricsFontStyle) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissClick,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Text(
            text = stringResource(StringsR.string.text_style),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = stringResource(StringsR.string.font_size))
                Spacer(Modifier.weight(1f))
                Text(text = fontStyle.fontSize.title())
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = fontStyle.fontSize.ordinal.toFloat(),
                onValueChange = { sliderValue ->
                    val fontSize = FontSize.entries[sliderValue.toInt()]
                    onFontStyleChanged(fontStyle.copy(fontSize = fontSize))
                },
                valueRange = 0f..3f,
                steps = 2,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BottomBarSwitch(
                    title = stringResource(StringsR.string.align_to_start),
                    checked = fontStyle.alignToStart,
                    onClick = {
                        val style = fontStyle.copy(alignToStart = !fontStyle.alignToStart)
                        onFontStyleChanged(style)
                    }
                )
                BottomBarSwitch(
                    title = stringResource(StringsR.string.bold_text),
                    checked = fontStyle.isBold,
                    onClick = {
                        val style = fontStyle.copy(isBold = !fontStyle.isBold)
                        onFontStyleChanged(style)
                    }
                )
                BottomBarSwitch(
                    title = stringResource(StringsR.string.high_contrast_text),
                    checked = fontStyle.isHighContrast,
                    onClick = {
                        val style = fontStyle.copy(isHighContrast = !fontStyle.isHighContrast)
                        onFontStyleChanged(style)
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BottomBarSwitch(
    title: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Switch,
                onClick = onClick
            )
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = { onClick() },
            enabled = enabled
        )
    }
}

@Composable
private fun FontSize.title() = when (this) {
    FontSize.Small -> stringResource(StringsR.string.font_size_small)
    FontSize.Normal -> stringResource(StringsR.string.font_size_normal)
    FontSize.Large -> stringResource(StringsR.string.font_size_large)
    FontSize.Huge -> stringResource(StringsR.string.font_size_huge)
}
