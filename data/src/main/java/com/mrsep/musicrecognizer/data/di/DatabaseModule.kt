package com.mrsep.musicrecognizer.data.di

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.data.backup.AppBackupManagerImpl.Companion.getDatabaseInputStream
import com.mrsep.musicrecognizer.data.backup.AppBackupManagerImpl.Companion.getDatabaseRestoreUriFile
import com.mrsep.musicrecognizer.data.backup.releasePersistableUriPermission
import com.mrsep.musicrecognizer.data.database.migration.Migration5To6
import com.mrsep.musicrecognizer.data.database.migration.Migration6To7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

internal const val DATABASE_NAME = "application_database"

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
            .apply {
                // This check should be super fast since it happens on Main thread each app startup
                // (Room dependency injection to MainActivityViewModel)
                val databaseRestoreUriFile = appContext.getDatabaseRestoreUriFile()
                if (!databaseRestoreUriFile.exists()) return@apply
                // Restoring..
                val (databaseRestoreInputStream, callback) = try {
                    val backupUri = Uri.parse(databaseRestoreUriFile.readText())
                    databaseRestoreUriFile.delete()
                    val callback = object : RoomDatabase.PrepackagedDatabaseCallback() {
                        override fun onOpenPrepackagedDatabase(db: SupportSQLiteDatabase) {
                            super.onOpenPrepackagedDatabase(db)
                            appContext.releasePersistableUriPermission(backupUri)
                        }
                    }
                    val inputStream = requireNotNull(appContext.getDatabaseInputStream(backupUri))
                    inputStream to callback
                } catch (e: Exception) {
                    Log.e(this::class.java.simpleName, "Failed to get input stream to restore database", e)
                    return@apply
                }
                appContext.deleteDatabase(DATABASE_NAME)
                createFromInputStream(
                    inputStreamCallable = { databaseRestoreInputStream },
                    callback = callback
                )
            }
            .addMigrations(
                Migration5To6,
                Migration6To7,
            )
            .build()
    }
}
