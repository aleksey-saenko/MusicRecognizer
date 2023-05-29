package com.mrsep.musicrecognizer.feature.recognitionqueue.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.ui.components.ScreenScrollableTopBar
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun QueueScreenTopBar(
    modifier: Modifier = Modifier,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onBackPressed: () -> Unit,
    multiselectEnabled: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onCancelSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onDeleteAll: () -> Unit,
    onDisableSelectionMode: () -> Unit
) {
    ScreenScrollableTopBar(
        modifier = modifier,
        title = {
            AnimatedContent(
                targetState = multiselectEnabled,
                transitionSpec = {
                    fadeIn() + scaleIn(initialScale = 0.8f) with
                            fadeOut() + scaleOut(targetScale = 0.8f)
                },
                contentAlignment = Alignment.CenterStart
            ) { multiselectMode ->
                if (multiselectMode) {
                    Text(
                        text = "$selectedCount / $totalCount",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall
                    )
                } else {
                    Text(
                        text = stringResource(StringsR.string.queue).toUpperCase(Locale.current),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        },
        navigationIcon = {
            AnimatedContent(
                targetState = multiselectEnabled,
                transitionSpec = {
                    fadeIn() + scaleIn(initialScale = 0.6f) with
                            fadeOut() + scaleOut(targetScale = 0.6f)
                },
                contentAlignment = Alignment.CenterStart
            ) { multiselectMode ->
                if (multiselectMode) {
                    IconButton(onClick = onDisableSelectionMode) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(StringsR.string.close_multi_selection_mode)
                        )
                    }
                } else {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(StringsR.string.back)
                        )
                    }
                }
            }

        },
        actions = {
            val allSelected = (selectedCount == totalCount)
            Row {
                AnimatedVisibility(
                    visible = multiselectEnabled,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut(targetScale = 0.9f),
                ) {
                    Row {
                        if (allSelected) {
                            IconButton(onClick = onDeselectAll) {
                                Icon(
                                    painter = painterResource(UiR.drawable.baseline_deselect_24),
                                    contentDescription = stringResource(StringsR.string.deselect_all)
                                )
                            }
                        } else {
                            IconButton(onClick = onSelectAll) {
                                Icon(
                                    painter = painterResource(UiR.drawable.baseline_select_all_24),
                                    contentDescription = stringResource(StringsR.string.select_all)
                                )
                            }
                        }
                        IconButton(onClick = onCancelSelected) {
                            Icon(
                                painter = painterResource(UiR.drawable.baseline_cancel_schedule_send_24),
                                contentDescription = stringResource(StringsR.string.cancel_selected)
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = totalCount > 0,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut(targetScale = 0.9f)
                ) {
                    IconButton(
                        onClick = {
                            if (allSelected || !multiselectEnabled) onDeleteAll() else onDeleteSelected()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(StringsR.string.delete_selected)
                        )
                    }
                }
            }
        },
        topAppBarScrollBehavior = topAppBarScrollBehavior
    )
}