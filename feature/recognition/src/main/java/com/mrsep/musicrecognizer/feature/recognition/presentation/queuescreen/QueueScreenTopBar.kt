package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun QueueScreenTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    multiselectEnabled: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onCancelSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onDisableSelectionMode: () -> Unit
) {
    val transition = updateTransition(targetState = multiselectEnabled, label = "topBarMode")
    TopAppBar(
        modifier = modifier,
        title = {
            transition.Crossfade { multiselectMode ->
                if (multiselectMode) {
                    Text(
                        text = if (selectedCount == 0) "" else "$selectedCount / $totalCount",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = stringResource(StringsR.string.queue).toUpperCase(Locale.current),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            transition.AnimatedContent(
                contentAlignment = Alignment.CenterStart
            ) { multiselectMode ->
                if (multiselectMode) {
                    IconButton(onClick = onDisableSelectionMode) {
                        Icon(
                            painter = painterResource(UiR.drawable.outline_close_24),
                            contentDescription = stringResource(StringsR.string.disable_multi_selection_mode)
                        )
                    }
                }
            }
        },
        actions = {
            val allSelected = (selectedCount == totalCount)
            transition.Crossfade { multiselectMode ->
                if (multiselectMode) {
                    Row {
                        if (allSelected) {
                            IconButton(onClick = onDeselectAll) {
                                Icon(
                                    painter = painterResource(UiR.drawable.outline_deselect_24),
                                    contentDescription = stringResource(StringsR.string.deselect_all)
                                )
                            }
                        } else {
                            IconButton(onClick = onSelectAll) {
                                Icon(
                                    painter = painterResource(UiR.drawable.outline_select_all_24),
                                    contentDescription = stringResource(StringsR.string.select_all)
                                )
                            }
                        }
                        IconButton(onClick = onCancelSelected) {
                            Icon(
                                painter = painterResource(UiR.drawable.outline_cancel_schedule_send_24),
                                contentDescription = stringResource(StringsR.string.cancel_selected)
                            )
                        }
                        IconButton(onClick = onDeleteSelected) {
                            Icon(
                                painter = painterResource(UiR.drawable.outline_delete_24),
                                contentDescription = stringResource(StringsR.string.delete_selected)
                            )
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}