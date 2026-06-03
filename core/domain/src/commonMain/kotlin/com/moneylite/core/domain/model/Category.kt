package com.moneylite.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String, // Represented as icon name
    val colorKey: String, // Key/Hex code for color
    val type: TransactionType,
    val isDefault: Boolean = false
)
