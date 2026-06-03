package com.moneylite.core.data.mapper

import com.moneylite.core.data.local.entity.BudgetEntity
import com.moneylite.core.data.local.entity.CategoryBudgetEntity
import com.moneylite.core.data.local.entity.CategoryEntity
import com.moneylite.core.data.local.entity.TransactionEntity
import com.moneylite.core.domain.model.Budget
import com.moneylite.core.domain.model.CategoryBudget
import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    type = TransactionType.valueOf(type),
    amount = amount,
    categoryId = categoryId,
    note = note,
    date = date,
    createdAt = createdAt
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    type = type.name,
    amount = amount,
    categoryId = categoryId,
    note = note,
    date = date,
    createdAt = createdAt,
    updatedAt = createdAt
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    icon = icon,
    colorKey = colorKey,
    type = TransactionType.valueOf(type),
    isDefault = isDefault
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    colorKey = colorKey,
    type = type.name,
    isDefault = isDefault
)

fun BudgetEntity.toDomain() = Budget(
    id = id,
    month = month,
    year = year,
    limitAmount = limitAmount
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    month = month,
    year = year,
    limitAmount = limitAmount
)

fun CategoryBudgetEntity.toDomain() = CategoryBudget(
    id = id,
    categoryId = categoryId,
    month = month,
    year = year,
    limitAmount = limitAmount
)

fun CategoryBudget.toEntity() = CategoryBudgetEntity(
    id = id,
    categoryId = categoryId,
    month = month,
    year = year,
    limitAmount = limitAmount
)

