package com.mrsep.musicrecognizer.core.common.di

import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatter
import com.mrsep.musicrecognizer.core.common.util.AppDateTimeFormatterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface UtilModule {

    @Binds
    fun bindAppDateTimeFormatter(implementation: AppDateTimeFormatterImpl): AppDateTimeFormatter

}