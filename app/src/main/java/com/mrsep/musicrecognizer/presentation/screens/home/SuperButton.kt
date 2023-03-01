package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.ui.theme.SeaBlue

@Composable
fun SuperButton(
    activated: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        RippleAnimation(
            activated = activated,
            modifier = Modifier.size(220.dp),
            baseColor = MaterialTheme.colorScheme.primary,
            activatedColor = SeaBlue,
            circlesCount = 9,
            animationSpeed = 12_000
        )
        AnimatedEnhancedButton(
            modifier = Modifier.size(150.dp),
            activated = activated,
            enabled = enabled,
            onClick = onClick,
            onLongPress = onLongPress
        ) {
            AnimatedWaveIcon(
                activated = activated,
                baseColor = MaterialTheme.colorScheme.primary,
                activatedColor = SeaBlue
            )
        }
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
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        SuperButton(
            onClick = onButtonClick,
            onLongPress = onButtonClick,
            activated = activated,
            enabled = true
        )
    }
}