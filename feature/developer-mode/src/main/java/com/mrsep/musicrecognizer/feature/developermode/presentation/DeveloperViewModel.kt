package com.mrsep.musicrecognizer.feature.developermode.presentation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.data.track.util.DatabaseFiller
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DeveloperViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val databaseFiller: DatabaseFiller,
//    private val recognitionDatabaseFiller: EnqRecognitionDBFiller,
    private val trackRepositoryDo: TrackRepositoryDo,
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

    fun clearDb() = processSuspend { trackRepositoryDo.deleteAll() }

    fun prepopulateDbFakes() = processSuspend { databaseFiller.prepopulateByFaker(count = 1_000) }

    fun prepopulateDbAssets() = processSuspend { databaseFiller.prepopulateFromAssets() }

    fun prepopulateRecognitionDb() { }
//        processSuspend { recognitionDatabaseFiller.prepopulateWithStoredTracks() }
}

private fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}