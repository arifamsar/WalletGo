package com.moneylite.core.data.service

import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration =
    AlarmeeIosPlatformConfiguration
