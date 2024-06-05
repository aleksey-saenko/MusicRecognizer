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
import com.mrsep.musicrecognizer.feature.onboarding.presentation.common.PagerControls
import kotlinx.coroutines.launch

private enum class OnboardingPage {
    Welcome,
    Permissions,
    Token,
    Final
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OnboardingScreen(
    onOnboardingClose: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { OnboardingPage.entries.size }
    )

    BackHandler {
        if (pagerState.canScrollBackward) {
            scope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
        } else {
            onOnboardingClose()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .systemBarsPadding()
            .imePadding()
    ) {
        HorizontalPager(
            beyondBoundsPageCount = 0,
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            when (pageIndex) {
                OnboardingPage.Welcome.ordinal -> WelcomePage(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )

                OnboardingPage.Permissions.ordinal -> PermissionsPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    onPermissionsGranted = {
                        scope.launch {
                            pagerState.animateScrollToPage(OnboardingPage.Token.ordinal)
                        }
                    }
                )

                OnboardingPage.Token.ordinal -> {
                    val pageUiState by viewModel.uiState.collectAsStateWithLifecycle()
                    TokenPage(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        uiState = pageUiState,
                        onTokenChanged = viewModel::setTokenField,
                        onTokenValidate = viewModel::applyTokenIfValid,
                        onTokenApplied = {
                            scope.launch {
                                pagerState.animateScrollToPage(OnboardingPage.Final.ordinal)
                            }
                        }
                    )
                }

                OnboardingPage.Final.ordinal -> FinalPage(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    onOnboardingCompleted = {
                        viewModel.setOnboardingCompleted(true)
                    }
                )

                else -> throw IllegalStateException("OnboardingScreen: Unavailable page index")
            }
        }
        PagerControls(
            modifier = Modifier.fillMaxWidth(),
            currentPage = pagerState.currentPage,
            pageCount = pagerState.pageCount,
            showNextButton = remember {
                derivedStateOf { pagerState.currentPage < pagerState.pageCount - 1 }
            },
            showPreviousButton = remember {
                derivedStateOf { pagerState.currentPage > 0 }
            },
            onNextPageClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            onPreviousPageClick = {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        )
    }
}
