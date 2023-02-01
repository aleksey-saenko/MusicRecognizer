package com.mrsep.musicrecognizer.presentation

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.util.forwardingPainter

data class TrackCardArgs(
    val author: String,
    val track: String,
    val album: String,
    val year: String,
    val lyrics: String?,
    val imageUri: Uri
)

@Composable
fun TrackCard(
    trackCardArgs: TrackCardArgs,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessVeryLow))
    ) {
        var lyricsExpanded by rememberSaveable { mutableStateOf(false) }
        Column {
            Row {
                val placeholder = forwardingPainter(
                    painter = painterResource(R.drawable.baseline_album_96),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    alpha = 0.3f
                )
                AsyncImage(
                    model = trackCardArgs.imageUri,
                    placeholder = placeholder,
                    error = placeholder,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .size(150.dp)
                )
                Column(modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = trackCardArgs.track,
                        modifier = Modifier,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1
                    )
                    Text(
                        text = trackCardArgs.author,
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = "${trackCardArgs.album} (${trackCardArgs.year})",
                        modifier = Modifier,
                        style = MaterialTheme.typography.titleMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (trackCardArgs.lyrics != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.medium)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                                .clickable { lyricsExpanded = !lyricsExpanded }
                                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                                .align(Alignment.End)
                        ) {
                            Text(
                                text = "LYRICS",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Image(
                                imageVector = if (lyricsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                        }

                    }
                }
            }
            if (lyricsExpanded) {
                Divider(modifier = Modifier
                    .padding(top = 0.dp, bottom = 8.dp)
                    .alpha(0.3f))
                Text(
                    text = "${trackCardArgs.lyrics})",
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@DevicePreviewNight
@Composable
fun ResultCardPreview() {
    MusicRecognizerTheme {
        Surface {
            Column(modifier = Modifier.padding(8.dp)) {
                TrackCard(
                    trackCardArgs = fakeTrackCardArgs,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Suppress("SpellCheckingInspection")
val fakeTrackCardArgs = TrackCardArgs(
    author = "Pink Floyd",
    track = "Wish You Were Here",
    album = "Meddle",
    year = "1973",
    lyrics = "[Verse 1: Mike Shinoda]\nWhy does it feel like night today?\nSomething in here's not right today\nWhy am I so uptight today?\nParanoia's all I got left\n\nI don't know what stressed me first\nOr how the pressure was fed\nBut I know just what it feels like\nTo have a voice in the back of my head\n\nLike a face that I hold inside\nA face that awakes when I close my eyes\nA face that watches every time I lie\nA face that laughs every time I fall\n\n(And watches everything)\nSo I know that when it's time to sink or swim\nThat the face inside is here in me\nRight underneath my skin\n\n[Chorus: Chester Bennington &amp; Mike Shinoda]\nIt's like I'm paranoid, looking over my back\nIt's like a whirlwind inside of my head\nIt's like I can't stop what I'm hearing within\nIt's like the face inside is right beneath my skin\n[Verse 2: Mike Shinoda]\nI know I've got a face in me\nPoints out all my mistakes to me\nYou've got a face on the inside, too\nAnd your paranoia's probably worse\n\nI don't know what set me off first\nBut I know what I can't stand\nEverybody acts like the fact of the matter\nIs I can't add up to what you can\n\nBut everybody has a face that they hold inside\nA face that awakes when I close my eyes\nA face that watches every time they lie\nA face that laughs every time they fall\n\n(And watches everything)\nSo you know that when it's time to sink or swim\nThat the face inside is watching you, too\nRight inside your skin\n\n[Chorus: Chester Bennington &amp; Mike Shinoda]\nIt's like I'm paranoid, looking over my back\nIt's like a whirlwind inside of my head\nIt's like I can't stop what I'm hearing within\nIt's like the face inside is right beneath the skin\nIt's like I'm paranoid, looking over my back\nIt's like a whirlwind inside of my head\nIt's like I can't stop what I'm hearing within\nIt's like the face inside is right beneath my skin\n\n[Interlude: Mike Shinoda]\nThe face inside is right beneath your skin\nThe face inside is right beneath your skin\nThe face inside is right beneath your skin\n\n[Bridge: Chester Bennington]\nThe sun goes down\nI feel the light betray me\nThe sun goes down\nI feel the light betray me\n\n[Outro: Chester Bennington &amp; Mike Shinoda]\n(The sun) It's like I'm paranoid, looking over my back\nIt's like a whirlwind inside of my head\nIt's like I can't stop what I'm hearing within\nIt's like the face inside is right beneath the skin\n(I feel the light betray me)\n\n(The sun) It's like I'm paranoid, looking over my back\nIt's like a whirlwind inside of my head\nIt's like I can't stop what I'm hearing within\nIt's like I can't stop what I'm hearing within\n(I feel the light betray me)\nIt's like I can't stop what I'm hearing within\nIt's like the face inside is right beneath my skin",
    imageUri = Uri.parse("https://upload.wikimedia.org/wikipedia/ru/1/1e/Meddle_album_cover.jpg")
)
