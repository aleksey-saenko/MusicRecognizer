package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onDismissRequest: () -> Unit,
    filterState: TrackFilterState,
    onApplyClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Text(
            text = stringResource(StringsR.string.filters),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .weight(1f, false)
        ) {
            FilterGroup(title = stringResource(StringsR.string.filter_favorites)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = filterState.favoritesMode == FavoritesMode.All,
                        onClick = { filterState.favoritesMode = FavoritesMode.All },
                        label = { Text(text = stringResource(StringsR.string.filter_all)) }
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
            Spacer(Modifier.height(8.dp))
            FilterGroup(title = stringResource(StringsR.string.filter_sort_by)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            Spacer(Modifier.height(8.dp))
            FilterGroup(title = stringResource(StringsR.string.filter_order_By)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
        }
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            TextButton(onClick = filterState::resetFilter) {
                Text(
                    text = stringResource(StringsR.string.reset),
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

@Composable
private fun FilterGroup(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        content()
        Spacer(Modifier.height(4.dp))
    }
}
