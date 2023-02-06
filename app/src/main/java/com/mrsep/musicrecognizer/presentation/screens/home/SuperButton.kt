package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme

@Composable
fun SuperButton(
    activated: Boolean,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint by animateColorAsState(
        targetValue = if (activated) Color.Magenta else MaterialTheme.colorScheme.onPrimary,
        animationSpec = tween(durationMillis = 50)
//                animationSpec = repeatable(3, TweenSpec())
    )
    Button(
        onClick = { onButtonClick() },
        modifier = modifier
            .size(150.dp)
            .background(
                brush = Brush.radialGradient(
                    0.75f to MaterialTheme.colorScheme.primary,
                    1.0f to MaterialTheme.colorScheme.background,
                ),
                shape = CircleShape
            ),
        enabled = true, //!activated
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Transparent,
        ),
        elevation = ButtonDefaults.buttonElevation(),
        border = BorderStroke(width = 8.dp, color = MaterialTheme.colorScheme.onPrimary),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_mic_80),
            contentDescription = "Recognize button",
            tint = iconTint,
        )
    }
}

@Composable
fun SuperButtonSection(
    title: String,
    activated: Boolean,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        SuperButton(onButtonClick = onButtonClick, activated = activated)
    }

}

@PreviewDeviceNight
@Composable
fun SuperButtonSectionPreview() {
    MusicRecognizerTheme {
        Surface {
            SuperButtonSection(
                title = "Tap to recognize",
                activated = false,
                onButtonClick = { /*TODO*/ }
            )
        }

    }
}
