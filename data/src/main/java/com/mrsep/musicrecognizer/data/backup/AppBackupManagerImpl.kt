package com.mrsep.musicrecognizer.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import coil3.imageLoader
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.common.util.getAppVersionCode
import com.mrsep.musicrecognizer.data.database.ApplicationDatabase
import com.mrsep.musicrecognizer.data.di.DATABASE_NAME
import com.mrsep.musicrecognizer.data.di.USER_PREFERENCES_STORE
import com.mrsep.musicrecognizer.data.enqueued.RecordingFileDataSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/* Work in progress */
internal class AppBackupManagerImpl @Inject constructor(
    private val database: ApplicationDatabase,
    private val recordingFileDataSource: RecordingFileDataSource,
    private val userPreferencesDataStore: DataStore<UserPreferencesProto>,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val appScope: CoroutineScope,
    moshi: Moshi,
) : AppBackupManager {

    private val restoreCoroutineContext = appScope.coroutineContext + ioDispatcher

    @OptIn(ExperimentalStdlibApi::class)
    private val metadataJsonAdapter = moshi.adapter<BackupMetadata>()

    override suspend fun estimateAppDataSize(): Map<BackupEntry, Long> {
        return withContext(ioDispatcher) {
            val databaseSize = database.getDataSize()
            val recordingsSize = recordingFileDataSource.getTotalSize()
            val preferencesSize = userPreferencesDataStore.data.first().serializedSize.toLong()
            mapOf(
                BackupEntry.Data to databaseSize + recordingsSize,
                BackupEntry.Preferences to preferencesSize,
            )
        }
    }

    override suspend fun backup(
        destination: Uri,
        entries: Set<BackupEntry>,
    ): BackupResult = withContext(ioDispatcher) {
        check(entries.isNotEmpty()) { "At least one BackupEntry must be provided" }
        try {
            val outputStream = try {
                requireNotNull(appContext.contentResolver.openOutputStream(destination))
            } catch (e: Exception) {
                return@withContext BackupResult.FileNotFound
            }
            ZipOutputStream(outputStream.buffered()).use { zipInputStream ->
                writeMetadata(zipInputStream, entries.toSet())
                if (entries.contains(BackupEntry.Data)) {
                    exportDatabase(zipInputStream)
                    exportRecordings(zipInputStream)
                }
                if (entries.contains(BackupEntry.Preferences)) {
                    exportUserPreferences(zipInputStream)
                }
            }
            BackupResult.Success
        } catch (e: CancellationException) {
            deleteUnfinishedBackup(destination)
            throw e
        } catch (e: Exception) { // potential ZipException or IOException
            Log.e(this::class.simpleName, "Fatal error while creating backup", e)
            deleteUnfinishedBackup(destination)
            BackupResult.UnhandledError
        }
    }

    private fun deleteUnfinishedBackup(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(appContext.contentResolver, uri)
        } catch (e: FileNotFoundException) {
            Log.e(this::class.simpleName, "Failed to delete unfinished backup file", e)
        }
    }

    private suspend fun exportDatabase(zipOutputStream: ZipOutputStream) {
        val appDatabaseFile = appContext.getDatabasePath(DATABASE_NAME)
        if (!appDatabaseFile.exists()) error("App database file is not found")
        if (!database.checkoutWithRetry()) {
            error("Database checkpoint was not performed, database is busy")
        }
        val zipEntry = ZipEntry(DATABASE_ZIP_ENTRY)
        with(zipOutputStream) {
            putNextEntry(zipEntry)
            appDatabaseFile.inputStream().buffered().use { input ->
                input.copyTo(this)
            }
            closeEntry()
        }
    }

    private fun exportRecordings(zipOutputStream: ZipOutputStream) {
        val recordings = recordingFileDataSource.getFiles()
        with(zipOutputStream) {
            for (recording in recordings) {
                putNextEntry(ZipEntry("$RECORDINGS_DIR_ZIP_ENTRY${recording.name}"))
                recording.inputStream().buffered().use { input ->
                    input.copyTo(this)
                }
                closeEntry()
            }
        }
    }

    private suspend fun exportUserPreferences(zipOutputStream: ZipOutputStream) {
        val preferences = userPreferencesDataStore.data.first()
        with(zipOutputStream) {
            putNextEntry(ZipEntry(PREFERENCES_ZIP_ENTRY))
            preferences.writeTo(this)
            closeEntry()
        }
    }

    override suspend fun readBackupMetadata(
        source: Uri,
    ): BackupMetadataResult = withContext(ioDispatcher) {
        try {
            val inputStream = try {
                requireNotNull(appContext.contentResolver.openInputStream(source))
            } catch (e: Exception) {
                return@withContext BackupMetadataResult.FileNotFound
            }
            ZipInputStream(inputStream.buffered()).use { zipInputStream ->
                var metadata: BackupMetadata? = null
                val foundEntries = mutableMapOf<BackupEntry, Long>()

                var currentEntry: ZipEntry? = zipInputStream.nextEntry
                // The first entry must be metadata file
                if (currentEntry?.name == METADATA_ZIP_ENTRY) {
                    metadata = readMetadata(zipInputStream)
                }
                if (metadata == null) return@withContext BackupMetadataResult.NotBackupFile
                zipInputStream.closeEntry()

                currentEntry = zipInputStream.nextEntry
                while (currentEntry != null) {
                    // Metadata fields in the ZipEntry are only available after the entry data has been read
                    // This is because the metadata follows the data in the Zip format
                    zipInputStream.closeEntry()
                    when (currentEntry.name) {
                        DATABASE_ZIP_ENTRY -> {
                            foundEntries[BackupEntry.Data] = currentEntry.size
                        }

                        RECORDINGS_DIR_ZIP_ENTRY -> {}  // just skip

                        PREFERENCES_ZIP_ENTRY -> {
                            foundEntries[BackupEntry.Preferences] = currentEntry.size
                        }

                        else -> {
                            findRecordingName(currentEntry)
                                ?: return@withContext BackupMetadataResult.MalformedBackup
                            // Database file must precede audio recordings
                            val dataSize = foundEntries[BackupEntry.Data]
                                ?: return@withContext BackupMetadataResult.MalformedBackup
                            foundEntries[BackupEntry.Data] = dataSize + currentEntry.size
                        }
                    }
                    currentEntry = zipInputStream.nextEntry
                }
                BackupMetadataResult.Success(
                    metadata = metadata,
                    entryUncompressedSize = foundEntries,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Fatal error while reading backup metadata", e)
            BackupMetadataResult.UnhandledError
        }
    }

    override suspend fun restore(
        source: Uri,
        entries: Set<BackupEntry>,
    ): RestoreResult = withContext(restoreCoroutineContext) {
        check(entries.isNotEmpty()) { "At least one BackupEntry must be provided" }
        val inputStream = try {
            requireNotNull(appContext.contentResolver.openInputStream(source))
        } catch (e: Exception) {
            return@withContext RestoreResult.FileNotFound
        }
        try {
            ZipInputStream(inputStream).use { zipInputStream ->
                var metadata: BackupMetadata? = null
                var currentEntry: ZipEntry? = zipInputStream.nextEntry
                // The first entry must be backup metadata
                if (currentEntry?.name == METADATA_ZIP_ENTRY) {
                    metadata = readMetadata(zipInputStream)
                }
                if (metadata == null) return@withContext RestoreResult.NotBackupFile
                if (metadata.appVersionCode > appContext.getAppVersionCode()) {
                    return@withContext RestoreResult.NewerVersionBackup
                }
                zipInputStream.closeEntry()
                currentEntry = zipInputStream.nextEntry
                var appDataDeleted = false
                while (currentEntry != null) {
                    when (currentEntry.name) {
                        DATABASE_ZIP_ENTRY -> {
                            if (entries.contains(BackupEntry.Data)) {
                                deleteAppData()
                                appDataDeleted = true
//                                importDatabase(zipInputStream)
                                scheduleDatabaseImport(backupUri = source)
                            }
                        }

                        RECORDINGS_DIR_ZIP_ENTRY -> {} // just skip

                        PREFERENCES_ZIP_ENTRY -> {
                            if (entries.contains(BackupEntry.Preferences)) {
                                importPreferences(zipInputStream)
                            }
                        }

                        else -> {
                            val recordingName = findRecordingName(currentEntry)
                            if (recordingName != null) {
                                if (entries.contains(BackupEntry.Data) && appDataDeleted) {
                                    importRecording(recordingName, zipInputStream)
                                }
                            } else {
                                Log.w(
                                    this::class.simpleName,
                                    "Unknown backup entry \"${currentEntry.name}\""
                                )
                            }
                        }
                    }
                    zipInputStream.closeEntry()
                    currentEntry = zipInputStream.nextEntry
                }
            }
            RestoreResult.Success(appRestartRequired = true)
        } catch (e: CancellationException) {
            cleanOnRestoreError(source)
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Fatal error while restoring data from backup", e)
            cleanOnRestoreError(source)
            RestoreResult.UnhandledError
        }
    }

    private fun importDatabase(zipInputStream: ZipInputStream) {
        appContext.deleteDatabase(DATABASE_NAME)
        val databaseFile = appContext.getDatabasePath(DATABASE_NAME)
        databaseFile.createNewFile()
        databaseFile.outputStream().buffered().use { outputStream ->
            zipInputStream.copyTo(outputStream)
        }
    }

    private fun scheduleDatabaseImport(backupUri: Uri) {
        appContext.takePersistableUriPermission(backupUri)
        appContext.getDatabaseRestoreUriFile().run {
            if (exists()) delete()
            createNewFile()
            outputStream().buffered().use {
                it.write(backupUri.toString().encodeToByteArray())
            }
        }
    }

    private suspend fun importRecording(recordingName: String, zipInputStream: ZipInputStream) {
        recordingFileDataSource.import(zipInputStream, recordingName)
    }

    private fun importPreferences(zipInputStream: ZipInputStream) {
        val preferencesFile = appContext.dataStoreFile(USER_PREFERENCES_STORE)
        if (preferencesFile.exists()) {
            preferencesFile.delete()
        } else {
            Files.createDirectories(preferencesFile.toPath().parent)
        }
        preferencesFile.createNewFile()
        preferencesFile.outputStream().buffered().use { outputStream ->
            zipInputStream.copyTo(outputStream)
        }
    }

    private suspend fun deleteAppData() {
        // If we delete only rows, Room will inform all background workers about data deletion,
        // and they will cancel themselves. Bad contract?
        database.clearAllTables()
        recordingFileDataSource.deleteAll()
        with(appContext.imageLoader) {
            diskCache?.clear()
            memoryCache?.clear()
        }
    }

    private suspend fun cleanOnRestoreError(backupUri: Uri) {
        withContext(NonCancellable) {
            appContext.releasePersistableUriPermission(backupUri)
            appContext.getDatabaseRestoreUriFile().delete()
            deleteAppData()
        }
    }

    private fun writeMetadata(zipOutputStream: ZipOutputStream, entries: Set<BackupEntry>) {
        val metadata = BackupMetadata(
            backupSignature = BACKUP_VERIFICATION_UUID,
            appVersionCode = appContext.getAppVersionCode(),
            creationDate = Instant.now(),
            entries = entries,
        )
        val metadataJson = metadataJsonAdapter.toJson(metadata)
        with(zipOutputStream) {
            putNextEntry(ZipEntry(METADATA_ZIP_ENTRY))
            write(metadataJson.encodeToByteArray())
            closeEntry()
        }
    }

    private fun readMetadata(zipInputStream: ZipInputStream): BackupMetadata? {
        val metadataJson = zipInputStream.readBytes().decodeToString()
        return try {
            metadataJsonAdapter.fromJson(metadataJson)
                .takeIf { it?.backupSignature == BACKUP_VERIFICATION_UUID }
        } catch (e: Exception) {
            null
        }
    }

    private fun findRecordingName(entry: ZipEntry): String? {
        return recordingEntryNamePattern.matchEntire(entry.name)?.groups?.get(1)?.value
    }

    companion object {

        fun Context.getDatabaseRestoreUriFile(): File {
            return dataDir.resolve("databases/DATABASE_RESTORE_URI")
        }

        fun Context.getDatabaseInputStream(uri: Uri): InputStream? {
            return try {
                val inputStream = contentResolver.openInputStream(uri) ?: return null
                val zipInputStream = ZipInputStream(inputStream.buffered())
                var currentEntry: ZipEntry? = zipInputStream.nextEntry
                while (currentEntry != null) {
                    if (currentEntry.name == DATABASE_ZIP_ENTRY) {
                        return zipInputStream
                    }
                    currentEntry = zipInputStream.nextEntry
                }
                return null
            } catch (e: Exception) {
                null
            }
        }

        private const val PREFERENCES_ZIP_ENTRY = "preferences"
        private const val DATABASE_ZIP_ENTRY = "database"
        private const val METADATA_ZIP_ENTRY = "metadata"
        private const val RECORDINGS_DIR_ZIP_ENTRY = "audio_recordings/"
        private const val BACKUP_VERIFICATION_UUID = "9530d0d1-8023-4c3a-99d0-7cbb084020f1"

        private val recordingEntryNamePattern = Regex("^$RECORDINGS_DIR_ZIP_ENTRY(rec_\\d+)\$")
    }
}

internal fun Context.takePersistableUriPermission(uri: Uri) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    contentResolver.takePersistableUriPermission(uri, flags)
}

// https://commonsware.com/blog/2020/06/13/count-your-saf-uri-permission-grants.html
internal fun Context.releasePersistableUriPermission(uri: Uri) {
    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    contentResolver.releasePersistableUriPermission(uri, flags)
}
