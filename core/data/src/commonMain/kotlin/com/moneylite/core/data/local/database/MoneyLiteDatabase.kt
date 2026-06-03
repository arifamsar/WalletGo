package com.moneylite.core.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.moneylite.core.data.local.dao.BudgetDao
import com.moneylite.core.data.local.dao.CategoryDao
import com.moneylite.core.data.local.dao.TransactionDao
import com.moneylite.core.data.local.entity.BudgetEntity
import com.moneylite.core.data.local.entity.CategoryEntity
import com.moneylite.core.data.local.entity.TransactionEntity

import androidx.room.AutoMigration
import com.moneylite.core.data.local.dao.CategoryBudgetDao
import com.moneylite.core.data.local.entity.CategoryBudgetEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        CategoryBudgetEntity::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(MoneyLiteDatabaseConstructor::class)
abstract class MoneyLiteDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MoneyLiteDatabaseConstructor : RoomDatabaseConstructor<MoneyLiteDatabase> {
    override fun initialize(): MoneyLiteDatabase
}
