package com.moneylite.core.domain.usecase

import com.moneylite.core.domain.model.Budget
import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.CategoryBudget
import com.moneylite.core.domain.model.Transaction
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.repository.BudgetRepository
import com.moneylite.core.domain.repository.CategoryBudgetRepository
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.repository.NotificationSettingsRepository
import com.moneylite.core.domain.service.NotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FakeTransactionRepository : TransactionRepository {
    val insertedTransactions = mutableListOf<Transaction>()

    override fun getTransactions(): Flow<List<Transaction>> = TODO()
    override fun getTransactionsByMonth(month: Int, year: Int): Flow<List<Transaction>> =
        flowOf(insertedTransactions.filter { it.date.monthNumber == month && it.date.year == year })
    override suspend fun getTransactionById(id: String): Transaction? = TODO()

    override suspend fun insertTransaction(transaction: Transaction) {
        insertedTransactions.add(transaction)
    }

    override suspend fun deleteTransaction(id: String) = TODO()
    override suspend fun deleteAllTransactions() = TODO()
}

class FakeBudgetRepository : BudgetRepository {
    var budget: Budget? = null
    override fun getBudget(month: Int, year: Int): Flow<Budget?> = flowOf(budget)
    override suspend fun insertBudget(budget: Budget) { this.budget = budget }
    override suspend fun setBudgetLimit(month: Int, year: Int, limitAmount: Long) {
        this.budget = Budget("1", month, year, limitAmount)
    }
}

class FakeCategoryBudgetRepository : CategoryBudgetRepository {
    var categoryBudget: CategoryBudget? = null
    override fun getCategoryBudgets(month: Int, year: Int): Flow<List<CategoryBudget>> = TODO()
    override fun getCategoryBudget(categoryId: String, month: Int, year: Int): Flow<CategoryBudget?> =
        flowOf(if (categoryBudget?.categoryId == categoryId) categoryBudget else null)
    override suspend fun setCategoryBudgetLimit(categoryId: String, month: Int, year: Int, limitAmount: Long) {
        categoryBudget = CategoryBudget("cb1", categoryId, month, year, limitAmount)
    }
    override suspend fun deleteCategoryBudget(id: String) { categoryBudget = null }
}

class FakeCategoryRepository : CategoryRepository {
    var categories = listOf<Category>()
    override fun getCategories(): Flow<List<Category>> = flowOf(categories)
    override suspend fun getCategoryById(id: String): Category? = categories.find { it.id == id }
    override suspend fun insertCategory(category: Category) { categories = categories + category }
    override suspend fun deleteCategory(id: String) { categories = categories.filter { it.id != id } }
    override suspend fun seedDefaultCategories() {}
}

class FakeNotificationService : NotificationService {
    val sentNotifications = mutableListOf<Pair<String, String>>()
    val scheduledNotifications = mutableListOf<ScheduledNotification>()

    data class ScheduledNotification(
        val id: String,
        val title: String,
        val body: String,
        val scheduledTimeMillis: Long
    )

    override fun showInstantNotification(title: String, body: String) {
        sentNotifications.add(Pair(title, body))
    }

    override fun scheduleSingleNotification(id: String, title: String, body: String, scheduledTimeMillis: Long) {
        scheduledNotifications.add(ScheduledNotification(id, title, body, scheduledTimeMillis))
    }

    override fun scheduleRepeatingNotification(
        id: String,
        title: String,
        body: String,
        dayOfWeek: Int?,
        hour: Int,
        minute: Int,
        interval: com.moneylite.core.domain.model.RepeatIntervalType
    ) {}

    override fun cancelNotification(id: String) {
        scheduledNotifications.removeAll { it.id == id }
    }
}

class FakeNotificationSettingsRepository : NotificationSettingsRepository {
    var notificationsEnabled = true
    var warningThreshold = 0.8f
    var dailyReminderEnabled = true
    var dailyReminderHour = 20
    var scheduledAlertsEnabled = true
    var weeklyReportEnabled = true

    override fun isNotificationsEnabledFlow(): Flow<Boolean> = flowOf(notificationsEnabled)
    override suspend fun isNotificationsEnabled(): Boolean = notificationsEnabled
    override suspend fun setNotificationsEnabled(enabled: Boolean) { notificationsEnabled = enabled }

    override fun getWarningThresholdFlow(): Flow<Float> = flowOf(warningThreshold)
    override suspend fun getWarningThreshold(): Float = warningThreshold
    override suspend fun setWarningThreshold(threshold: Float) { warningThreshold = threshold }

    override fun isDailyReminderEnabledFlow(): Flow<Boolean> = flowOf(dailyReminderEnabled)
    override suspend fun isDailyReminderEnabled(): Boolean = dailyReminderEnabled
    override suspend fun setDailyReminderEnabled(enabled: Boolean) { dailyReminderEnabled = enabled }

    override fun getDailyReminderHourFlow(): Flow<Int> = flowOf(dailyReminderHour)
    override suspend fun getDailyReminderHour(): Int = dailyReminderHour
    override suspend fun setDailyReminderHour(hour: Int) { dailyReminderHour = hour }

    override fun isScheduledAlertsEnabledFlow(): Flow<Boolean> = flowOf(scheduledAlertsEnabled)
    override suspend fun isScheduledAlertsEnabled(): Boolean = scheduledAlertsEnabled
    override suspend fun setScheduledAlertsEnabled(enabled: Boolean) { scheduledAlertsEnabled = enabled }

    override fun isWeeklyReportEnabledFlow(): Flow<Boolean> = flowOf(weeklyReportEnabled)
    override suspend fun isWeeklyReportEnabled(): Boolean = weeklyReportEnabled
    override suspend fun setWeeklyReportEnabled(enabled: Boolean) { weeklyReportEnabled = enabled }
}

class AddTransactionUseCaseTest {

    private val repository = FakeTransactionRepository()
    private val useCase = AddTransactionUseCase(repository)

    @Test
    fun testInsert_negativeAmount_returnsFailure() = runTest {
        val tx = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = -100L,
            categoryId = "food",
            note = "Negative test",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        val result = useCase(tx)
        assertTrue(result.isFailure)
        assertTrue(repository.insertedTransactions.isEmpty())
    }

    @Test
    fun testInsert_zeroAmount_returnsFailure() = runTest {
        val tx = Transaction(
            id = "2",
            type = TransactionType.Expense,
            amount = 0L,
            categoryId = "food",
            note = "Zero test",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        val result = useCase(tx)
        assertTrue(result.isFailure)
        assertTrue(repository.insertedTransactions.isEmpty())
    }

    @Test
    fun testInsert_positiveAmount_returnsSuccessAndInserts() = runTest {
        val tx = Transaction(
            id = "3",
            type = TransactionType.Expense,
            amount = 15000L,
            categoryId = "food",
            note = "Bakso Kupang",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        val result = useCase(tx)
        assertTrue(result.isSuccess)
        assertFalse(repository.insertedTransactions.isEmpty())
        assertTrue(repository.insertedTransactions.contains(tx))
    }

    @Test
    fun testInsert_budgetWarningNotification() = runTest {
        val txRepository = FakeTransactionRepository()
        val budgetRepository = FakeBudgetRepository()
        val categoryBudgetRepository = FakeCategoryBudgetRepository()
        val categoryRepository = FakeCategoryRepository()
        val notificationService = FakeNotificationService()

        val checkUseCase = AddTransactionUseCase(
            transactionRepository = txRepository,
            budgetRepository = budgetRepository,
            categoryBudgetRepository = categoryBudgetRepository,
            categoryRepository = categoryRepository,
            notificationService = notificationService
        )

        budgetRepository.setBudgetLimit(6, 2026, 100000L)

        val tx1 = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = 70000L,
            categoryId = "food",
            note = "Bakso Kupang",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx1)
        assertTrue(notificationService.sentNotifications.isEmpty())

        val tx2 = Transaction(
            id = "2",
            type = TransactionType.Expense,
            amount = 15000L,
            categoryId = "food",
            note = "Es Teh",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx2)
        assertFalse(notificationService.sentNotifications.isEmpty())
        val lastNotif = notificationService.sentNotifications.last()
        assertTrue(lastNotif.first.contains("Warning"))
    }

    @Test
    fun testInsert_budgetExceededNotification() = runTest {
        val txRepository = FakeTransactionRepository()
        val budgetRepository = FakeBudgetRepository()
        val categoryBudgetRepository = FakeCategoryBudgetRepository()
        val categoryRepository = FakeCategoryRepository()
        val notificationService = FakeNotificationService()

        val checkUseCase = AddTransactionUseCase(
            transactionRepository = txRepository,
            budgetRepository = budgetRepository,
            categoryBudgetRepository = categoryBudgetRepository,
            categoryRepository = categoryRepository,
            notificationService = notificationService
        )

        budgetRepository.setBudgetLimit(6, 2026, 100000L)

        val tx1 = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = 90000L,
            categoryId = "food",
            note = "Steak",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx1)
        // Warning should fire since 90k >= 80k
        assertFalse(notificationService.sentNotifications.isEmpty())
        assertTrue(notificationService.sentNotifications.last().first.contains("Warning"))

        val tx2 = Transaction(
            id = "2",
            type = TransactionType.Expense,
            amount = 15000L,
            categoryId = "food",
            note = "Drink",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx2)
        // Exceeded should fire since 105k >= 100k
        assertTrue(notificationService.sentNotifications.last().first.contains("Exceeded"))
    }

    @Test
    fun testInsert_categoryBudgetWarningAndExceededNotification() = runTest {
        val txRepository = FakeTransactionRepository()
        val budgetRepository = FakeBudgetRepository()
        val categoryBudgetRepository = FakeCategoryBudgetRepository()
        val categoryRepository = FakeCategoryRepository()
        val notificationService = FakeNotificationService()

        val checkUseCase = AddTransactionUseCase(
            transactionRepository = txRepository,
            budgetRepository = budgetRepository,
            categoryBudgetRepository = categoryBudgetRepository,
            categoryRepository = categoryRepository,
            notificationService = notificationService
        )

        val category = Category("food", "Food & Beverage", "eat", "#FF0000", TransactionType.Expense)
        categoryRepository.insertCategory(category)
        categoryBudgetRepository.setCategoryBudgetLimit("food", 6, 2026, 50000L)

        val tx1 = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = 41000L,
            categoryId = "food",
            note = "Dinner",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx1)
        // Warning should fire since 41k >= 40k (80% of 50k)
        assertFalse(notificationService.sentNotifications.isEmpty())
        assertTrue(notificationService.sentNotifications.last().first.contains("Category Budget Warning"))

        val tx2 = Transaction(
            id = "2",
            type = TransactionType.Expense,
            amount = 10000L,
            categoryId = "food",
            note = "Coffee",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx2)
        // Exceeded should fire since 51k >= 50k
        assertTrue(notificationService.sentNotifications.last().first.contains("Category Budget Exceeded"))
    }

    @Test
    fun testInsert_notificationsDisabled_suppressesNotification() = runTest {
        val txRepository = FakeTransactionRepository()
        val budgetRepository = FakeBudgetRepository()
        val categoryBudgetRepository = FakeCategoryBudgetRepository()
        val categoryRepository = FakeCategoryRepository()
        val notificationService = FakeNotificationService()
        val settingsRepository = FakeNotificationSettingsRepository()

        settingsRepository.notificationsEnabled = false

        val checkUseCase = AddTransactionUseCase(
            transactionRepository = txRepository,
            budgetRepository = budgetRepository,
            categoryBudgetRepository = categoryBudgetRepository,
            categoryRepository = categoryRepository,
            notificationService = notificationService,
            notificationSettingsRepository = settingsRepository
        )

        budgetRepository.setBudgetLimit(6, 2026, 100000L)

        val tx1 = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = 90000L,
            categoryId = "food",
            note = "Dinner",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx1)
        assertTrue(notificationService.sentNotifications.isEmpty())
    }

    @Test
    fun testInsert_customWarningThreshold_triggersWarning() = runTest {
        val txRepository = FakeTransactionRepository()
        val budgetRepository = FakeBudgetRepository()
        val categoryBudgetRepository = FakeCategoryBudgetRepository()
        val categoryRepository = FakeCategoryRepository()
        val notificationService = FakeNotificationService()
        val settingsRepository = FakeNotificationSettingsRepository()

        settingsRepository.warningThreshold = 0.5f

        val checkUseCase = AddTransactionUseCase(
            transactionRepository = txRepository,
            budgetRepository = budgetRepository,
            categoryBudgetRepository = categoryBudgetRepository,
            categoryRepository = categoryRepository,
            notificationService = notificationService,
            notificationSettingsRepository = settingsRepository
        )

        budgetRepository.setBudgetLimit(6, 2026, 100000L)

        // Threshold is 50%, so 51k should trigger warning
        val tx1 = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = 51000L,
            categoryId = "food",
            note = "Dinner",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx1)
        assertFalse(notificationService.sentNotifications.isEmpty())
        assertTrue(notificationService.sentNotifications.last().first.contains("Warning"))
    }

    @Test
    fun testInsert_dailyReminderEnabled_schedulesReminderForTomorrow() = runTest {
        val txRepository = FakeTransactionRepository()
        val budgetRepository = FakeBudgetRepository()
        val categoryBudgetRepository = FakeCategoryBudgetRepository()
        val categoryRepository = FakeCategoryRepository()
        val notificationService = FakeNotificationService()
        val settingsRepository = FakeNotificationSettingsRepository()

        settingsRepository.notificationsEnabled = true
        settingsRepository.setDailyReminderEnabled(true)
        settingsRepository.setDailyReminderHour(20)

        val checkUseCase = AddTransactionUseCase(
            transactionRepository = txRepository,
            budgetRepository = budgetRepository,
            categoryBudgetRepository = categoryBudgetRepository,
            categoryRepository = categoryRepository,
            notificationService = notificationService,
            notificationSettingsRepository = settingsRepository
        )

        val tx1 = Transaction(
            id = "1",
            type = TransactionType.Expense,
            amount = 1000L,
            categoryId = "food",
            note = "Snack",
            date = LocalDate(2026, 6, 3),
            createdAt = Instant.fromEpochMilliseconds(0L)
        )
        checkUseCase(tx1)

        assertFalse(notificationService.scheduledNotifications.isEmpty())
        val scheduled = notificationService.scheduledNotifications.first()
        assertTrue(scheduled.id == "daily_logging_reminder")
        assertTrue(scheduled.title == "MoneyLite Reminder")
    }
}


