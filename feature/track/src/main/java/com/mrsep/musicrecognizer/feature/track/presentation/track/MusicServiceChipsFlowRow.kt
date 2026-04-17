package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating
import com.mrsep.musicrecognizer.core.ui.modifiers.animatePlacement
import com.mrsep.musicrecognizer.core.ui.resources.iconId
import com.mrsep.musicrecognizer.core.ui.resources.titleId
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun MusicServiceChipsFlowRow(
    isLoading: Boolean,
    trackLinks: ImmutableList<TrackLink>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .animatePlacement()
                .align(Alignment.CenterVertically)
        ) {
            key(Unit) {
                VinylRotating(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trackLinks.forEach { link ->
            key(link.service) {
                MusicServiceChip(
                    titleRes = link.service.titleId(),
                    iconRes = link.service.iconId(),
                    link = link.url,
                    modifier = Modifier.animatePlacement()
                )
            }
        }
    }
}

@Composable
internal fun MusicServiceChip(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = 1.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .sizeIn(minHeight = 32.dp)
                .combinedClickable(
                    interactionSource = null,
                    indication = LocalIndication.current,
                    onClick = { context.openUrlImplicitly(link) },
                    onLongClick = { context.copyTextToClipboard(link) },
                    role = Role.Button
                )
                .padding(horizontal = 8.dp)

        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(SuggestionChipDefaults.IconSize)
            )
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
