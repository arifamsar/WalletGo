package com.moneylite.core.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

private lateinit var dbContext: Context

fun initRoomDatabase(context: Context) {
    dbContext = context
}

actual fun createRoomDatabase(): MoneyLiteDatabase {
    val dbFile = dbContext.getDatabasePath("moneylite.db")
    return Room.databaseBuilder<MoneyLiteDatabase>(
        context = dbContext,
        name = dbFile.absolutePath
    )
    .setDriver(BundledSQLiteDriver())
    .build()
}
