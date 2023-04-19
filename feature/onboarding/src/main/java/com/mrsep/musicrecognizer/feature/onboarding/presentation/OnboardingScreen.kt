package com.mrsep.musicrecognizer.feature.onboarding.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mrsep.musicrecognizer.feature.onboarding.presentation.common.PageWithIndicator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private enum class OnboardingPage(val index: Int) {
    WELCOME(0),
    PERMISSIONS(1),
    TOKEN(2),
    FINAL(3)
}

private val TOTAL_PAGES = OnboardingPage.values().size

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit,
    onOnboardingClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var availablePages by remember { mutableStateOf(2) }
    val pagerState = rememberPagerState()

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
        pageCount = availablePages,
        beyondBoundsPageCount = 0,
        state = pagerState,
//        modifier = Modifier.statusBarsPadding()
    ) { page ->
        Log.d("OnboardingScreen-Pager", "Page index: $page, AvailablePages=$availablePages")
        when (page) {
            OnboardingPage.WELCOME.index -> {
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
                    currentPage = page,
                    modifier = Modifier.fillMaxSize()
                )
            }
            OnboardingPage.PERMISSIONS.index -> {
                PageWithIndicator(
                    PageContent = {
                        PermissionsPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            onPermissionsGranted = {
                                val minimumRequiredPages = OnboardingPage.TOKEN.index + 1
                                if (availablePages < minimumRequiredPages) {
                                    availablePages = minimumRequiredPages
                                }
                                scope.launch {
                                    snapshotFlow { pagerState.canScrollForward }
                                        .filter { it }
                                        .take(1)
                                        .collect { pagerState.animateScrollToPage(OnboardingPage.TOKEN.index) }
                                }
                            }
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = page,
                    modifier = Modifier.fillMaxSize()
                )
            }
            OnboardingPage.TOKEN.index -> {
                PageWithIndicator(
                    PageContent = {
                        TokenPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            onTokenApplied = {
                                val minimumRequiredPages = OnboardingPage.FINAL.index + 1
                                if (availablePages < minimumRequiredPages) {
                                    availablePages = minimumRequiredPages
                                }
                                scope.launch {
                                    snapshotFlow { pagerState.canScrollForward }
                                        .filter { it }
                                        .take(1)
                                        .collect { pagerState.animateScrollToPage(OnboardingPage.FINAL.index) }
                                }
                            }
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = page,
                    modifier = Modifier.fillMaxSize()
                )
            }
            OnboardingPage.FINAL.index -> {
                PageWithIndicator(
                    PageContent = {
                        FinalPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            onOnboardingCompletedClick = {
                                onOnboardingCompleted()
                            }
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = page,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                throw IllegalStateException("OnboardingScreen: Unavailable page index")
            }
        }

    }
}

