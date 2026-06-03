package com.moneylite.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class TransactionUiModel(
    val id: String,
    val type: TransactionType,
    val amount: Long,
    val note: String?,
    val date: LocalDate,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String
)
