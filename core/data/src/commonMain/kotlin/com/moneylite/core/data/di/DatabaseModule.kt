package com.moneylite.core.data.di

import com.moneylite.core.data.local.database.MoneyLiteDatabase
import com.moneylite.core.data.local.database.createRoomDatabase
import com.moneylite.core.data.repository.BudgetRepositoryImpl
import com.moneylite.core.data.repository.CategoryRepositoryImpl
import com.moneylite.core.data.repository.TransactionRepositoryImpl
import com.moneylite.core.domain.repository.BudgetRepository
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.usecase.AddTransactionUseCase
import com.moneylite.core.domain.usecase.GetBudgetProgressUseCase
import com.moneylite.core.domain.usecase.GetMonthlySummaryUseCase
import com.moneylite.core.domain.usecase.GetTransactionsUseCase
import com.moneylite.core.domain.repository.NotificationHistoryRepository
import com.moneylite.core.data.repository.NotificationHistoryRepositoryImpl
import com.moneylite.core.domain.repository.NotificationSettingsRepository
import com.moneylite.core.data.repository.NotificationSettingsRepositoryImpl
import com.moneylite.core.domain.usecase.GetNotificationHistoryUseCase
import com.moneylite.core.domain.usecase.ClearNotificationHistoryUseCase
import org.koin.dsl.module

val databaseModule = module {
    // Database instance
    single { createRoomDatabase() }

    // DAOs
    single { get<MoneyLiteDatabase>().transactionDao() }
    single { get<MoneyLiteDatabase>().categoryDao() }
    single { get<MoneyLiteDatabase>().budgetDao() }
    single { get<MoneyLiteDatabase>().categoryBudgetDao() }
    single { get<MoneyLiteDatabase>().notificationDao() }

    // Repositories
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
    single<com.moneylite.core.domain.repository.CategoryBudgetRepository> { com.moneylite.core.data.repository.CategoryBudgetRepositoryImpl(get()) }
    single<NotificationHistoryRepository> { NotificationHistoryRepositoryImpl(get()) }
    single<NotificationSettingsRepository> { NotificationSettingsRepositoryImpl(get()) }

    // UseCases
    single { AddTransactionUseCase(get(), get(), get(), get(), get(), get()) }
    single { GetMonthlySummaryUseCase(get()) }
    single { GetTransactionsUseCase(get()) }
    single { GetBudgetProgressUseCase(get(), get()) }
    single { com.moneylite.core.domain.usecase.GetCategoryBudgetProgressUseCase(get(), get(), get()) }
    single { GetNotificationHistoryUseCase(get()) }
    single { ClearNotificationHistoryUseCase(get()) }
}

