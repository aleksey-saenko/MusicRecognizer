package com.mrsep.musicrecognizer.core.ui.components.workshop
//
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.RowScope
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RowScope.TrackBackgroundForDismissible(
//    dismissState: DismissState,
//    modifier: Modifier = Modifier
//) {
//    val direction = dismissState.dismissDirection ?: return
//    val actionColor by animateColorAsState(
//        targetValue = when (dismissState.targetValue) {
//            DismissValue.Default -> Color.Transparent
//            DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
//            DismissValue.DismissedToStart -> Color.Red.copy(alpha = 0.3f)
//        }
//    )
//    val backgroundColors = when (direction) {
//        DismissDirection.StartToEnd -> listOf(
//            Color.Transparent,
//            actionColor
//        )
//        DismissDirection.EndToStart -> listOf(
//            actionColor,
//            Color.Transparent
//        )
//    }
//    val alignment = when (direction) {
//        DismissDirection.StartToEnd -> Alignment.CenterStart
//        DismissDirection.EndToStart -> Alignment.CenterEnd
//    }
//    val icon = when (direction) {
//        DismissDirection.StartToEnd -> Icons.Default.Favorite
//        DismissDirection.EndToStart -> Icons.Default.Close
//    }
//    val scale by animateFloatAsState(
//        if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
//    )
//
//    Box(
//        modifier
//            .fillMaxSize()
//            .padding(vertical = 16.dp)
//            .background(brush = Brush.horizontalGradient(colors = backgroundColors))
//            .padding(horizontal = 24.dp),
//        contentAlignment = alignment
//    ) {
//        Icon(
//            icon,
//            contentDescription = null,
//            modifier = Modifier.scale(scale)
//        )
//    }
//}