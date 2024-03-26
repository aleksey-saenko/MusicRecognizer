package com.mrsep.musicrecognizer.data.database.migration

import androidx.core.database.getStringOrNull
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseMigrationUtils {

    internal fun SupportSQLiteDatabase.isSQLiteVersionAtLeast(version: String): Boolean? {
        val thisVersion = querySQLiteVersion() ?: return null
        return runCatching { (compareSQLiteVersions(thisVersion, version) != -1) }.getOrNull()
    }

    private fun SupportSQLiteDatabase.querySQLiteVersion(): String? {
        return query("SELECT sqlite_version()").use {
            if (!it.moveToNext()) return null
            it.getStringOrNull(0)
        }
    }

    private fun compareSQLiteVersions(version1: String, version2: String): Int {
        val pattern = Regex("^\\d+(\\.\\d+)*\$")
        if (!pattern.matches(version1)) {
            throw IllegalArgumentException("Incorrect version format:$version1")
        }
        if (!pattern.matches(version2)) {
            throw IllegalArgumentException("Incorrect version format:$version2")
        }
        val partsThis = version1.split('.')
        val partsOther = version2.split('.')
        val maxLength = maxOf(partsThis.size, partsOther.size)
        for (i in 0 until maxLength) {
            val partThis = partsThis.getOrNull(i)?.toIntOrNull() ?: 0
            val partOther = partsOther.getOrNull(i)?.toIntOrNull() ?: 0

            when {
                partThis > partOther -> return 1
                partThis < partOther -> return -1
            }
        }
        return 0
    }
}

