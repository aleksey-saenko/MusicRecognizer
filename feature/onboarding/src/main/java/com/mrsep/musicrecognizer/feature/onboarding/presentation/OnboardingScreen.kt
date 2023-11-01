package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.feature.onboarding.presentation.common.PageWithIndicator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private enum class OnboardingPage {
    WELCOME,
    PERMISSIONS,
    TOKEN,
    FINAL
}

private val TOTAL_PAGES = OnboardingPage.entries.size
private const val AVAILABLE_PAGES_ON_START = 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit,
    onOnboardingClose: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var availablePages by remember { mutableIntStateOf(AVAILABLE_PAGES_ON_START) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        availablePages
    }

    fun openNextPage(pageIndex: Int) {
        val minAvailablePages = pageIndex + 1
        if (availablePages < minAvailablePages) {
            availablePages = minAvailablePages
        }
        snapshotFlow { pagerState.canScrollForward }
            .filter { it }
            .take(1)
            .onEach { pagerState.animateScrollToPage(pageIndex) }
            .launchIn(scope)
    }

    BackHandler {
        scope.launch {
            if (pagerState.canScrollBackward) {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            } else {
                onOnboardingClose()
            }
        }
    }
    HorizontalPager(
        beyondBoundsPageCount = 0,
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) { pageIndex ->
        when (pageIndex) {
            OnboardingPage.WELCOME.ordinal -> {
                PageWithIndicator(
                    PageContent = {
                        WelcomePage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = pageIndex,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                )
            }

            OnboardingPage.PERMISSIONS.ordinal -> {
                PageWithIndicator(
                    PageContent = {
                        PermissionsPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            onPermissionsGranted = {
                                openNextPage(OnboardingPage.TOKEN.ordinal)
                            }
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = pageIndex,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                )
            }

            OnboardingPage.TOKEN.ordinal -> {
                val pageUiState by viewModel.uiState.collectAsStateWithLifecycle()
                PageWithIndicator(
                    PageContent = {
                        TokenPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            uiState = pageUiState,
                            onTokenChanged = viewModel::setTokenField,
                            onTokenSkip = viewModel::skipTokenApplying,
                            onTokenValidate = viewModel::applyTokenIfValid,
                            onTokenApplied = {
                                openNextPage(OnboardingPage.FINAL.ordinal)
                            }
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = pageIndex,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                )
            }

            OnboardingPage.FINAL.ordinal -> {
                PageWithIndicator(
                    PageContent = {
                        FinalPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            onOnboardingCompleted = {
                                viewModel.setOnboardingCompleted(true)
                                onOnboardingCompleted()
                            }
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = pageIndex,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                )
            }

            else -> {
                throw IllegalStateException("OnboardingScreen: Unavailable page index")
            }
        }
    }
}

