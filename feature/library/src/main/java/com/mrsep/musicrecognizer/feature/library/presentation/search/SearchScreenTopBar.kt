package com.mrsep.musicrecognizer.feature.library.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackDataField
import kotlinx.collections.immutable.ImmutableSet
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreenTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    resetSearchQuery: () -> Unit,
    onBackPressed: () -> Unit,
    onSearchScopeChanged: (Set<TrackDataField>) -> Unit,
    searchScope: ImmutableSet<TrackDataField>,
    focusRequester: FocusRequester,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = modifier,
        title = {
            val interactionSource = remember { MutableInteractionSource() }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = modifier
                    .height(SearchBarDefaults.InputFieldHeight)
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = LocalTextStyle.current.color.takeOrElse {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { }),
                interactionSource = interactionSource,
                decorationBox = @Composable { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        value = query,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        placeholder = { Text(stringResource(StringsR.string.search_track_hint)) },
                        shape = SearchBarDefaults.inputFieldShape,
                        colors = SearchBarDefaults.inputFieldColors(),
                        contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                        container = {},
                    )
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    painter = painterResource(UiR.drawable.outline_arrow_back_24),
                    contentDescription = stringResource(StringsR.string.back)
                )
            }
        },
        actions = {
            Row {
                AnimatedVisibility(
                    visible = query.isNotBlank(),
                    enter = fadeIn(spring()),
                    exit = fadeOut(spring()),
                ) {
                    IconButton(onClick = resetSearchQuery) {
                        Icon(
                            painter = painterResource(UiR.drawable.outline_close_24),
                            contentDescription = stringResource(StringsR.string.clear_search_query)
                        )
                    }
                }
                SearchScopeDropdownMenu(
                    searchScope = searchScope,
                    onSearchScopeChanged = onSearchScopeChanged,
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}