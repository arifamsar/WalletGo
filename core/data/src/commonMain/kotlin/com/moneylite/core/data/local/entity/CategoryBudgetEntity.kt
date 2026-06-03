package com.moneylite.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId", "month", "year"], unique = true)]
)
data class CategoryBudgetEntity(
    @PrimaryKey val id: String, // format: "categoryId-yyyy-MM"
    val categoryId: String,
    val month: Int,
    val year: Int,
    val limitAmount: Long
)
