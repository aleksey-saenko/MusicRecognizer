package com.mrsep.musicrecognizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiStateStream = preferencesRepository.userPreferencesFlow
        .map { MainActivityUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

    fun isLoadingState() = uiStateStream.value is MainActivityUiState.Loading

}

sealed interface MainActivityUiState {
    object Loading: MainActivityUiState
    data class Success(val userPreferences: UserPreferences): MainActivityUiState
}