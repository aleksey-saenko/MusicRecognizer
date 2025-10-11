package com.mrsep.musicrecognizer.feature.backup.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import coil3.imageLoader
import com.mrsep.musicrecognizer.core.datastore.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.common.util.getAppVersionCode
import com.mrsep.musicrecognizer.core.data.enqueued.AudioSampleDataSource
import com.mrsep.musicrecognizer.core.database.ApplicationDatabase
import com.mrsep.musicrecognizer.core.datastore.USER_PREFERENCES_STORE
import com.mrsep.musicrecognizer.feature.backup.AppBackupManager
import com.mrsep.musicrecognizer.feature.backup.BackupEntry
import com.mrsep.musicrecognizer.feature.backup.BackupMetadata
import com.mrsep.musicrecognizer.feature.backup.BackupMetadataResult
import com.mrsep.musicrecognizer.feature.backup.BackupResult
import com.mrsep.musicrecognizer.feature.backup.RestoreResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.io.path.exists

internal class AppBackupManagerImpl @Inject constructor(
    private val database: ApplicationDatabase,
    private val audioSampleDataSource: AudioSampleDataSource,
    private val userPreferencesDataStore: DataStore<UserPreferencesProto>,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val appScope: CoroutineScope,
    private val json: Json,
) : AppBackupManager {

    private val restoreCoroutineContext = appScope.coroutineContext + ioDispatcher

    override suspend fun estimateAppDataSize(): Map<BackupEntry, Long> {
        return withContext(ioDispatcher) {
            val databaseSize = database.getDataSize()
            val recordingsSize = audioSampleDataSource.getTotalSize()
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
            } catch (_: Exception) {
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
        coroutineContext.ensureActive()
        val appDatabasePath = appContext.getDatabasePath(database.openHelper.databaseName).toPath()
        check(appDatabasePath.exists()) { "App database file is not found" }
        check(database.checkoutWithRetry()) { "DB checkpoint was not performed, database is busy" }
        with(zipOutputStream) {
            putNextEntry(ZipEntry(DATABASE_ZIP_ENTRY))
            Files.copy(appDatabasePath, this)
            closeEntry()
        }
    }

    private suspend fun exportRecordings(zipOutputStream: ZipOutputStream) {
        val recordings = audioSampleDataSource.getFiles()
        with(zipOutputStream) {
            for (recording in recordings) {
                coroutineContext.ensureActive()
                putNextEntry(ZipEntry("$RECORDINGS_DIR_ZIP_ENTRY${recording.name}"))
                Files.copy(recording.toPath(), this)
                closeEntry()
            }
        }
    }

    private suspend fun exportUserPreferences(zipOutputStream: ZipOutputStream) {
        coroutineContext.ensureActive()
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
            } catch (_: Exception) {
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
        } catch (_: Exception) {
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

                var appDataDeleted = false
                currentEntry = zipInputStream.nextEntry
                while (currentEntry != null) {
                    ensureActive()
                    when (currentEntry.name) {
                        DATABASE_ZIP_ENTRY -> if (entries.contains(BackupEntry.Data)) {
                            deleteAppData()
                            appDataDeleted = true
                            importDatabase(zipInputStream)
                        }

                        RECORDINGS_DIR_ZIP_ENTRY -> {} // just skip

                        PREFERENCES_ZIP_ENTRY -> if (entries.contains(BackupEntry.Preferences)) {
                            importPreferences(zipInputStream)
                        }

                        else -> {
                            val recordingName = findRecordingName(currentEntry)
                            if (recordingName != null) {
                                if (entries.contains(BackupEntry.Data) && appDataDeleted) {
                                    importRecording(zipInputStream, recordingName)
                                }
                            } else {
                                val msg = "Unknown backup entry \"${currentEntry.name}\""
                                Log.w(this::class.simpleName, msg)
                            }
                        }
                    }
                    zipInputStream.closeEntry()
                    currentEntry = zipInputStream.nextEntry
                }
            }
            RestoreResult.Success(appRestartRequired = true)
        } catch (e: CancellationException) {
            cleanOnRestoreError()
            throw e
        } catch (e: Exception) {
            Log.e(this::class.simpleName, "Fatal error while restoring data from backup", e)
            cleanOnRestoreError()
            RestoreResult.UnhandledError
        }
    }

    private fun importDatabase(zipInputStream: ZipInputStream) {
        val databasePath = appContext.getDatabasePath(database.openHelper.databaseName).toPath()
        database.close()
        appContext.deleteDatabase(database.openHelper.databaseName)
        Files.createDirectories(databasePath.parent)
        Files.copy(zipInputStream, databasePath, StandardCopyOption.REPLACE_EXISTING)
    }

    private suspend fun importRecording(zipInputStream: ZipInputStream, filename: String) {
        audioSampleDataSource.import(zipInputStream, filename)
    }

    private fun importPreferences(zipInputStream: ZipInputStream) {
        val preferencesPath = appContext.dataStoreFile(USER_PREFERENCES_STORE).toPath()
        Files.deleteIfExists(preferencesPath)
        Files.createDirectories(preferencesPath.parent)
        Files.copy(zipInputStream, preferencesPath, StandardCopyOption.REPLACE_EXISTING)
    }

    private suspend fun deleteAppData() {
        // If we delete only rows, Room will inform all background workers about data deletion,
        // and they will cancel themselves. Bad contract?
        database.clearAllTables()
        audioSampleDataSource.deleteAll()
        with(appContext.imageLoader) {
            diskCache?.clear()
            memoryCache?.clear()
        }
    }

    private suspend fun cleanOnRestoreError() {
        withContext(NonCancellable) {
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
        val prettyJson = Json(json) { prettyPrint = true }
        val metadataJson = prettyJson.encodeToString(metadata)
        with(zipOutputStream) {
            putNextEntry(ZipEntry(METADATA_ZIP_ENTRY))
            write(metadataJson.encodeToByteArray())
            closeEntry()
        }
    }

    private fun readMetadata(zipInputStream: ZipInputStream): BackupMetadata? {
        val metadataJson = zipInputStream.readBytes().decodeToString()
        return try {
            json.decodeFromString<BackupMetadata>(metadataJson)
                .takeIf { it.backupSignature == BACKUP_VERIFICATION_UUID }
        } catch (_: Exception) {
            null
        }
    }

    private fun findRecordingName(entry: ZipEntry): String? {
        return recordingEntryNamePattern.matchEntire(entry.name)?.groups?.get(1)?.value
    }

    companion object {

        private const val PREFERENCES_ZIP_ENTRY = "preferences"
        private const val DATABASE_ZIP_ENTRY = "database"
        private const val METADATA_ZIP_ENTRY = "metadata"
        private const val RECORDINGS_DIR_ZIP_ENTRY = "audio_recordings/"
        private const val BACKUP_VERIFICATION_UUID = "9530d0d1-8023-4c3a-99d0-7cbb084020f1"

        private val recordingEntryNamePattern = Regex("^$RECORDINGS_DIR_ZIP_ENTRY(rec_\\d+(?:\\.\\w+)?)\$")
    }
}
