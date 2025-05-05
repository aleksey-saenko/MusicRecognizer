package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class MultiSelectionState<T>(selectedIds: List<T>) {

    private val selectedIds = mutableStateSetOf<T>().apply { addAll(selectedIds) }

    val selectedCount get() = selectedIds.size

    val multiselectEnabled get() = selectedIds.isNotEmpty()

    fun select(ids: List<T>) {
        selectedIds.addAll(ids)
    }

    fun toggleSelection(itemId: T) {
        if (itemId in selectedIds) selectedIds.remove(itemId) else selectedIds.add(itemId)
    }

    fun isSelected(itemId: T) = itemId in selectedIds

    fun getSelected(): Set<T> = selectedIds

    fun deselectAll() = selectedIds.clear()

    companion object {
        fun <T> getSaver(): Saver<MultiSelectionState<T>, *> = listSaver(
            save = { state: MultiSelectionState<T> -> state.selectedIds.toList() },
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
