package com.mrsep.musicrecognizer.presentation.screens.library

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
    onTrackClick: (mbId: String) -> Unit
) {
    val resentsList by viewModel.recentTracksFlow.collectAsStateWithLifecycle()
    val favoritesList by viewModel.favoriteTracksFlow.collectAsStateWithLifecycle()
//    val foundList by viewModel.foundTracksFlow.collectAsStateWithLifecycle()
    val searchResult by viewModel.trackSearchResultFlow.collectAsStateWithLifecycle()
    val topBarBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var searchActive by rememberSaveable { mutableStateOf(false) }
    val animationDuration = 300

    val isLibraryEmpty = resentsList.isEmpty() && favoritesList.isEmpty()

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AnimatedContent(
            targetState = searchActive,
            contentAlignment = Alignment.Center,
            transitionSpec = {
                fadeIn(animationSpec = tween(animationDuration)) with
                        fadeOut(animationSpec = tween(animationDuration))
            },
        ) { isSearchActive ->
            if (isSearchActive) {
                TrackSearchBar(
                    onSearch = { query -> viewModel.submitSearchKeyword(query) },
                    onSearchClose = { searchActive = false },
                    searchResult = searchResult,
                    onTrackClick = onTrackClick
                )
            } else {
                Column {
                    LibraryTopBar(
                        topAppBarScrollBehavior = topBarBehaviour,
                        onSearchIconClick = { searchActive = true },
                        modifier = Modifier.animateEnterExit(
                            enter = slideInVertically(
                                animationSpec = tween(animationDuration),
                                initialOffsetY = { -it / 2 }
                            ),
                            exit = slideOutVertically(
                                animationSpec = tween(animationDuration),
                                targetOffsetY = { -it / 2 }
                            )
                        )
                    )
                    Column(
                        modifier = Modifier.animateEnterExit(
                            enter = slideInVertically(
                                animationSpec = tween(animationDuration),
                                initialOffsetY = { it / 2 }
                            ),
                            exit = slideOutVertically(
                                animationSpec = tween(animationDuration),
                                targetOffsetY = { it / 2 }
                            )
                        ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isLibraryEmpty) {
                            EmptyLibraryMessage(modifier = Modifier.weight(1f))
                        } else {
                            if (resentsList.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.recently),
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .align(Alignment.Start)
                                )
                                TrackLazyGrid(
                                    trackList = resentsList,
                                    onTrackClick = onTrackClick,
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .nestedScroll(topBarBehaviour.nestedScrollConnection)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun EmptyLibraryMessage(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.empty_library_message),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Icon(
            painter = painterResource(R.drawable.baseline_recently_24),
            contentDescription = null,
            modifier = Modifier
                .padding(24.dp)
                .size(80.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }

}