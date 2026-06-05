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
import com.moneylite.core.data.local.dao.NotificationDao
import com.moneylite.core.data.local.entity.NotificationEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        CategoryBudgetEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(MoneyLiteDatabaseConstructor::class)
abstract class MoneyLiteDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun notificationDao(): NotificationDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object MoneyLiteDatabaseConstructor : RoomDatabaseConstructor<MoneyLiteDatabase> {
    override fun initialize(): MoneyLiteDatabase
}
