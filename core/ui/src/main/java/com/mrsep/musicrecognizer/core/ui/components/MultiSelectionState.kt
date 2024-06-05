package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class MultiSelectionState<T>(selectedIds: List<T>) {

    private val selectedIds = mutableStateMapOf<T, Unit>().apply {
        putAll(selectedIds.associateWith { Unit })
    }

    val selectedCount get() = selectedIds.size

    val multiselectEnabled get() = selectedIds.isNotEmpty()

    fun select(ids: List<T>) {
        selectedIds.putAll(ids.associateWith { Unit })
    }

    fun toggleSelection(itemId: T) {
        if (selectedIds.containsKey(itemId)) {
            selectedIds.remove(itemId)
        } else {
            selectedIds[itemId] = Unit
        }
    }

    fun isSelected(itemId: T) = selectedIds.containsKey(itemId)

    fun getSelected() = selectedIds.keys.toList()

    fun deselectAll() = selectedIds.clear()

    companion object {
        fun <T> getSaver(): Saver<MultiSelectionState<T>, *> = listSaver(
            save = { state: MultiSelectionState<T> -> state.selectedIds.keys.toList() },
            restore = { selectedIds -> MultiSelectionState(selectedIds) }
        )
    }
}

@Composable
fun <T> rememberMultiSelectionState(
    vararg inputs: Any?,
    selectedIds: List<T> = emptyList()
): MultiSelectionState<T> {
    return rememberSaveable(inputs = inputs, saver = MultiSelectionState.getSaver()) {
        MultiSelectionState(selectedIds)
    }
}
