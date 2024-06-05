package com.mrsep.musicrecognizer.feature.onboarding.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun PagerControls(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int,
    showNextButton: State<Boolean>,
    showPreviousButton: State<Boolean>,
    onNextPageClick: () -> Unit,
    onPreviousPageClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedContent(
            targetState = showPreviousButton.value,
            label = "PreviousPageButton"
        ) { visible ->
            if (visible) {
                IconButton(
                    onClick = onPreviousPageClick,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_arrow_back_24),
                        contentDescription = stringResource(StringsR.string.back)
                    )
                }
            } else {
                Spacer(Modifier.size(64.dp))
            }
        }
        PageIndicator(
            totalPages = pageCount,
            currentPage = currentPage,
            modifier = Modifier.padding(16.dp)
        )
        AnimatedContent(
            targetState = showNextButton.value,
            label = "NextPageButton"
        ) { visible ->
            if (visible) {
                IconButton(
                    onClick = onNextPageClick,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Icon(
                        painter = painterResource(UiR.drawable.outline_arrow_forward_24),
                        contentDescription = stringResource(StringsR.string.forward)
                    )
                }
            } else {
                Spacer(Modifier.size(64.dp))
            }
        }
    }
}
