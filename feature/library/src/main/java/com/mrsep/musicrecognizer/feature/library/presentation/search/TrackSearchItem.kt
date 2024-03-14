package com.mrsep.musicrecognizer.feature.library.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun TrackSearchItem(
    track: TrackUi,
    keyword: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    showRecognitionDate: Boolean = true,
    contentPadding: PaddingValues
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(contentPadding)
    ) {
        val placeholder = forwardingPainter(
            painter = painterResource(UiR.drawable.outline_album_fill1_24),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            alpha = 0.3f
        )
        AsyncImage(
            model = track.artworkUrl,
            fallback = placeholder,
            error = placeholder,
            contentDescription = stringResource(StringsR.string.artwork),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .shadow(
                    elevation = 1.dp,
                    shape = shape
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = shape
                )
                .clip(shape)
                .heightIn(max = 100.dp)
                .aspectRatio(1f, true)
        )
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = highlightKeyword(track.title, keyword),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = highlightKeyword(track.artist, keyword),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = track.album?.run { highlightKeyword(this, keyword) }
                    ?: AnnotatedString(" "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showRecognitionDate) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = track.recognitionDate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .alpha(0.9f)
                        .align(Alignment.End)
                )
            }
        }
    }
}

@Composable
@Stable
private fun highlightKeyword(text: String, keyword: String): AnnotatedString {
    if (keyword.isBlank()) return AnnotatedString(text)
    val spanStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
    return buildAnnotatedString {
        var start = 0
        while (true) {
            val first = text.indexOf(keyword, start, ignoreCase = true).takeIf { it != -1 } ?: break
            val end = first + keyword.length
            append(text.substring(start, first))
            withStyle(spanStyle) {
                append(text.substring(first, end))
            }
            start = end
        }
        append(text.substring(start, text.length))
    }
}