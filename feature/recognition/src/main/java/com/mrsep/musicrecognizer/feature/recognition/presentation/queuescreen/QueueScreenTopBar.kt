package com.mrsep.musicrecognizer.feature.recognition.presentation.queuescreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun QueueScreenTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    onBackPressed: () -> Unit,
    multiselectEnabled: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onCancelSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onDisableSelectionMode: () -> Unit
) {
    // since the toolbar has collapsing behavior, we have to disable the icons to avoid false positives
    val isExpanded by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction < 0.6f }
    }
    val topBarAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        label = ""
    )
    val transition = updateTransition(targetState = multiselectEnabled, label = "topBarMode")
    TopAppBar(
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
                    IconButton(onClick = onDisableSelectionMode, enabled = isExpanded) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(StringsR.string.close_multi_selection_mode)
                        )
                    }
                } else {
                    IconButton(onClick = onBackPressed, enabled = isExpanded) {
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
            transition.Crossfade { multiselectMode ->
                if (multiselectMode) {
                    Row {
                        if (allSelected) {
                            IconButton(onClick = onDeselectAll, enabled = isExpanded) {
                                Icon(
                                    painter = painterResource(UiR.drawable.baseline_deselect_24),
                                    contentDescription = stringResource(StringsR.string.deselect_all)
                                )
                            }
                        } else {
                            IconButton(onClick = onSelectAll, enabled = isExpanded) {
                                Icon(
                                    painter = painterResource(UiR.drawable.baseline_select_all_24),
                                    contentDescription = stringResource(StringsR.string.select_all)
                                )
                            }
                        }
                        IconButton(onClick = onCancelSelected, enabled = isExpanded) {
                            Icon(
                                painter = painterResource(UiR.drawable.baseline_cancel_schedule_send_24),
                                contentDescription = stringResource(StringsR.string.cancel_selected)
                            )
                        }
                        IconButton(onClick = onDeleteSelected, enabled = isExpanded) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(StringsR.string.delete_selected)
                            )
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Unspecified,
            scrolledContainerColor = Color.Unspecified,
        ),
        modifier = modifier.alpha(topBarAlpha)
    )
}