package com.mrsep.musicrecognizer.presentation.screens.recently

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.isContainer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSearchBar(
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    fun closeSearchBar() {
        focusManager.clearFocus()
        active = false
    }

    Box() {//Modifier.fillMaxSize()
        // Talkback focus order sorts based on x and y position before considering z-index. The
        // extra Box with fillMaxWidth is a workaround to get the search bar to focus before the
        // content.
        Box(modifier.semantics { isContainer = true }.zIndex(1f).fillMaxWidth()) {
            DockedSearchBar(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(16.dp),
                query = text,
                onQueryChange = { text = it },
                onSearch = { closeSearchBar() },
                active = false,
//                active = active,
                onActiveChange = {
                    active = it
                    if (!active) focusManager.clearFocus()
                },
                placeholder = { Text("Search track...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                colors = SearchBarDefaults.colors(
//                    containerColor = MaterialTheme.colorScheme.background,
//                    dividerColor = MaterialTheme.colorScheme.primary,
//                    inputFieldColors = inputFieldColors(),
//                ),
//                tonalElevation = if (active) 0.dp else 3.dp
            ) {
//                LazyColumn(
//                    modifier = Modifier.fillMaxWidth(),
//                    contentPadding = PaddingValues(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    items(4) { idx ->
//                        val resultText = "Suggestion $idx"
//                        ListItem(
//                            headlineText = { Text(resultText) },
//                            supportingText = { Text("Additional info") },
//                            leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
//                            modifier = Modifier.clickable {
//                                text = resultText
//                                closeSearchBar()
//                            }
//                        )
//                    }
//                }
            }
        }

//        LazyColumn(
//            contentPadding = PaddingValues(start = 16.dp, top = 72.dp, end = 16.dp, bottom = 16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            val list = List(100) { "Text $it" }
//            items(count = list.size) {
//                Text(list[it], Modifier.fillMaxWidth().padding(horizontal = 16.dp))
//            }
//        }
    }
}