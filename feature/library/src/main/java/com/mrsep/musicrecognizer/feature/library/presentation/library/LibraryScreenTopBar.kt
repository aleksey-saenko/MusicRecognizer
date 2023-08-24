package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import com.mrsep.musicrecognizer.core.ui.R
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreenTopBar(
    modifier: Modifier = Modifier,
    onSearchIconClick: () -> Unit,
    onFilterIconClick: () -> Unit,
    isLibraryEmpty: Boolean,
    isFilterApplied: Boolean,
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    val isTotalExpanded by remember {
        derivedStateOf { topAppBarScrollBehavior.state.collapsedFraction < 0.7f }
    }
    Column {
        TopAppBar(
            modifier = modifier,
            title = {
                AnimatedVisibility(
                    visible = isTotalExpanded,
                    enter = slideInVertically { totalHeight -> -totalHeight } +
                            fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = slideOutVertically { totalHeight -> -totalHeight } +
                            fadeOut() + scaleOut(targetScale = 0.95f),
                    content = {
                        Text(
                            text = stringResource(StringsR.string.library).toUpperCase(Locale.current),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                )
            },
            navigationIcon = { },
            actions = {
                AnimatedVisibility(
                    visible = isTotalExpanded && !isLibraryEmpty,
                    enter = slideInVertically { totalHeight -> -totalHeight } +
                            fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = slideOutVertically { totalHeight -> -totalHeight } +
                            fadeOut() + scaleOut(targetScale = 0.95f),
                    content = {
                        Row {
                            IconButton(onClick = onSearchIconClick) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = stringResource(StringsR.string.search_track)
                                )
                            }
                            IconButton(onClick = onFilterIconClick) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_filter_24),
                                    tint = animateColorAsState(
                                        if (isFilterApplied)
                                            MaterialTheme.colorScheme.tertiary
                                        else
                                            MaterialTheme.colorScheme.onBackground,
                                        label = "FilterIconColor"
                                    ).value,
                                    contentDescription = stringResource(StringsR.string.filters)
                                )
                            }
                        }
                    }
                )
            },
            scrollBehavior = topAppBarScrollBehavior,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Unspecified,
                scrolledContainerColor = Color.Unspecified,
            )
        )
    }

}