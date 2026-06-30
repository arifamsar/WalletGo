@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)
package com.moneylite.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneylite.core.domain.repository.TransactionRepository
import com.moneylite.core.domain.repository.CategoryRepository
import com.moneylite.core.data.service.UserPreferences
import com.moneylite.core.data.service.shareText
import com.moneylite.core.domain.usecase.ExportTransactionsUseCase
import com.moneylite.core.domain.usecase.ImportTransactionsUseCase
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import com.moneylite.core.ui.components.FilePickerButton
import com.moneylite.core.ui.components.FileSaverButton
import com.moneylite.core.ui.adaptive.AdaptiveWindowBox
import com.moneylite.core.ui.adaptive.AdaptiveWindowClass
import com.moneylite.core.ui.adaptive.isExpanded
import com.moneylite.core.ui.theme.ThemeTemplate
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onThemeSettings: () -> Unit
) {
    val userPreferences = koinInject<UserPreferences>()
    val transactionRepository = koinInject<TransactionRepository>()
    val categoryRepository = koinInject<CategoryRepository>()
    val exportTransactionsUseCase = koinInject<ExportTransactionsUseCase>()
    val importTransactionsUseCase = koinInject<ImportTransactionsUseCase>()
    val isDark by userPreferences.darkModeEnabledFlow().collectAsStateWithLifecycle(initialValue = false)
    val themeTemplate by userPreferences.themeTemplateFlow().collectAsStateWithLifecycle(initialValue = ThemeTemplate.Default)
    val coroutineScope = rememberCoroutineScope()

    var showClearDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var isImporting by remember { mutableStateOf(false) }

    AdaptiveWindowBox(modifier = modifier) { windowClass ->
        SettingsScreenContent(
            isDark = isDark,
            themeTemplate = themeTemplate,
            windowClass = windowClass,
            showClearDialog = showClearDialog,
            onBack = onBack,
            onThemeSettings = onThemeSettings,
            onDarkModeChange = { value ->
                coroutineScope.launch {
                    userPreferences.setDarkModeEnabled(value)
                }
            },
            onShowClearDialogChange = { showClearDialog = it },
            onClearDatabase = {
                coroutineScope.launch {
                    transactionRepository.deleteAllTransactions()
                    categoryRepository.deleteAllCategories()
                    categoryRepository.seedDefaultCategories()
                    snackbarHostState.showSnackbar("Database cleared successfully!")
                }
                showClearDialog = false
            },
            onExportLedger = { onReady ->
                coroutineScope.launch {
                    try {
                        val csvText = exportTransactionsUseCase()
                        onReady(csvText)
                        snackbarHostState.showSnackbar("Ledger exported successfully!")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Export failed: ${e.message ?: "Unknown error"}")
                    }
                }
            },
            onImportLedger = { csvContent ->
                coroutineScope.launch {
                    isImporting = true
                    var successMessage: String? = null
                    var errorMessage: String? = null
                    
                    try {
                        importTransactionsUseCase(csvContent)
                            .onSuccess { count ->
                                successMessage = "Successfully imported $count transactions!"
                            }
                            .onFailure { error ->
                                errorMessage = "Import failed: ${error.message ?: "Unknown error"}"
                            }
                    } catch (e: Exception) {
                        errorMessage = "Import failed: ${e.message ?: "Unknown error"}"
                    }
                    
                    // Clear the loading overlay before showing the snackbar, 
                    // because showSnackbar suspends until the snackbar is dismissed!
                    isImporting = false
                    
                    val finalSuccess = successMessage
                    val finalError = errorMessage
                    if (finalSuccess != null) {
                        snackbarHostState.showSnackbar(finalSuccess)
                    } else if (finalError != null) {
                        snackbarHostState.showSnackbar(finalError)
                    }
                }
            },
            snackbarHostState = snackbarHostState,
            isImporting = isImporting
        )
    }
}

@Composable
fun SettingsScreenContent(
    isDark: Boolean,
    themeTemplate: ThemeTemplate,
    windowClass: AdaptiveWindowClass,
    showClearDialog: Boolean,
    onBack: () -> Unit,
    onThemeSettings: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onShowClearDialogChange: (Boolean) -> Unit,
    onClearDatabase: () -> Unit,
    onExportLedger: ((String) -> Unit) -> Unit,
    onImportLedger: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    isImporting: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!windowClass.isExpanded) {
                        IconButton(
                            shapes = IconButtonDefaults.shapes(),
                            onClick = onBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
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
                .padding(horizontal = if (windowClass.isExpanded) 24.dp else 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // General Settings Section
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column {
                    // Dark Mode Toggle Row
                    SettingRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Enable dark theme",
                        action = {
                            Switch(
                                checked = isDark,
                                onCheckedChange = onDarkModeChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    )

                    // Currency Row
                    SettingRow(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = themeTemplate.label,
                        action = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        },
                        onClick = onThemeSettings
                    )

                    SettingRow(
                        icon = Icons.Default.Payments,
                        title = "Currency",
                        subtitle = "Default: IDR (Rupiah)",
                        action = {
                            Text(
                                text = "Rp (IDR)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }

            // Data Management Section
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column {
                    // Export ledger row
                    FileSaverButton(
                        onRequestFileContent = onExportLedger,
                        fileName = "moneylite_ledger.json",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingRow(
                            icon = Icons.Default.Share,
                            title = "Export Ledger",
                            subtitle = "Save transactions list to a JSON file",
                            action = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            },
                            onClick = null
                        )
                    }

                    // Import ledger row
                    FilePickerButton(
                        onFileContentPicked = onImportLedger,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingRow(
                            icon = Icons.Default.Upload,
                            title = "Import Ledger",
                            subtitle = "Load transactions from a JSON file",
                            action = {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            },
                            onClick = null
                        )
                    }

                    // Clear database row
                    SettingRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Database",
                        subtitle = "Erase all transactions data",
                        action = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        },
                        onClick = { onShowClearDialogChange(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // App version card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MoneyLite v1.0.0 • Offline-First",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        }
        }

        if (isImporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Clear Database Confirm Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { onShowClearDialogChange(false) },
            title = { Text("Erase Database") },
            text = { Text("Are you sure you want to delete all transactions? This action is permanent and cannot be undone.") },
            confirmButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = onClearDatabase
                ) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { onShowClearDialogChange(false) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        action()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailScreen(
    modifier: Modifier = Modifier,
    settingId: String,
    onBack: () -> Unit
) {
    // Left as stub since nested settings are not requested for MVP v1
}
