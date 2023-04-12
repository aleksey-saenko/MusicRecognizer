package com.mrsep.musicrecognizer.core.ui.components.workshop
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextOverflow
//import com.mrsep.musicrecognizer.presentation.common.PreviewDeviceNight
//
//@PreviewDeviceNight
//@Composable
//fun ColorsListScreen(modifier: Modifier = Modifier) {
//    val colorList = listOf(
//        "primary" to MaterialTheme.colorScheme.primary,
//        "onPrimary" to MaterialTheme.colorScheme.onPrimary,
//        "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
//        "onPrimaryContainer" to MaterialTheme.colorScheme.onPrimaryContainer,
//        "inversePrimary" to MaterialTheme.colorScheme.inversePrimary,
//        "secondary" to MaterialTheme.colorScheme.secondary,
//        "onSecondary" to MaterialTheme.colorScheme.onSecondary,
//        "secondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
//        "onSecondaryContainer" to MaterialTheme.colorScheme.onSecondaryContainer,
//        "tertiary" to MaterialTheme.colorScheme.tertiary,
//        "onTertiary" to MaterialTheme.colorScheme.onTertiary,
//        "tertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
//        "onTertiaryContainer" to MaterialTheme.colorScheme.onTertiaryContainer,
//        "background" to MaterialTheme.colorScheme.background,
//        "onBackground" to MaterialTheme.colorScheme.onBackground,
//        "surface" to MaterialTheme.colorScheme.surface,
//        "onSurface" to MaterialTheme.colorScheme.onSurface,
//        "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
//        "onSurfaceVariant" to MaterialTheme.colorScheme.onSurfaceVariant,
//        "surfaceTint" to MaterialTheme.colorScheme.surfaceTint,
//        "inverseSurface" to MaterialTheme.colorScheme.inverseSurface,
//        "inverseOnSurface" to MaterialTheme.colorScheme.inverseOnSurface,
//        "error" to MaterialTheme.colorScheme.error,
//        "onError" to MaterialTheme.colorScheme.onError,
//        "errorContainer" to MaterialTheme.colorScheme.errorContainer,
//        "onErrorContainer" to MaterialTheme.colorScheme.onErrorContainer,
//        "outline" to MaterialTheme.colorScheme.outline,
//        "outlineVariant" to MaterialTheme.colorScheme.outlineVariant,
//        "scrim" to MaterialTheme.colorScheme.scrim
//    )
//    LazyVerticalGrid(
//        modifier = modifier,
//        contentPadding = PaddingValues(8.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp),
//        columns = GridCells.Adaptive(minSize = 160.dp)
//    ) {
//        items(colorList) { (title, color) ->
//            ColorsItem(
//                title = title,
//                color = color
//            )
//        }
//    }
//}
//
//@Composable
//fun ColorsItem(
//    modifier: Modifier = Modifier,
//    title: String = "Magenta",
//    color: Color = Color.Magenta
//) {
//    Surface(
//        shadowElevation = 16.dp,
//        shape = MaterialTheme.shapes.small,
//        modifier = modifier
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Start
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .background(color)
//            )
//            Text(
//                text = title,
//                overflow = TextOverflow.Ellipsis,
//                maxLines = 1,
//                modifier = Modifier.padding(8.dp)
//            )
//        }
//    }
//}
//
//@Preview
//@Composable
//fun Prev() {
//    ColorsItem()
//}
//
