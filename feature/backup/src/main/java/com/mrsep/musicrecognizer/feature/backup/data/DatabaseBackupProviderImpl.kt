package com.mrsep.musicrecognizer.feature.backup.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mrsep.musicrecognizer.core.database.DatabaseBackupProvider
import com.mrsep.musicrecognizer.feature.backup.data.AppBackupManagerImpl.Companion.getDatabaseInputStream
import com.mrsep.musicrecognizer.feature.backup.data.AppBackupManagerImpl.Companion.getDatabaseRestoreUriFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject

internal class DatabaseBackupProviderImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
): DatabaseBackupProvider {

    private var usedBackupUri: Uri? = null

    // This check should be super fast since it happens on Main thread each app startup
    override fun getDatabaseBackup(): InputStream? {
        val databaseRestoreUriFile = appContext.getDatabaseRestoreUriFile()
        if (!databaseRestoreUriFile.exists()) return null
        // Restoring...
        return try {
            check(usedBackupUri == null)
            val backupUri = Uri.parse(databaseRestoreUriFile.readText())
            usedBackupUri = backupUri
            databaseRestoreUriFile.delete()
            appContext.getDatabaseInputStream(backupUri)
        } catch (e: Exception) {
            Log.e(this::class.java.simpleName, "Failed to get input stream to restore database", e)
            null
        }
    }

    override fun onDatabaseRestored() {
        usedBackupUri?.let { appContext.releasePersistableUriPermission(it) }
    }
}
