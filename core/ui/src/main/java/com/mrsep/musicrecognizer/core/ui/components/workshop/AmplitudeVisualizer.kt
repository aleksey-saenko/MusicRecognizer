package com.mrsep.musicrecognizer.core.ui.components.workshop
//
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.EaseInOutQuart
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//
//@Composable
//fun AmplitudeVisualizer(
//    modifier: Modifier = Modifier,
//    currentValue: Float,
//) {
//    val color by animateColorAsState(
//        targetValue = when (currentValue) {
//            in 0f..0.4f -> Color(0xFF50C878)
//            in 0.4f..0.6f -> Color.Yellow
//            in 0.6f..0.8f -> Color(0xFFFFA500)
//            else -> Color.Red
//        },
//        animationSpec = tween(easing = EaseInOutQuart, durationMillis = 500)
//    )
//    val smoothedValue by animateFloatAsState(
//        targetValue = currentValue,
//        animationSpec = tween(easing = EaseInOutQuart, durationMillis = 300)
//    )
//    Box(
//        modifier = modifier
//            .fillMaxWidth(smoothedValue)
//            .background(color = color),
//        content = {}
//    )
//}