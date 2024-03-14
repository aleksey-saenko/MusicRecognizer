package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR

internal enum class SearchProvider { WebDefault, Wikipedia }
internal enum class SearchTarget { Track, Artist, Album }

internal data class SearchParams(
    val provider: SearchProvider,
    val target: SearchTarget
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun WebSearchBottomSheet(
    sheetState: SheetState,
    onPerformWebSearchClick: (SearchParams) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
    ) {
        var providerSelected by rememberSaveable { mutableStateOf(SearchProvider.WebDefault) }
        var targetSelected by rememberSaveable { mutableStateOf(SearchTarget.Track) }

        Text(
            text = stringResource(StringsR.string.web_search),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            SheetGroup(title = stringResource(StringsR.string.search_in)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = providerSelected == SearchProvider.WebDefault,
                        onClick = { providerSelected = SearchProvider.WebDefault },
                        label = { Text(text = stringResource(StringsR.string.web)) }
                    )
                    FilterChip(
                        selected = providerSelected == SearchProvider.Wikipedia,
                        onClick = { providerSelected = SearchProvider.Wikipedia },
                        label = { Text(text = stringResource(StringsR.string.wikipedia)) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            SheetGroup(title = stringResource(StringsR.string.search_for)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = targetSelected == SearchTarget.Track,
                        onClick = { targetSelected = SearchTarget.Track },
                        label = { Text(text = stringResource(StringsR.string.track)) }
                    )
                    FilterChip(
                        selected = targetSelected == SearchTarget.Artist,
                        onClick = { targetSelected = SearchTarget.Artist },
                        label = { Text(text = stringResource(StringsR.string.artist)) }
                    )
                    FilterChip(
                        selected = targetSelected == SearchTarget.Album,
                        onClick = { targetSelected = SearchTarget.Album },
                        label = { Text(text = stringResource(StringsR.string.album)) }
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
                .padding(horizontal = 8.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    onPerformWebSearchClick(SearchParams(providerSelected, targetSelected))
                }
            ) {
                Text(
                    text = stringResource(StringsR.string.search),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SheetGroup(
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