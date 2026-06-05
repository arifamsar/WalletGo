package com.moneylite.core.data.di

import com.tweener.alarmee.createAlarmeeService
import org.koin.dsl.module
import com.moneylite.core.data.service.createAlarmeePlatformConfiguration
import com.moneylite.core.data.service.NotificationServiceImpl
import com.moneylite.core.domain.service.NotificationService

val alarmeeModule = module {
    single {
        createAlarmeeService().apply {
            initialize(platformConfiguration = createAlarmeePlatformConfiguration())
        }
    }
    single<NotificationService> { NotificationServiceImpl(get(), get()) }
}

