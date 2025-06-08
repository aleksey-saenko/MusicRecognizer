package com.mrsep.musicrecognizer.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mrsep.musicrecognizer.core.database.ApplicationDatabase
import com.mrsep.musicrecognizer.core.database.DatabaseBackupProvider
import com.mrsep.musicrecognizer.core.database.migration.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

const val DATABASE_NAME = "application_database"

@Module
@InstallIn(SingletonComponent::class)
internal class RoomModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext appContext: Context,
        backupProvider: DatabaseBackupProvider,
    ): ApplicationDatabase {
        return Room.databaseBuilder(
            appContext,
            ApplicationDatabase::class.java,
            DATABASE_NAME
        )
            .apply {
                val backupInputStream = backupProvider.getDatabaseBackup() ?: return@apply
                appContext.deleteDatabase(DATABASE_NAME)
                val callback = object : RoomDatabase.PrepackagedDatabaseCallback() {
                    override fun onOpenPrepackagedDatabase(db: SupportSQLiteDatabase) {
                        super.onOpenPrepackagedDatabase(db)
                        backupProvider.onDatabaseRestored()
                    }
                }
                createFromInputStream(
                    inputStreamCallable = { backupInputStream },
                    callback = callback
                )
            }
            .addMigrations(
                Migration5To6,
                Migration6To7,
                Migration7To8,
                Migration8To9,
                Migration7To9,
            )
            .build()
    }
}
