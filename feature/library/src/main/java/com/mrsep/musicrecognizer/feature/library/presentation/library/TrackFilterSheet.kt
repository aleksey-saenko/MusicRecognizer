package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.library.domain.model.FavoritesMode
import com.mrsep.musicrecognizer.feature.library.domain.model.OrderBy
import com.mrsep.musicrecognizer.feature.library.domain.model.SortBy
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TrackFilterBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    filterState: TrackFilterState,
    onDismissRequest: () -> Unit,
    onApplyClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(StringsR.string.filters),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            FilterGroup(
                modifier = Modifier.padding(top = 16.dp),
                title = stringResource(StringsR.string.filter_favorite_status)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = filterState.favoritesMode == FavoritesMode.All,
                        onClick = { filterState.favoritesMode = FavoritesMode.All },
                        label = { Text(text = stringResource(StringsR.string.all)) }
                    )
                    FilterChip(
                        selected = filterState.favoritesMode == FavoritesMode.OnlyFavorites,
                        onClick = { filterState.favoritesMode = FavoritesMode.OnlyFavorites },
                        label = { Text(text = stringResource(StringsR.string.filter_only_favorites)) }
                    )
                    FilterChip(
                        selected = filterState.favoritesMode == FavoritesMode.ExcludeFavorites,
                        onClick = { filterState.favoritesMode = FavoritesMode.ExcludeFavorites },
                        label = { Text(text = stringResource(StringsR.string.filter_exclude_favorites)) }
                    )
                }
            }
            FilterGroup(
                modifier = Modifier.padding(top = 16.dp),
                title = stringResource(StringsR.string.filter_sort_by)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = filterState.sortBy == SortBy.RecognitionDate,
                        onClick = { filterState.sortBy = SortBy.RecognitionDate },
                        label = { Text(text = stringResource(StringsR.string.filter_recognition_date)) }
                    )
                    FilterChip(
                        selected = filterState.sortBy == SortBy.Title,
                        onClick = { filterState.sortBy = SortBy.Title },
                        label = { Text(text = stringResource(StringsR.string.filter_title)) }
                    )
                    FilterChip(
                        selected = filterState.sortBy == SortBy.Artist,
                        onClick = { filterState.sortBy = SortBy.Artist },
                        label = { Text(text = stringResource(StringsR.string.filter_artist)) }
                    )
                    FilterChip(
                        selected = filterState.sortBy == SortBy.ReleaseDate,
                        onClick = { filterState.sortBy = SortBy.ReleaseDate },
                        label = { Text(text = stringResource(StringsR.string.filter_release_date)) }
                    )
                }
            }
            FilterGroup(
                modifier = Modifier.padding(top = 16.dp),
                title = stringResource(StringsR.string.filter_order_By)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = filterState.orderBy == OrderBy.Desc,
                        onClick = { filterState.orderBy = OrderBy.Desc },
                        label = { Text(text = stringResource(StringsR.string.filter_descending)) }
                    )
                    FilterChip(
                        selected = filterState.orderBy == OrderBy.Asc,
                        onClick = { filterState.orderBy = OrderBy.Asc },
                        label = { Text(text = stringResource(StringsR.string.filter_ascending)) }
                    )
                }
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                TextButton(onClick = filterState::resetFilter) {
                    Text(
                        text = stringResource(StringsR.string.reset_all),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                TextButton(onClick = onApplyClick) {
                    Text(
                        text = stringResource(StringsR.string.apply),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FilterGroup(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = title,
            modifier = Modifier.padding(16.dp)
        )
        content()
    }
}