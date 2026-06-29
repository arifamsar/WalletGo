package com.moneylite.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val categories: List<Category>,
    val transactions: List<Transaction>
)
