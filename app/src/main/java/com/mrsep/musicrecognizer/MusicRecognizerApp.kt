package com.mrsep.musicrecognizer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class MusicRecognizerApp : Application(), ImageLoaderFactory {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    var onboardingCompleted = false

    override fun onCreate() {
        super.onCreate()
        onboardingCompleted = runBlocking {
            preferencesRepository.userPreferencesFlow.first().onboardingCompleted
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .build()
    }

}