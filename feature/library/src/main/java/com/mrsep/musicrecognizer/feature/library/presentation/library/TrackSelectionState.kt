package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
internal class TrackSelectionState(selectedIds: List<String>) {

    private val selectedIds = mutableStateMapOf<String, Unit>().apply {
        putAll(selectedIds.associateWith { Unit })
    }

    val selectedCount get() = selectedIds.size

    val multiselectEnabled get () = selectedIds.isNotEmpty()

    fun select(trackMbIds: List<String>) {
        selectedIds.putAll(trackMbIds.associateWith { Unit })
    }

    fun toggleSelection(trackMbId: String) {
        if (selectedIds.containsKey(trackMbId)) {
            selectedIds.remove(trackMbId)
        } else {
            selectedIds[trackMbId] = Unit
        }
    }

    fun isTrackSelected(mbId: String) = selectedIds.containsKey(mbId)

    fun getSelected() = selectedIds.keys.toList()

    fun deselectAll() = selectedIds.clear()

    companion object {
        val Saver: Saver<TrackSelectionState, *> = listSaver(
            save = { state -> state.selectedIds.keys.toList() },
            restore = { selectedIds -> TrackSelectionState(selectedIds) }
        )
    }
}

@Composable
internal fun rememberTracksSelectionState(
    vararg inputs: Any?,
    selectedIds: List<String> = emptyList()
): TrackSelectionState {
    return rememberSaveable(inputs = inputs, saver = TrackSelectionState.Saver) {
        TrackSelectionState(selectedIds)
    }
}