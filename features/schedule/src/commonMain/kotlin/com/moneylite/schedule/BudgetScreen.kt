@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)
package com.moneylite.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.moneylite.core.domain.model.formatToRupiah
import com.moneylite.core.domain.model.rupiahDigits
import com.moneylite.core.domain.usecase.BudgetProgress
import com.moneylite.core.domain.usecase.CategoryBudgetProgress
import com.moneylite.core.domain.model.Category
import com.moneylite.core.ui.adaptive.AdaptiveWindowBox
import com.moneylite.core.ui.adaptive.AdaptiveWindowClass
import com.moneylite.core.ui.adaptive.isExpanded
import com.moneylite.core.ui.components.RupiahAmountVisualTransformation
import com.moneylite.core.ui.utils.getCategoryIcon
import com.moneylite.core.ui.utils.toColor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: BudgetViewModel = koinViewModel()
    val progressState by viewModel.budgetProgress.collectAsState()
    val categoryBudgets by viewModel.categoryBudgets.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var limitInput by remember { mutableStateOf("") }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryLimitInput by remember { mutableStateOf("") }
    var isEditingCategory by remember { mutableStateOf(false) }

    AdaptiveWindowBox(modifier = modifier) { windowClass ->
        BudgetScreenContent(
            progressState = progressState,
            categoryBudgets = categoryBudgets,
            categories = categories,
            windowClass = windowClass,
            showEditDialog = showEditDialog,
            limitInput = limitInput,
            onLimitInputChange = { limitInput = it },
            onShowEditDialogChange = { showEditDialog = it },
            onPrepareEdit = {
                limitInput = if (progressState.limitAmount == 0L) "" else progressState.limitAmount.toString()
                showEditDialog = true
            },
            onSaveBudget = { limit ->
                viewModel.updateBudgetLimit(limit)
                showEditDialog = false
            },
            showCategoryDialog = showCategoryDialog,
            selectedCategory = selectedCategory,
            categoryLimitInput = categoryLimitInput,
            onCategoryLimitInputChange = { categoryLimitInput = it },
            onSelectedCategoryChange = { selectedCategory = it },
            onShowCategoryDialogChange = { showCategoryDialog = it },
            onPrepareAddCategoryBudget = {
                // Filter categories to only those that don't have budgets yet, and are Expense type
                val customExpenseCats = categories.filter { it.type == com.moneylite.core.domain.model.TransactionType.Expense }
                val remainingCats = customExpenseCats.filter { cat -> categoryBudgets.none { it.categoryId == cat.id } }
                selectedCategory = remainingCats.firstOrNull()
                categoryLimitInput = ""
                isEditingCategory = false
                showCategoryDialog = true
            },
            onPrepareEditCategoryBudget = { cb ->
                selectedCategory = categories.find { it.id == cb.categoryId }
                categoryLimitInput = if (cb.limitAmount == 0L) "" else cb.limitAmount.toString()
                isEditingCategory = true
                showCategoryDialog = true
            },
            onSaveCategoryBudget = { catId, limit ->
                viewModel.updateCategoryBudgetLimit(catId, limit)
                showCategoryDialog = false
            },
            onDeleteCategoryBudget = { catId ->
                viewModel.deleteCategoryBudget(catId)
            },
            isEditingCategory = isEditingCategory
        )
    }
}

@Composable
fun BudgetScreenContent(
    progressState: BudgetProgress,
    categoryBudgets: List<CategoryBudgetProgress>,
    categories: List<Category>,
    windowClass: AdaptiveWindowClass,
    showEditDialog: Boolean,
    limitInput: String,
    onLimitInputChange: (String) -> Unit,
    onShowEditDialogChange: (Boolean) -> Unit,
    onPrepareEdit: () -> Unit,
    onSaveBudget: (Long) -> Unit,
    showCategoryDialog: Boolean,
    selectedCategory: Category?,
    categoryLimitInput: String,
    onCategoryLimitInputChange: (String) -> Unit,
    onSelectedCategoryChange: (Category) -> Unit,
    onShowCategoryDialogChange: (Boolean) -> Unit,
    onPrepareAddCategoryBudget: () -> Unit,
    onPrepareEditCategoryBudget: (CategoryBudgetProgress) -> Unit,
    onSaveCategoryBudget: (String, Long) -> Unit,
    onDeleteCategoryBudget: (String) -> Unit,
    isEditingCategory: Boolean
) {
    val remaining = progressState.limitAmount - progressState.usedAmount
    val remainingLabel = if (remaining >= 0) remaining.formatToRupiah() else "Over Budget (${(-remaining).formatToRupiah()})"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Monthly Budget",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = if (windowClass.isExpanded) 32.dp else 20.dp, vertical = 8.dp)

        LazyColumn(
            modifier = contentModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                if (windowClass.isExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        BudgetProgressVisual(
                            progress = progressState.progress,
                            modifier = Modifier.weight(0.8f)
                        )
                        BudgetDetails(
                            progressState = progressState,
                            remainingLabel = remainingLabel,
                            remaining = remaining,
                            onPrepareEdit = onPrepareEdit,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        BudgetProgressVisual(progress = progressState.progress)
                        BudgetDetails(
                            progressState = progressState,
                            remainingLabel = remainingLabel,
                            remaining = remaining,
                            onPrepareEdit = onPrepareEdit
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Insight",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (progressState.limitAmount == 0L) {
                                "Set a budget to get alerts and statistics about your monthly expenses."
                            } else if (progressState.progress < 0.5f) {
                                "Great job! You've used less than half of your monthly budget limit."
                            } else if (progressState.progress < 0.9f) {
                                "Keep an eye out! You've spent more than 50% of your budget limit."
                            } else {
                                "Alert! You have reached or exceeded your budget limit for this month."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Budgets Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = onPrepareAddCategoryBudget,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category Budget",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (categoryBudgets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No category budgets configured yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(categoryBudgets, key = { it.categoryId }) { cb ->
                    CategoryBudgetCard(
                        categoryBudget = cb,
                        onEdit = { onPrepareEditCategoryBudget(cb) },
                        onDelete = { onDeleteCategoryBudget(cb.categoryId) }
                    )
                }
            }
        }
    }

    // Edit Budget Limit Dialog
    if (showEditDialog) {
        Dialog(onDismissRequest = { onShowEditDialogChange(false) }) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Set Monthly Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = {
                            onLimitInputChange(it.rupiahDigits())
                        },
                        label = { Text("Limit Amount (IDR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = RupiahAmountVisualTransformation,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            shapes = ButtonDefaults.shapes(),
                            onClick = { onShowEditDialogChange(false) }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            shapes = ButtonDefaults.shapes(),
                            onClick = {
                                val limit = limitInput.toLongOrNull() ?: 0L
                                onSaveBudget(limit)
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }

    // Category Budget Add/Edit Dialog
    if (showCategoryDialog) {
        Dialog(onDismissRequest = { onShowCategoryDialogChange(false) }) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isEditingCategory) "Edit Category Budget" else "Add Category Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    var dropdownExpanded by remember { mutableStateOf(false) }
                    // Filter categories to only Expenses
                    val expenseCategories = categories.filter { it.type == com.moneylite.core.domain.model.TransactionType.Expense }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "Select Category",
                            onValueChange = {},
                            label = { Text("Category") },
                            readOnly = true,
                            enabled = !isEditingCategory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isEditingCategory) { dropdownExpanded = true }
                        )
                        if (!isEditingCategory) {
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                expenseCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            onSelectedCategoryChange(category)
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = categoryLimitInput,
                        onValueChange = {
                            onCategoryLimitInputChange(it.rupiahDigits())
                        },
                        label = { Text("Limit Amount (IDR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = RupiahAmountVisualTransformation,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            shapes = ButtonDefaults.shapes(),
                            onClick = { onShowCategoryDialogChange(false) }
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            shapes = ButtonDefaults.shapes(),
                            enabled = selectedCategory != null && categoryLimitInput.isNotEmpty(),
                            onClick = {
                                val limit = categoryLimitInput.toLongOrNull() ?: 0L
                                onSaveCategoryBudget(selectedCategory!!.id, limit)
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetCard(
    categoryBudget: CategoryBudgetProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catColor = categoryBudget.categoryColor.toColor()
    val isOver = categoryBudget.usedAmount > categoryBudget.limitAmount
    val remaining = categoryBudget.limitAmount - categoryBudget.usedAmount

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(categoryBudget.categoryIcon),
                            contentDescription = categoryBudget.categoryName,
                            tint = catColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = categoryBudget.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOver) {
                                "Over by ${(-remaining).formatToRupiah()}"
                            } else {
                                "${remaining.formatToRupiah()} remaining"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOver) Color(0xFFD32F2F) else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { categoryBudget.progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (isOver) Color(0xFFD32F2F) else catColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(categoryBudget.progress * 100).toInt()}% spent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${categoryBudget.usedAmount.formatToRupiah()} of ${categoryBudget.limitAmount.formatToRupiah()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressVisual(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = if (progress >= 0.9f) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Spent",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun BudgetDetails(
    progressState: BudgetProgress,
    remainingLabel: String,
    remaining: Long,
    onPrepareEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = progressState.limitAmount.formatToRupiah(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = onPrepareEdit,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budget",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            LinearWavyProgressIndicator(
                progress = { progressState.progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (progressState.progress >= 0.9f) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = remainingLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining >= 0) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFC62828)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = progressState.usedAmount.formatToRupiah(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

