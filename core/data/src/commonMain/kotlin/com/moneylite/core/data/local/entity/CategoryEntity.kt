package com.moneylite.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val colorKey: String,
    val type: String, // "Income" or "Expense"
    val isDefault: Boolean
)
