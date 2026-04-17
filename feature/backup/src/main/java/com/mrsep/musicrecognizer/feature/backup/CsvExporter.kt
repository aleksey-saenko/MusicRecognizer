package com.mrsep.musicrecognizer.feature.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.core.domain.preferences.FavoritesMode
import com.mrsep.musicrecognizer.core.domain.track.TrackRepository
import com.mrsep.musicrecognizer.feature.backup.CsvField.Companion.extractFrom
import com.mrsep.musicrecognizer.feature.backup.presentation.header
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

internal interface CsvExporter {

    suspend fun export(destination: Uri, params: CsvExportParams) : ExportResult
}

internal data class CsvExportParams(
    val exportFields: List<CsvField>,
    val writeHeader: Boolean,
    val favoritesMode: FavoritesMode,
)

internal class CsvExporterImpl @Inject constructor(
    private val trackRepository: TrackRepository,
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CsvExporter {

    private val csvWriter = csvWriter()

    override suspend fun export(destination: Uri, params: CsvExportParams) = withContext(ioDispatcher) {
        try {
            val outputStream = try {
                requireNotNull(appContext.contentResolver.openOutputStream(destination))
            } catch (_: Exception) {
                return@withContext ExportResult.FileNotFound
            }
            val tracks = trackRepository.getTracksFlow(params.favoritesMode).first()
            val csvRows = tracks.mapNotNull { track ->
                val row = params.exportFields.extractFrom(track)
                row.takeIf { it.any { value -> !value.isNullOrBlank() } }
            }
            csvWriter.openAsync(outputStream) {
                if (params.writeHeader) {
                    writeRow(params.exportFields.map { it.header(appContext) })
                }
                writeRows(csvRows)
            }
            ExportResult.Success(csvRows.size)
        } catch (e: CancellationException) {
            deleteUnfinishedExportFile(destination)
            throw e
        } catch (e: Exception) {
            val baseMsg = "Fatal error while creating CSV file"
            Log.e(this::class.simpleName, baseMsg, e)
            deleteUnfinishedExportFile(destination)
            ExportResult.UnhandledError(e.message ?: baseMsg)
        }
    }

    private fun deleteUnfinishedExportFile(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(appContext.contentResolver, uri)
        } catch (e: FileNotFoundException) {
            Log.e(this::class.simpleName, "Failed to delete unfinished CSV file", e)
        }
    }
}

internal sealed class ExportResult {
    data class Success(val trackCount: Int) : ExportResult()
    data object FileNotFound : ExportResult()
    data class UnhandledError(val message: String) : ExportResult()
}
