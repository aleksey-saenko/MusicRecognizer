package com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun AcrCloudHelpDialog(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        modifier = modifier,
        title = {
            Text(text = stringResource(StringsR.string.acr_cloud))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = AnnotatedString.fromHtml(
                        htmlString = stringResource(
                            StringsR.string.acr_cloud_help_dialog_message,
                            stringResource(StringsR.string.acrcloud_project_guide_url)
                        ),
                        linkStyles = TextLinkStyles(
                            style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                            hoveredStyle = SpanStyle(textDecoration = TextDecoration.Underline),
                        )
                    ),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(StringsR.string.acr_cloud_help_dialog_steps_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_dialog_step1))
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_dialog_step2))
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_dialog_step3))
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_dialog_step4))
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}
