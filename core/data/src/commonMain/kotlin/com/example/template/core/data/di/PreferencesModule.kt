package com.example.template.core.data.di

import com.example.template.core.data.service.UserPreferences
import com.example.template.core.data.service.createPreferencesDataStore
import org.koin.dsl.module

val preferencesModule = module {
    single { UserPreferences(createPreferencesDataStore()) }
}
