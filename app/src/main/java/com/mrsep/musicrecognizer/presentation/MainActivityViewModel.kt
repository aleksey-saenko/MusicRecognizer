package com.mrsep.musicrecognizer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiStateStream = preferencesRepository.userPreferencesFlow
        .map { MainActivityUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

    fun isLoadingState() = uiStateStream.value is MainActivityUiState.Loading

    fun setNotificationServiceEnabled(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationServiceEnabled(value)
        }
    }

}

@Immutable
sealed class MainActivityUiState {

    data object Loading: MainActivityUiState()

    data class Success(val userPreferences: UserPreferences): MainActivityUiState()

}