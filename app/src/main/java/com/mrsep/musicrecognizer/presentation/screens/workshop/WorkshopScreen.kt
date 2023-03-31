package com.mrsep.musicrecognizer.presentation.screens.workshop

import android.widget.Space
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

fun NavGraphBuilder.workshopScreen() {
    composable("workshop") {
        WorkshopScreen()
    }
}

@Immutable
class MyDate() {
    private val formatter = DateTimeFormatter.ISO_DATE

    private val dateParser = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private fun String.toLocalDate() = runCatching { LocalDate.parse(this, dateParser) }.getOrNull()

    val unixTime = 1679313600L //Mon Mar 20 2023 12:00:00 GMT+0000 //Mon Mar 20 2023 15:00:00 GMT+0300 (Moscow Standard Time) //SECONDS NOT MILLIE
    val offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneOffset.systemDefault())
    val localDateTime = LocalDateTime.ofEpochSecond(unixTime, 0, ZoneOffset.ofHours(+3))
    val current = OffsetDateTime.parse("2023-03-28T10:00:00.000+03:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
    val past = Instant.now().epochSecond
    fun main() {
        OffsetDateTime.now()
        Instant.MIN
    }
}

@Composable
fun WorkshopScreen(
    modifier: Modifier = Modifier,
    viewModel: WorkshopViewModel = hiltViewModel(),
    dateClass: MyDate = MyDate()
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = viewModel::startFlowTest) {
            Text(text = "START TEST")
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "offsetDateTime=${dateClass.offsetDateTime}\n" +
                    "localDateTime=${dateClass.localDateTime}\n" +
                    "current=${dateClass.current}\n" +
                    "past=${dateClass.past}\n"
        )

    }
}

@Composable
private fun AudioGraph(
    modifier: Modifier = Modifier,
    chunk: FloatArray
) {
//    Canvas(modifier = modifier) {
//        chunk.forEach {
//            drawLine(
//                color = Color.White,
//                start = Offset(x =, y =),
//                end = Offset(x =, y =)
//            )
//        }
//    }
}