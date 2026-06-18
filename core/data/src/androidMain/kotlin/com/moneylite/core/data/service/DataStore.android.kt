package com.moneylite.core.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal lateinit var applicationContext: Context

fun initPreferencesDataStore(context: Context) {
    applicationContext = context
}

fun getPreferencesDataStorePath(context: Context): String {
    return context.filesDir.resolve(dataStoreFileName).absolutePath
}

actual fun createPreferencesDataStore(): DataStore<Preferences> {
    val path = getPreferencesDataStorePath(applicationContext)
    return getDataStore { path }
}
