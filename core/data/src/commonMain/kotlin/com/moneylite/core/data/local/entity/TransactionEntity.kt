package com.moneylite.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String, // "Income" or "Expense"
    val amount: Long,
    val categoryId: String,
    val note: String?,
    val date: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant
)
