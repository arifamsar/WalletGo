package com.moneylite.core.data.service

import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import android.app.NotificationManager

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration =
    AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = android.R.drawable.ic_dialog_info,
        notificationChannels = listOf(
            AlarmeeNotificationChannel(
                id = "budget_alerts",
                name = "Budget Alerts",
                importance = NotificationManager.IMPORTANCE_HIGH
            )
        )
    )
