package com.mrsep.musicrecognizer.data.di

import android.content.Context
import androidx.room.Room
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.data.database.migration.Migration5To6
import com.mrsep.musicrecognizer.data.database.migration.Migration6To7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "application_database"

@Module
@InstallIn(SingletonComponent::class)
internal class RoomModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext appContext: Context): ApplicationDatabase {
        return Room.databaseBuilder(
            appContext,
            ApplicationDatabase::class.java,
            DATABASE_NAME
        )
            .addMigrations(
                Migration5To6,
                Migration6To7,
            )
            .build()
    }
}
