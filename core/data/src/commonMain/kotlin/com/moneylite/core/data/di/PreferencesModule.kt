package com.moneylite.core.data.di

import com.moneylite.core.data.service.UserPreferences
import com.moneylite.core.data.service.createPreferencesDataStore
import org.koin.dsl.module

val preferencesModule = module {
    single { UserPreferences(createPreferencesDataStore()) }
}
