package com.mrsep.musicrecognizer.di

import android.content.Context
import com.mrsep.musicrecognizer.feature.backup.AppRestartManager
import com.mrsep.musicrecognizer.presentation.MainActivity.Companion.restartApplicationOnRestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidAppRestarter @Inject constructor(
    @ApplicationContext private val appContext: Context,
): AppRestartManager {

    override fun restartApplicationOnRestore() {
        appContext.restartApplicationOnRestore()
    }
}