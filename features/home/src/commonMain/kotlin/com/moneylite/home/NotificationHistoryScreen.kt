@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)
package com.moneylite.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moneylite.core.common.utils.isToday
import com.moneylite.core.common.utils.nowAsLocalDate
import com.moneylite.core.common.utils.to12HourFormat
import com.moneylite.core.domain.model.NotificationHistoryItem
import com.moneylite.core.ui.adaptive.AdaptiveWindowBox
import com.moneylite.core.ui.adaptive.AdaptiveWindowClass
import com.moneylite.core.ui.adaptive.isExpanded
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant as KxInstant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import com.moneylite.core.ui.generated.resources.*

@Composable
fun NotificationHistoryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val viewModel: NotificationHistoryViewModel = koinViewModel()
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()

    var showClearConfirm by remember { mutableStateOf(false) }

    AdaptiveWindowBox(modifier = modifier) { windowClass ->
        NotificationHistoryScreenContent(
            historyItems = historyItems,
            windowClass = windowClass,
            showClearConfirm = showClearConfirm,
            onBack = onBack,
            onShowClearConfirm = { showClearConfirm = it },
            onClearAll = {
                viewModel.clearHistory()
                showClearConfirm = false
            }
        )
    }
}

@Composable
fun NotificationHistoryScreenContent(
    historyItems: List<NotificationHistoryItem>,
    windowClass: AdaptiveWindowClass,
    showClearConfirm: Boolean,
    onBack: () -> Unit,
    onShowClearConfirm: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.notification_history),
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
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(Res.string.back)
                            )
                        }
                    }
                },
                actions = {
                    if (historyItems.isNotEmpty()) {
                        IconButton(onClick = { onShowClearConfirm(true) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(Res.string.clear_all),
                                tint = MaterialTheme.colorScheme.error
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
            if (historyItems.isEmpty()) {
                EmptyNotificationsView(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = if (windowClass.isExpanded) 760.dp else Dp.Unspecified)
                )
            } else {
                val grouped = remember(historyItems) {
                    historyItems.groupBy { item ->
                        val localDateTime = KxInstant.fromEpochMilliseconds(item.timestamp.toEpochMilliseconds())
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        localDateTime.date
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = if (windowClass.isExpanded) 760.dp else Dp.Unspecified),
                    contentPadding = PaddingValues(
                        horizontal = if (windowClass.isExpanded) 24.dp else 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    grouped.forEach { (date, items) ->
                        item(key = "header_${date.hashCode()}") {
                            Text(
                                text = date.toDateHeaderString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(
                            items = items,
                            key = { it.id }
                        ) { item ->
                            val localTime = KxInstant.fromEpochMilliseconds(item.timestamp.toEpochMilliseconds())
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .time

                            NotificationRowItem(
                                title = item.title,
                                body = item.body,
                                timeStr = localTime.to12HourFormat(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { onShowClearConfirm(false) },
            title = { Text("Clear Notification History") },
            text = { Text("Are you sure you want to clear all budget notifications? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = onClearAll
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { onShowClearConfirm(false) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NotificationRowItem(
    title: String,
    body: String,
    timeStr: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.all_caught_up),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.no_notifications_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private fun LocalDate.toDateHeaderString(): String {
    val today = Clock.System.nowAsLocalDate()
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return when (this) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> {
            val monthName = month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
            "$monthName $day, $year"
        }
    }
}
