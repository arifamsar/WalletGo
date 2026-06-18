package com.moneylite.core.data.service

import com.moneylite.core.domain.model.NotificationHistoryItem
import com.moneylite.core.domain.repository.NotificationHistoryRepository
import com.moneylite.core.domain.service.NotificationService
import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Clock
import com.tweener.alarmee.model.RepeatInterval
import com.moneylite.core.domain.model.RepeatIntervalType
import kotlin.time.Instant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit

class NotificationServiceImpl(
    private val alarmeeService: AlarmeeService,
    private val notificationHistoryRepository: NotificationHistoryRepository
) : NotificationService {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun showInstantNotification(title: String, body: String) {
        val uuid = Random.nextLong().toString()
        alarmeeService.local.immediate(
            Alarmee(
                uuid = uuid,
                notificationTitle = title,
                notificationBody = body,
                androidNotificationConfiguration = AndroidNotificationConfiguration(
                    priority = AndroidNotificationPriority.HIGH,
                    channelId = "budget_alerts"
                ),
                iosNotificationConfiguration = IosNotificationConfiguration()
            )
        )
        coroutineScope.launch {
            try {
                notificationHistoryRepository.saveNotification(
                    NotificationHistoryItem(
                        id = uuid,
                        title = title,
                        body = body,
                        timestamp = Clock.System.now()
                    )
                )
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    override fun scheduleSingleNotification(id: String, title: String, body: String, scheduledTimeMillis: Long) {
        val triggerTime = Instant.fromEpochMilliseconds(scheduledTimeMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())

        alarmeeService.local.schedule(
            Alarmee(
                uuid = id,
                notificationTitle = title,
                notificationBody = body,
                scheduledDateTime = triggerTime,
                androidNotificationConfiguration = AndroidNotificationConfiguration(
                    priority = AndroidNotificationPriority.HIGH,
                    channelId = "budget_alerts"
                ),
                iosNotificationConfiguration = IosNotificationConfiguration()
            )
        )
    }

    override fun scheduleRepeatingNotification(
        id: String,
        title: String,
        body: String,
        dayOfWeek: Int?,
        hour: Int,
        minute: Int,
        interval: RepeatIntervalType
    ) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var scheduledDate = now.date

        if (interval == RepeatIntervalType.Weekly && dayOfWeek != null) {
            val targetDay = kotlinx.datetime.DayOfWeek(dayOfWeek)
            while (scheduledDate.dayOfWeek != targetDay) {
                scheduledDate = scheduledDate.plus(1, DateTimeUnit.DAY)
            }
        }

        var scheduledTime = LocalDateTime(scheduledDate, kotlinx.datetime.LocalTime(hour, minute))
        if (scheduledTime < now) {
            scheduledTime = if (interval == RepeatIntervalType.Weekly) {
                val nextDate = scheduledDate.plus(7, DateTimeUnit.DAY)
                LocalDateTime(nextDate, kotlinx.datetime.LocalTime(hour, minute))
            } else {
                val nextDate = scheduledDate.plus(1, DateTimeUnit.DAY)
                LocalDateTime(nextDate, kotlinx.datetime.LocalTime(hour, minute))
            }
        }

        val repeat = when (interval) {
            RepeatIntervalType.Daily -> RepeatInterval.Daily
            RepeatIntervalType.Weekly -> RepeatInterval.Weekly
        }

        alarmeeService.local.schedule(
            Alarmee(
                uuid = id,
                notificationTitle = title,
                notificationBody = body,
                scheduledDateTime = scheduledTime,
                androidNotificationConfiguration = AndroidNotificationConfiguration(
                    priority = AndroidNotificationPriority.HIGH,
                    channelId = "budget_alerts"
                ),
                iosNotificationConfiguration = IosNotificationConfiguration(),
                repeatInterval = repeat
            )
        )
    }

    override fun cancelNotification(id: String) {
        alarmeeService.local.cancel(uuid = id)
    }
}


