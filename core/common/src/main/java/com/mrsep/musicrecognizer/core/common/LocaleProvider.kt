package com.mrsep.musicrecognizer.core.common

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

fun interface LocaleProvider {
    fun get(): Locale
}

internal class AppLocaleProvider @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : LocaleProvider {
    override fun get(): Locale = appContext.resources.configuration.locales[0]
}

internal class JvmLocaleProvider @Inject constructor() : LocaleProvider {
    override fun get(): Locale = Locale.getDefault()
}
