package com.moneylite.core.domain.model

import kotlin.time.Instant

data class NotificationHistoryItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Instant
)
