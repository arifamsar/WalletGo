package com.moneylite.core.data.local.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun createRoomDatabase(): MoneyLiteDatabase {
    val dbFile = documentDirectory() + "/moneylite.db"
    return Room.databaseBuilder<MoneyLiteDatabase>(
        name = dbFile,
        factory = { MoneyLiteDatabaseConstructor.initialize() }
    )
    .setDriver(BundledSQLiteDriver())
    .build()
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(documentDirectory?.path)
}
