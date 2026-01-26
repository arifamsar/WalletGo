package com.example.template.core.data.di

import org.koin.dsl.module

val dataModule = module {
    includes(
        preferencesModule,
        httpModule
    )
}
