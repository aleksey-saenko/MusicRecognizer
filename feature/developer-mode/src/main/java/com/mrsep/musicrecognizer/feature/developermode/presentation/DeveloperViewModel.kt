package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.feature.developermode.domain.DatabaseInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DeveloperViewModel @Inject constructor(
    private val databaseInteractor: DatabaseInteractor
) : ViewModel() {

    private var counter = MutableStateFlow(0)

    val isProcessing = counter.map { it > 0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private fun processSuspend(block: suspend () -> Unit) {
        viewModelScope.launch {
            counter.getAndUpdate { value -> value + 1 }
            block()
            counter.getAndUpdate { value -> value - 1 }
        }
    }

    fun clearDb() = processSuspend { databaseInteractor.clear() }

    fun prepopulateDbFakes() = processSuspend { databaseInteractor.prepopulateByFaker(1000) }

    fun prepopulateDbAssets() = processSuspend { databaseInteractor.prepopulateFromAssets() }

}