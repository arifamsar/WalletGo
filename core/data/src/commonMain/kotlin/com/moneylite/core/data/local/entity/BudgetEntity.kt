package com.moneylite.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String, // format: "yyyy-MM"
    val month: Int,
    val year: Int,
    val limitAmount: Long
)
