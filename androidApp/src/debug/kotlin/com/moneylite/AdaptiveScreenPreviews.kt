package com.moneylite

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.SnackbarHostState
import com.moneylite.core.domain.model.Category
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.TransactionUiModel
import com.moneylite.core.domain.usecase.BudgetProgress
import com.moneylite.core.ui.adaptive.AdaptiveWindowClass
import com.moneylite.core.ui.theme.AppTheme
import com.moneylite.core.ui.theme.ThemeTemplate
import com.moneylite.home.DashboardState
import com.moneylite.home.HomeScreenContent
import com.moneylite.profile.ProfileScreenContent
import com.moneylite.profile.SettingsScreenContent
import com.moneylite.schedule.BudgetScreenContent
import com.moneylite.transactions.AddTransactionScreenContent
import com.moneylite.transactions.TransactionScreenContent
import com.moneylite.transactions.TransactionListState
import kotlinx.datetime.LocalDate

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showBackground = true)
annotation class FormFactorPreviews

@FormFactorPreviews
@Composable
private fun HomeAdaptivePreview() {
    AppTheme(isDark = false) {
        HomeScreenContent(
            uiState = DashboardState(
                isLoading = false,
                monthBalance = 7_500_000,
                monthIncome = 12_000_000,
                monthExpense = 4_500_000,
                budgetLimit = 8_000_000,
                budgetUsedPercent = 0.56f,
                recentTransactions = sampleTransactions
            ),
            windowClass = AdaptiveWindowClass.Expanded,
            onRefresh = {}
        )
    }
}

@FormFactorPreviews
@Composable
private fun BudgetAdaptivePreview() {
    AppTheme(isDark = false) {
        BudgetScreenContent(
            progressState = BudgetProgress(8_000_000, 4_500_000, 0.56f),
            categoryBudgets = listOf(
                com.moneylite.core.domain.usecase.CategoryBudgetProgress(
                    categoryId = "food",
                    categoryName = "Food",
                    categoryIcon = "restaurant",
                    categoryColor = "#F97316",
                    limitAmount = 2_000_000,
                    usedAmount = 1_200_000,
                    progress = 0.6f
                )
            ),
            categories = sampleCategories,
            windowClass = AdaptiveWindowClass.Expanded,
            showEditDialog = false,
            limitInput = "",
            onLimitInputChange = {},
            onShowEditDialogChange = {},
            onPrepareEdit = {},
            onSaveBudget = {},
            showCategoryDialog = false,
            selectedCategory = null,
            categoryLimitInput = "",
            onCategoryLimitInputChange = {},
            onSelectedCategoryChange = {},
            onShowCategoryDialogChange = {},
            onPrepareAddCategoryBudget = {},
            onPrepareEditCategoryBudget = {},
            onSaveCategoryBudget = { _, _ -> },
            onDeleteCategoryBudget = {},
            isEditingCategory = false,
            selectedMonth = 6,
            selectedYear = 2026,
            onPreviousMonth = {},
            onNextMonth = {}
        )
    }
}

@FormFactorPreviews
@Composable
private fun TransactionsAdaptivePreview() {
    AppTheme(isDark = false) {
        TransactionScreenContent(
            uiState = TransactionListState(
                isLoading = false,
                groupedTransactions = mapOf(sampleDate to sampleTransactions)
            ),
            windowClass = AdaptiveWindowClass.Expanded,
            snackbarHostState = SnackbarHostState(),
            onIntent = {}
        )
    }
}

@FormFactorPreviews
@Composable
private fun AddTransactionAdaptivePreview() {
    AppTheme(isDark = false) {
        AddTransactionScreenContent(
            transactionId = null,
            windowClass = AdaptiveWindowClass.Expanded,
            amount = "250000",
            type = TransactionType.Expense,
            selectedCategoryId = "food",
            date = sampleDate,
            note = "Lunch",
            categories = sampleCategories,
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onAmountChange = {},
            onTypeChange = {},
            onCategorySelect = {},
            onDateChange = {},
            onNoteChange = {},
            onSaveTransaction = {}
        )
    }
}

@Preview(name = "Profile Phone", device = Devices.PHONE, showBackground = true)
@Composable
private fun ProfileCompactPreview() {
    AppTheme(isDark = false) {
        ProfileScreenContent(
            userName = "User",
            userJob = "Freelancer",
            darkModeEnabled = false,
            showEditProfileDialog = false,
            windowClass = AdaptiveWindowClass.Compact,
            onDarkModeChange = {},
            onShowEditProfileDialogChange = {},
            onUpdateProfile = { _, _ -> }
        )
    }
}

@Preview(name = "Profile Tablet", device = Devices.TABLET, showBackground = true)
@Composable
private fun ProfileExpandedPreview() {
    AppTheme(isDark = false) {
        ProfileScreenContent(
            userName = "User",
            userJob = "Freelancer",
            darkModeEnabled = false,
            showEditProfileDialog = false,
            windowClass = AdaptiveWindowClass.Expanded,
            onDarkModeChange = {},
            onShowEditProfileDialogChange = {},
            onUpdateProfile = { _, _ -> }
        )
    }
}

@FormFactorPreviews
@Composable
private fun SettingsAdaptivePreview() {
    AppTheme(isDark = false) {
        SettingsScreenContent(
            isDark = false,
            themeTemplate = ThemeTemplate.Default,
            windowClass = AdaptiveWindowClass.Expanded,
            showClearDialog = false,
            onBack = {},
            onThemeSettings = {},
            onDarkModeChange = {},
            onShowClearDialogChange = {},
            onClearDatabase = {},
            onExportLedger = {},
            onImportLedger = {}
        )
    }
}

private val sampleDate = LocalDate(2026, 6, 3)

private val sampleTransactions = listOf(
    TransactionUiModel(
        id = "1",
        type = TransactionType.Expense,
        amount = 250_000,
        note = "Lunch",
        date = sampleDate,
        categoryId = "food",
        categoryName = "Food",
        categoryIcon = "restaurant",
        categoryColor = "#F97316"
    ),
    TransactionUiModel(
        id = "2",
        type = TransactionType.Income,
        amount = 12_000_000,
        note = "Salary",
        date = sampleDate,
        categoryId = "salary",
        categoryName = "Salary",
        categoryIcon = "money",
        categoryColor = "#16A34A"
    )
)

private val sampleCategories = listOf(
    Category("food", "Food", "restaurant", "#F97316", TransactionType.Expense),
    Category("transport", "Transport", "car", "#2563EB", TransactionType.Expense),
    Category("salary", "Salary", "money", "#16A34A", TransactionType.Income)
)
