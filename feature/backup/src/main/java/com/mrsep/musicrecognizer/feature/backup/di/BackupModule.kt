package com.mrsep.musicrecognizer.feature.backup.di

import com.mrsep.musicrecognizer.core.database.DatabaseBackupProvider
import com.mrsep.musicrecognizer.feature.backup.AppBackupManager
import com.mrsep.musicrecognizer.feature.backup.data.AppBackupManagerImpl
import com.mrsep.musicrecognizer.feature.backup.data.DatabaseBackupProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface BackupModule {

    @Binds
    fun bindAppBackupManager(implementation: AppBackupManagerImpl): AppBackupManager

    @Binds
    fun bindDatabaseBackupProvider(implementation: DatabaseBackupProviderImpl): DatabaseBackupProvider
}