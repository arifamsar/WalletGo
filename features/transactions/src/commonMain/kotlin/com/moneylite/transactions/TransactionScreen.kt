@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
package com.moneylite.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.formatToRupiah
import com.moneylite.core.ui.adaptive.AdaptiveWindowBox
import com.moneylite.core.ui.adaptive.AdaptiveWindowClass
import com.moneylite.core.ui.adaptive.isExpanded
import com.moneylite.core.ui.components.AppDialog
import com.moneylite.core.ui.components.AppPullToRefresh
import com.moneylite.core.domain.model.TransactionUiModel
import com.moneylite.core.ui.utils.toColor
import com.moneylite.core.ui.utils.getCategoryIcon
import com.moneylite.core.ui.components.charts.DailySpendLineChart
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToEditTransaction: (String) -> Unit = {}
) {
    val viewModel: TransactionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TransactionListEffect.TransactionDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Transaction deleted",
                        actionLabel = "Undo",
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            viewModel.onIntent(TransactionListIntent.UndoDeleteTransaction(effect.id))
                        }
                        SnackbarResult.Dismissed -> {
                            viewModel.onIntent(TransactionListIntent.ClearPendingUndo(effect.id))
                        }
                    }
                }
            }
        }
    }

    uiState.deleteCandidate?.let { transaction ->
        AppDialog(
            title = "Delete transaction?",
            message = "Delete ${transaction.categoryName} transaction for ${transaction.amount.formatToRupiah()}?",
            confirmText = "Delete",
            dismissText = "Cancel",
            icon = Icons.Default.DeleteOutline,
            onConfirm = {
                viewModel.onIntent(TransactionListIntent.ConfirmDeleteTransaction)
            },
            onDismiss = {
                viewModel.onIntent(TransactionListIntent.DismissDeleteDialog)
            }
        )
    }

    AdaptiveWindowBox(modifier = modifier) { windowClass ->
        TransactionScreenContent(
            uiState = uiState,
            windowClass = windowClass,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::onIntent,
            onNavigateToAddTransaction = onNavigateToAddTransaction,
            onNavigateToEditTransaction = onNavigateToEditTransaction
        )
    }
}

@Composable
fun TransactionScreenContent(
    uiState: TransactionListState,
    windowClass: AdaptiveWindowClass,
    snackbarHostState: SnackbarHostState,
    onIntent: (TransactionListIntent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToEditTransaction: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = if (windowClass.isExpanded) 760.dp else androidx.compose.ui.unit.Dp.Unspecified)
        ) {
            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = {
                    onIntent(TransactionListIntent.SearchQueryChanged(it))
                },
                placeholder = { Text("Search transactions...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = {
                                onIntent(TransactionListIntent.SearchQueryChanged(""))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter chips
            val isDark = com.moneylite.core.ui.theme.LocalThemeIsDark.current.value
            val selectedIncomeContainer = if (isDark) Color(0xFF1B5E20).copy(alpha = 0.4f) else Color(0xFFE8F5E9)
            val selectedIncomeLabel = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Chip
                val isAllSelected = uiState.selectedType == null
                FilterChip(
                    selected = isAllSelected,
                    onClick = {
                        onIntent(TransactionListIntent.TypeFilterSelected(null))
                    },
                    label = { Text("All") },
                    shape = MaterialTheme.shapes.medium,
                    border = if (isAllSelected) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                // Expense Chip
                val isExpenseSelected = uiState.selectedType == TransactionType.Expense
                FilterChip(
                    selected = isExpenseSelected,
                    onClick = {
                        onIntent(TransactionListIntent.TypeFilterSelected(TransactionType.Expense))
                    },
                    label = { Text("Expenses") },
                    shape = MaterialTheme.shapes.medium,
                    border = if (isExpenseSelected) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )

                // Income Chip
                val isIncomeSelected = uiState.selectedType == TransactionType.Income
                FilterChip(
                    selected = isIncomeSelected,
                    onClick = {
                        onIntent(TransactionListIntent.TypeFilterSelected(TransactionType.Income))
                    },
                    label = { Text("Income") },
                    shape = MaterialTheme.shapes.medium,
                    border = if (isIncomeSelected) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = selectedIncomeContainer,
                        selectedLabelColor = selectedIncomeLabel
                    )
                )
            }

            // List or Empty State
            AppPullToRefresh(
                isRefreshing = uiState.isLoading,
                onRefresh = {
                    onIntent(TransactionListIntent.Load)
                },
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.groupedTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try altering filters or search criteria",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            DailySpendLineChart(
                                transactions = uiState.transactions,
                                selectedType = uiState.selectedType,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                        }

                        uiState.groupedTransactions.forEach { (date, transactions) ->
                            // Date header
                            stickyHeader(key = date.toString()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = date.toString(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Transactions under this date
                            items(
                                items = transactions,
                                key = { it.id }
                            ) { tx ->
                                TransactionListItem(
                                    transaction = tx,
                                    onDelete = {
                                        onIntent(TransactionListIntent.RequestDeleteTransaction(tx))
                                    },
                                    onClick = {
                                        onNavigateToEditTransaction(tx.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: TransactionUiModel,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(transaction.categoryColor.toColor().copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.categoryIcon),
                    contentDescription = transaction.categoryName,
                    tint = transaction.categoryColor.toColor(),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title & Note
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.categoryName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val note = transaction.note
                if (!note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Amount & Delete action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = (if (transaction.type == TransactionType.Income) "+" else "-") + transaction.amount.formatToRupiah(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.type == TransactionType.Income) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    shapes = IconButtonDefaults.shapes(),
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
