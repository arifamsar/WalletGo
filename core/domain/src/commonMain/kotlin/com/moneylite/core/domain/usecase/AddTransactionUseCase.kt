package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.formatToRupiah
import com.moneylite.core.domain.repository.BudgetRepository
import com.moneylite.core.domain.repository.CategoryBudgetRepository
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.repository.NotificationSettingsRepository
import com.moneylite.core.domain.service.NotificationService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import kotlin.time.Clock

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository? = null,
    private val categoryBudgetRepository: CategoryBudgetRepository? = null,
    private val categoryRepository: CategoryRepository? = null,
    private val notificationService: NotificationService? = null,
    private val notificationSettingsRepository: NotificationSettingsRepository? = null
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        if (transaction.amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }
        return try {
            if (transaction.type == TransactionType.Expense) {
                val year = transaction.date.year
                val month = transaction.date.monthNumber

                val notificationsEnabled = notificationSettingsRepository?.isNotificationsEnabled() ?: true
                if (notificationsEnabled) {
                    val warningThreshold = notificationSettingsRepository?.getWarningThreshold()?.toDouble() ?: 0.8

                    // 1. Get transactions for this month/year before adding the new one
                    val currentTransactions = transactionRepository.getTransactionsByMonth(month, year).first()
                    val totalSpentBefore = currentTransactions
                        .filter { it.type == TransactionType.Expense }
                        .sumOf { it.amount }
                    val totalSpentAfter = totalSpentBefore + transaction.amount

                    // A. Overall Budget Check
                    if (budgetRepository != null && notificationService != null) {
                        val budget = budgetRepository.getBudget(month, year).first()
                        if (budget != null && budget.limitAmount > 0L) {
                            val limit = budget.limitAmount
                            if (totalSpentBefore < limit && totalSpentAfter >= limit) {
                                notificationService.showInstantNotification(
                                    title = "Budget Exceeded!",
                                    body = "You have spent ${totalSpentAfter.formatToRupiah()} of your ${limit.formatToRupiah()} monthly budget limit."
                                )
                            } else if (totalSpentBefore < limit * warningThreshold && totalSpentAfter >= limit * warningThreshold && totalSpentAfter < limit) {
                                notificationService.showInstantNotification(
                                    title = "Budget Warning!",
                                    body = "You have spent ${(warningThreshold * 100).toInt()}% of your ${limit.formatToRupiah()} monthly budget limit."
                                )
                            }
                        }
                    }

                    // B. Category Budget Check
                    if (categoryBudgetRepository != null && categoryRepository != null && notificationService != null) {
                        val categoryBudget = categoryBudgetRepository.getCategoryBudget(transaction.categoryId, month, year).first()
                        if (categoryBudget != null && categoryBudget.limitAmount > 0L) {
                            val catLimit = categoryBudget.limitAmount
                            val categorySpentBefore = currentTransactions
                                .filter { it.type == TransactionType.Expense && it.categoryId == transaction.categoryId }
                                .sumOf { it.amount }
                            val categorySpentAfter = categorySpentBefore + transaction.amount

                            val categories = categoryRepository.getCategories().first()
                            val categoryName = categories.find { it.id == transaction.categoryId }?.name ?: "Category"

                            if (categorySpentBefore < catLimit && categorySpentAfter >= catLimit) {
                                notificationService.showInstantNotification(
                                    title = "Category Budget Exceeded!",
                                    body = "$categoryName expenses have reached ${categorySpentAfter.formatToRupiah()} of your ${catLimit.formatToRupiah()} limit."
                                )
                            } else if (categorySpentBefore < catLimit * warningThreshold && categorySpentAfter >= catLimit * warningThreshold && categorySpentAfter < catLimit) {
                                notificationService.showInstantNotification(
                                    title = "Category Budget Warning!",
                                    body = "$categoryName expenses have reached ${(warningThreshold * 100).toInt()}% of your ${catLimit.formatToRupiah()} limit."
                                )
                            }
                        }
                    }
                }
            }

            // Reschedule/postpone the logging reminder to tomorrow if enabled
            if (notificationSettingsRepository != null && notificationService != null) {
                val dailyEnabled = notificationSettingsRepository.isDailyReminderEnabled()
                if (dailyEnabled) {
                    val hour = notificationSettingsRepository.getDailyReminderHour()
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val tomorrowDate = now.date.plus(1, DateTimeUnit.DAY)
                    val tomorrowTime = LocalDateTime(tomorrowDate, kotlinx.datetime.LocalTime(hour, 0))
                    val tomorrowTimeInstant = tomorrowTime.toInstant(TimeZone.currentSystemDefault())

                    notificationService.scheduleSingleNotification(
                        id = "daily_logging_reminder",
                        title = "MoneyLite Reminder",
                        body = "Don't forget to track your spending today to stay on top of your budget!",
                        scheduledTimeMillis = tomorrowTimeInstant.toEpochMilliseconds()
                    )
                }
            }

            transactionRepository.insertTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
