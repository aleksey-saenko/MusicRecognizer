package com.mrsep.musicrecognizer.presentation.screens.preferences

import androidx.lifecycle.ViewModel
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferencesFlow = preferencesRepository.userPreferencesFlow

}