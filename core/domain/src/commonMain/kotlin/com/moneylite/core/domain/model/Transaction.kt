package com.moneylite.core.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    Income, Expense
}

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Long,
    val categoryId: String,
    val note: String?,
    val date: LocalDate,
    val createdAt: Instant
)
