package com.mrsep.musicrecognizer.presentation.screens.onboarding.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PageWithIndicator(
    modifier: Modifier = Modifier,
    PageContent: @Composable ColumnScope.() -> Unit,
    totalPages: Int,
    currentPage: Int
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PageContent()
        WormPageIndicator(
            totalPages = totalPages,
            currentPage = currentPage,
            modifier = Modifier.padding(16.dp)
        )
    }
}