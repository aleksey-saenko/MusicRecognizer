package com.mrsep.musicrecognizer.core.database.migration

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.f4b6a3.uuid.UuidCreator
import com.mrsep.musicrecognizer.core.audio.audiorecord.encoder.AdtsToMp4Migration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import kotlin.io.path.ExperimentalPathApi

private data class RecognitionFile(
    val recognitionId: Int,
    val originalFile: File,
    val migrationFile: File,
)

// Migrate samples from ADTS to MP4 container, use UUIDs for new filenames
internal class Migration9To10(private val appContext: Context): Migration(9, 10) {

    @OptIn(ExperimentalPathApi::class)
    override fun migrate(db: SupportSQLiteDatabase) {
        val oldSamplesDir = appContext.filesDir.resolve("enqueued_records")
        val newSamplesDir = appContext.filesDir.resolve("audio_samples")
        newSamplesDir.mkdirs()

        val needMigration = mutableListOf<RecognitionFile>()
        val migrated = mutableListOf<RecognitionFile>()
        val corrupted = mutableListOf<RecognitionFile>()

        val cursor = db.query("SELECT id, record_file FROM enqueued_recognition")
        cursor.use {
            val idIndex = cursor.getColumnIndex("id")
            val fileIndex = cursor.getColumnIndex("record_file")
            while (cursor.moveToNext()) {
                val id = cursor.getInt(idIndex)
                val originalFile = cursor.getString(fileIndex).run(::File)
                val uniqueId = UuidCreator.getNameBasedSha1(originalFile.name)
                val migrationFile = newSamplesDir.resolve("$uniqueId.m4a")
                when {
                    originalFile.exists() -> needMigration += RecognitionFile(id, originalFile, migrationFile)
                    migrationFile.exists() -> migrated += RecognitionFile(id, migrationFile, migrationFile)
                    else -> corrupted += RecognitionFile(id, originalFile, migrationFile)
                }
            }
        }

        val semaphore = Semaphore(Runtime.getRuntime().availableProcessors())
        val adtsToMp4Migration = AdtsToMp4Migration(appContext)
        runBlocking {
            val conversions = needMigration.map { (id, adtsSampleFile, migrationFile) ->
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
                        val adtsFileName = adtsSampleFile.name
                        val newSampleTempFile = oldSamplesDir.resolve("${adtsFileName}_temp.m4a").apply {
                            delete()
                            createNewFile()
                        }
                        // Original filename 'rec_{EpochMillis}'
                        val timestamp = Instant.ofEpochMilli(adtsFileName.drop(4).toLong())
                        adtsToMp4Migration.convert(
                            input = adtsSampleFile,
                            output = newSampleTempFile,
                            creationTimestamp = timestamp,
                        )
                        try {
                            Files.move(
                                newSampleTempFile.toPath(),
                                migrationFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE
                            )
                        } catch (_: AtomicMoveNotSupportedException) {
                            migrationFile.delete()
                            check(newSampleTempFile.renameTo(migrationFile))
                        }
                        migrated += RecognitionFile(id, adtsSampleFile, migrationFile)
                        adtsSampleFile.delete()
                    }
                }
            }
            conversions.joinAll()
        }
        oldSamplesDir.deleteRecursively()

        for ((id, _, migrationFile) in migrated) {
            db.execSQL("UPDATE enqueued_recognition SET record_file = '${migrationFile.absolutePath}' WHERE id = $id")
        }
        for ((id, _) in corrupted) {
            db.execSQL("DELETE FROM enqueued_recognition WHERE id = $id")
        }
    }
}
