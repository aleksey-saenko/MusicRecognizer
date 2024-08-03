package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatter
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.repository.PreferencesRepository
import com.mrsep.musicrecognizer.feature.library.domain.repository.TrackRepository
import com.mrsep.musicrecognizer.feature.library.presentation.model.TrackUi
import com.mrsep.musicrecognizer.feature.library.presentation.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class LibraryViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dateTimeFormatter: AppDateTimeFormatter
) : ViewModel() {

    val uiState = combine(
        flow = preferencesRepository.userPreferencesFlow,
        flow2 = trackRepository.isEmptyFlow()
    ) { preferences, isDatabaseEmpty ->
        if (isDatabaseEmpty) {
            flowOf(
                LibraryUiState.Success(
                    trackList = persistentListOf(),
                    trackFilter = preferences.trackFilter,
                    isEmptyLibrary = true,
                    useGridLayout = preferences.useGridForLibrary,
                    showRecognitionDate = preferences.showRecognitionDateInLibrary
                )
            )
        } else {
            trackRepository.getTracksByFilterFlow(preferences.trackFilter)
                .map { trackList ->
                    LibraryUiState.Success(
                        trackList = trackList
                            .map { track -> track.toUi(dateTimeFormatter) }
                            .toImmutableList(),
                        trackFilter = preferences.trackFilter,
                        isEmptyLibrary = false,
                        useGridLayout = preferences.useGridForLibrary,
                        showRecognitionDate = preferences.showRecognitionDateInLibrary
                    )
                }
        }
    }.flatMapLatest { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState.Loading
        )

    fun applyFilter(trackFilter: TrackFilter) {
        viewModelScope.launch {
            preferencesRepository.setTrackFilter(trackFilter)
        }
    }

    fun deleteTracks(trackIds: List<String>) {
        viewModelScope.launch {
            trackRepository.delete(trackIds)
        }
    }

    fun setUseGrid(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUseGridForLibrary(value)
        }
    }

    fun setShowRecognitionDate(value: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShowRecognitionDateInLibrary(value)
        }
    }
}

@Immutable
internal sealed class LibraryUiState {

    data object Loading : LibraryUiState()

    data class Success(
        val trackList: ImmutableList<TrackUi>,
        val trackFilter: TrackFilter,
        val isEmptyLibrary: Boolean,
        val useGridLayout: Boolean,
        val showRecognitionDate: Boolean
    ) : LibraryUiState()
}
