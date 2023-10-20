package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.ui.R
import com.mrsep.musicrecognizer.core.strings.R as StringsR

private enum class TopBarMode { EmptyLibrary, Default, MultiSelection }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreenTopBar(
    isLibraryEmpty: Boolean,
    isFilterApplied: Boolean,
    isMultiselectEnabled: Boolean,
    selectedCount: Int,
    totalCount: Int,
    onSearchIconClick: () -> Unit,
    onFilterIconClick: () -> Unit,
    onDeleteIconClick: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    // since the toolbar has collapsing behavior, we have to disable the icons to avoid false positives
    val isExpanded by remember {
        derivedStateOf { topAppBarScrollBehavior.state.collapsedFraction < 0.6f }
    }
    val topBarMode = if (!isLibraryEmpty) {
        if (isMultiselectEnabled) TopBarMode.MultiSelection else TopBarMode.Default
    } else {
        TopBarMode.EmptyLibrary
    }
    val transition = updateTransition(targetState = topBarMode, label = "topBarMode")
    val topBarAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        label = ""
    )

    TopAppBar(
        modifier = modifier.alpha(topBarAlpha),
        title = {
            Crossfade(
                targetState = (selectedCount != 0),
                label = "Title"
            ) { selectionTitle ->
                Text(
                    text = if (selectionTitle) {
                        if (selectedCount == 0) "" else "$selectedCount / $totalCount"
                    } else {
                        stringResource(StringsR.string.library).toUpperCase(Locale.current)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            transition.AnimatedContent(
                contentAlignment = Alignment.CenterStart
            ) { mode ->
                when (mode) {
                    TopBarMode.EmptyLibrary,
                    TopBarMode.Default -> {}

                    TopBarMode.MultiSelection -> IconButton(
                        onClick = onDeselectAll,
                        enabled = isExpanded
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(StringsR.string.disable_multi_selection_mode)
                        )
                    }
                }
            }
        },
        actions = {
            transition.AnimatedContent(
                contentAlignment = Alignment.CenterEnd
            ) { mode ->
                when (mode) {
                    TopBarMode.EmptyLibrary -> {}

                    TopBarMode.Default -> Row(horizontalArrangement = Arrangement.End) {
                        IconButton(
                            onClick = onSearchIconClick,
                            enabled = isExpanded
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(StringsR.string.search_track)
                            )
                        }
                        IconButton(
                            onClick = onFilterIconClick,
                            enabled = isExpanded
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_filter_24),
                                tint = animateColorAsState(
                                    if (isFilterApplied)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        LocalContentColor.current,
                                    label = "FilterIconColor"
                                ).value,
                                contentDescription = stringResource(StringsR.string.filters)
                            )
                        }
                    }

                    TopBarMode.MultiSelection -> Row(
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onSelectAll,
                            enabled = isExpanded
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_select_all_24),
                                contentDescription = stringResource(StringsR.string.select_all)
                            )
                        }
                        IconButton(
                            onClick = onDeleteIconClick,
                            enabled = isExpanded
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(StringsR.string.delete_selected)
                            )
                        }
                    }
                }
            }
        },
        scrollBehavior = topAppBarScrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Unspecified,
            scrolledContainerColor = Color.Unspecified,
        )
    )

}