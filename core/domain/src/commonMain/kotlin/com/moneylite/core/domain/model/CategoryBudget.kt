package com.moneylite.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryBudget(
    val id: String,
    val categoryId: String,
    val month: Int, // 1-12
    val year: Int,
    val limitAmount: Long
)
