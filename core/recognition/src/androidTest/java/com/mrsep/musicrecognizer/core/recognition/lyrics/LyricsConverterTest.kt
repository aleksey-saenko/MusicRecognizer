package com.mrsep.musicrecognizer.core.recognition.lyrics

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mrsep.musicrecognizer.core.domain.track.model.SyncedLyrics
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

// It's better to use instrumented tests as Android’s regex engine is differ
@RunWith(AndroidJUnit4::class)
class LyricsConverterTest {

    private val converter = LyricsConverter()

    //region <normal cases>
    @Test
    fun combinedTest() {
        val input = """
        Provided by some provider
        [ti: Title of the song]
        [ar: Artist performing the song]
        [al:Album of the song]
        [AU: Author of the song ]
        [Length: 04:20]
        [offset: -10]
        [#: Comments]   
        [00:12.00]Line 1
        
        [00:17.20]Line 2
        [00:18.00] <00:18.45> Line <00:19.10>  3 <00:20.53> words 
        [00:21.10][00:45.10] Repeating line
        [00:30.00]In between
        [00:50.00] The  last line
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        val expected = SyncedLyricsWithMetadata(
            metadata = mapOf(
                "ti" to "Title of the song",
                "ar" to "Artist performing the song",
                "al" to "Album of the song",
                "au" to "Author of the song",
                "length" to "04:20",
                "offset" to "-10",
                "#" to "Comments",
            ),
            lines = listOf(
                SyncedLyrics.Line(12.seconds, "Line 1"),
                SyncedLyrics.Line(17.2.seconds, "Line 2"),
                SyncedLyrics.Line(18.seconds, "Line 3 words"),
                SyncedLyrics.Line(21.1.seconds, "Repeating line"),
                SyncedLyrics.Line(30.seconds, "In between"),
                SyncedLyrics.Line(45.1.seconds, "Repeating line"),
                SyncedLyrics.Line(50.seconds, "The  last line"),
            )
        )
        result.shouldNotBeNull()
        result.shouldBe(expected)
        result.title.shouldBe("Title of the song")
        result.artist.shouldBe("Artist performing the song")
        result.album.shouldBe("Album of the song")
        result.author.shouldBe("Author of the song")
        result.offset.shouldBe((-10).milliseconds)
        result.length.shouldBe(4.minutes + 20.seconds)
    }

    @Test
    fun trimMetadataAndLinesContent() {
        val input = """
        [ ti : Title of the song  ]
        [00:17.20]      Line 1 
        [00:20.20] 
        [00:20.30]
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        val expected = SyncedLyricsWithMetadata(
            metadata = mapOf(
                "ti" to "Title of the song",
            ),
            lines = listOf(
                SyncedLyrics.Line(17.2.seconds, "Line 1"),
                SyncedLyrics.Line(20.2.seconds, ""),
                SyncedLyrics.Line(20.3.seconds, ""),
            )
        )
        result.shouldBe(expected)
    }

    @Test
    fun keepEmptyOrBlankLines() {
        val input = """
        [00:00.00]   
        [00:10.00]Line 1 
        [00:20.00]
        [00:30.00] 
        [00:40.00]Line 2 
        [00:50.00]
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = false)
        val expected = SyncedLyricsWithMetadata(
            metadata = emptyMap(),
            lines = listOf(
                SyncedLyrics.Line(0.seconds, ""),
                SyncedLyrics.Line(10.seconds, "Line 1"),
                SyncedLyrics.Line(20.seconds, ""),
                SyncedLyrics.Line(30.seconds, ""),
                SyncedLyrics.Line(40.seconds, "Line 2"),
                SyncedLyrics.Line(50.seconds, ""),
            )
        )
        result.shouldBe(expected)
    }

    @Test
    fun respectMetadataParsingSkip() {
        val input = """
        [ti: Title of the song]
        [ar: Artist performing the song]
        [00:17.20]Line 1
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = false)
        val expected = SyncedLyricsWithMetadata(
            metadata = emptyMap(),
            lines = listOf(SyncedLyrics.Line(17.2.seconds, "Line 1"))
        )
        result.shouldBe(expected)
    }

    @Test
    fun copyAndShiftByOffset() {
        val original = SyncedLyricsWithMetadata(
            metadata = mapOf(
                "length" to "04:20",
                "offset" to "-100",
            ),
            lines = listOf(
                SyncedLyrics.Line(12.seconds, "Line 1"),
                SyncedLyrics.Line(17.2.seconds, "Line 2"),
            )
        )
        val copy1 = original.copyAndShiftByOffset(original.offset)
        val expectedCopy1 = SyncedLyricsWithMetadata(
            metadata = mapOf(
                "length" to "04:20",
                "offset" to "0",
            ),
            lines = listOf(
                SyncedLyrics.Line(11.9.seconds, "Line 1"),
                SyncedLyrics.Line(17.1.seconds, "Line 2"),
            )
        )
        copy1.shouldBe(expectedCopy1)

        val copy2 = original.copyAndShiftByOffset(200.milliseconds)
        val expectedCopy2 = SyncedLyricsWithMetadata(
            metadata = mapOf(
                "length" to "04:20",
                "offset" to "-300",
            ),
            lines = listOf(
                SyncedLyrics.Line(12.2.seconds, "Line 1"),
                SyncedLyrics.Line(17.4.seconds, "Line 2"),
            )
        )
        copy2.shouldBe(expectedCopy2)

        val copy3 = original.copyAndShiftByOffset((original.lines.first().timestamp + 1.seconds) * -1)
        copy3.shouldBeNull()
    }
    //endregion

    //region <allowed deviations>
    @Test
    fun validTimeTagVariations() {
        val input = """
        [00:12:20]Line 1
        [01:17.30]Line 2
        [01:20]Line 3
        [02:00:50][03:00.50] Repeating line
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        val expected = SyncedLyricsWithMetadata(
            metadata = emptyMap(),
            lines = listOf(
                SyncedLyrics.Line(12.2.seconds, "Line 1"),
                SyncedLyrics.Line(1.minutes + 17.3.seconds, "Line 2"),
                SyncedLyrics.Line(1.minutes + 20.seconds, "Line 3"),
                SyncedLyrics.Line(2.minutes + 0.5.seconds, "Repeating line"),
                SyncedLyrics.Line(3.minutes + 0.5.seconds, "Repeating line"),
            )
        )
        result.shouldBe(expected)
    }

    @Test
    fun dropWordTimestamps() {
        val input = """
        [00:10:00] <00:10.45> Line <00:15:00>  1 
        [00:20:00] <00:21.45> Line 2
        [00:30:00]<00:30.45>Line<00:35:00>3
        [00:40:00]  <00:40> Line    <00:42> 4   
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = false)
        val expected = SyncedLyricsWithMetadata(
            metadata = emptyMap(),
            lines = listOf(
                SyncedLyrics.Line(10.seconds, "Line 1"),
                SyncedLyrics.Line(20.seconds, "Line 2"),
                SyncedLyrics.Line(30.seconds, "Line 3"),
                SyncedLyrics.Line(40.seconds, "Line 4"),
            )
        )
        result.shouldBe(expected)
    }

    @Test
    fun sortUnsortedLines() {
        val input = """
        [00:12.20]Line 1
        [01:40.30]Line 3
        [00:17.50]Line 2
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = false)
        val expected = SyncedLyricsWithMetadata(
            metadata = emptyMap(),
            lines = listOf(
                SyncedLyrics.Line(12.2.seconds, "Line 1"),
                SyncedLyrics.Line(17.5.seconds, "Line 2"),
                SyncedLyrics.Line(1.minutes + 40.3.seconds, "Line 3"),
            )
        )
        result.shouldBe(expected)
    }

    @Test
    fun skipLinesWithMalformedTags() {
        val input = """
        [ti-Title of the song]
        [ti]Title of the song
        [00:12.20]Line 1
        [00:-12.20]Line 0
        [00.12.20]Line 0
        [+06:12.20]Line 0
        [ 06:12.20 ]Line 0
        [00:121.20]Line 0
        [1m:5s.20]Line 0
        [00:12.:10]Line 0
        [00:12:.10]Line 0
        [01]Line 0
        [00:20.00]Line 2
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        val expected = SyncedLyricsWithMetadata(
            metadata = emptyMap(),
            lines = listOf(
                SyncedLyrics.Line(12.2.seconds, "Line 1"),
                SyncedLyrics.Line(20.seconds, "Line 2"),
            )
        )
        result.shouldBe(expected)
    }

    @Test
    fun keepLastMetadataTag() {
        val input = """
        [offset: -10]
        [00:20.00]Line 1
        [offset: -20]
        [00:40.00]Line 2
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        val expected = SyncedLyricsWithMetadata(
            metadata = mapOf("offset" to "-20"),
            lines = listOf(
                SyncedLyrics.Line(20.seconds, "Line 1"),
                SyncedLyrics.Line(40.seconds, "Line 2"),
            )
        )
        result.shouldNotBeNull()
        result.shouldBe(expected)
        result.offset.shouldBe((-20).milliseconds)
    }
    //endregion

    //region <malformed lyrics>
    @Test
    fun emptyInputIsError() {
        val result = converter.parseFromString("", parseMetadata = true)
        result.shouldBeNull()
    }

    @Test
    fun emptyLyricsIsError() {
        val input = """
        [ti: Title of the song]
        [ar: Artist performing the song]
        Lyrics without timestamp tag 
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        result.shouldBeNull()
    }

    @Test
    fun blankLyricsIsError() {
        val input = """
        [ti: Title of the song]
        [ar: Artist performing the song]
        [00:17.50] 
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        result.shouldBeNull()
    }

    @Test
    fun overlappingLyricsIsError() {
        val input = """
        [00:10.00]Line 1
        [00:10.00]Line 2
        [00:17.50]Line 3
        """.trimIndent()
        val result = converter.parseFromString(input, parseMetadata = true)
        result.shouldBeNull()
    }
    //endregion

    //region <formatter tests>
    @Test
    fun formatCombinedTest() {
        val input = SyncedLyricsWithMetadata(
            metadata = mapOf(
                "ti" to "Title of the song",
                "ar" to "Artist performing the song",
                "length" to "04:20",
                "offset" to "-10",
                "#" to "Comments",
            ),
            lines = listOf(
                SyncedLyrics.Line(0.seconds, "Line 0"),
                SyncedLyrics.Line(12.seconds, "Line 1"),
                SyncedLyrics.Line(17.2.seconds, "Line 2"),
                SyncedLyrics.Line(21.1.seconds, "Repeating line"),
                SyncedLyrics.Line(45.1.seconds, "Repeating line"),
                SyncedLyrics.Line(60.0.seconds, "Line 3"),
                SyncedLyrics.Line(90.0.seconds, "Repeating line"),
            )
        )
        val expected = """
        [ti:Title of the song]
        [ar:Artist performing the song]
        [length:04:20]
        [offset:-10]
        [#:Comments]
        [00:00.00]Line 0
        [00:12.00]Line 1
        [00:17.20]Line 2
        [00:21.10][00:45.10][01:30.00]Repeating line
        [01:00.00]Line 3
        """.trimIndent()
        val result = converter.formatToString(input, formatMetadata = true)
        result.shouldBe(expected)
    }

    @Test
    fun respectMetadataFormattingSkip() {
        val input = SyncedLyricsWithMetadata(
            metadata = mapOf("length" to "04:20"),
            lines = listOf(SyncedLyrics.Line(0.seconds, "   "))
        )
        val expected = "[00:00.00]"
        val result = converter.formatToString(input, formatMetadata = false)
        result.shouldBe(expected)
    }
    //endregion
}
