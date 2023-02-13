package com.mrsep.musicrecognizer.presentation.screens.onboarding

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrsep.musicrecognizer.presentation.screens.onboarding.common.PageWithIndicator

private const val TOTAL_PAGES = 4

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onSignUpClick: (String) -> Unit,
    onApplyTokenClick: () -> Unit
) {
    var availablePages by remember { mutableStateOf(2) }
    val pagerState = rememberPagerState()
    HorizontalPager(
        pageCount = availablePages,
        state = pagerState,
        userScrollEnabled = true
    ) { page ->
        Log.d("Pager", "Page: $page")
        when (page) {
            0 -> {
                PageWithIndicator(
                    PageContent = {
                        WelcomePage(
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = page,
                    modifier = modifier.fillMaxSize()
                )
            }
            1 -> {
                PageWithIndicator(
                    PageContent = {
                        TokenPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            viewModel = viewModel,
                            onSignUpClick = { link -> onSignUpClick(link) },
                            onApplyTokenClick = { onApplyTokenClick() }
//                            onApplyTokenClick = { }
                        )
                    },
                    totalPages = TOTAL_PAGES,
                    currentPage = page,
                    modifier = modifier.fillMaxSize()
                )
            }
            2 -> {
                PageWithIndicator(
                    PageContent = { SimplePage(page) },
                    totalPages = TOTAL_PAGES,
                    currentPage = page,
                    modifier = modifier.fillMaxSize()
                )
            }
            3 -> {
                PageWithIndicator(
                    PageContent = { SimplePage(page) },
                    totalPages = TOTAL_PAGES,
                    currentPage = page,
                    modifier = modifier.fillMaxSize()
                )
            }
            else -> {
                throw IllegalStateException("OnboardingScreen: unavailable page index")
            }
        }

    }
}

@Composable
fun SimplePage(
    page: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Page: $page",
        modifier = modifier
    )
}

