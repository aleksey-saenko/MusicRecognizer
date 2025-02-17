package com.mrsep.musicrecognizer.core.database

import java.io.InputStream

interface DatabaseBackupProvider {

    fun getDatabaseBackup(): InputStream?

    fun onDatabaseRestored()
}
