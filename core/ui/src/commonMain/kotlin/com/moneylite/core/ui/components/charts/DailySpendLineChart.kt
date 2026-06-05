package com.moneylite.core.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.TransactionUiModel
import com.moneylite.core.ui.theme.LocalThemeIsDark
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class ChartFilter {
    Expense,
    Income,
    Net
}

@Composable
fun DailySpendLineChart(
    transactions: List<TransactionUiModel>,
    selectedType: TransactionType?,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    var chartFilter by remember { mutableStateOf(ChartFilter.Expense) }

    LaunchedEffect(selectedType) {
        chartFilter = when (selectedType) {
            TransactionType.Expense -> ChartFilter.Expense
            TransactionType.Income -> ChartFilter.Income
            null -> ChartFilter.Expense
        }
    }

    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val currentMonth = now.month
    val currentYear = now.year
    val currentDay = now.day

    LaunchedEffect(transactions, chartFilter) {
        // Filter transactions of the current month
        val monthlyTransactions = transactions.filter {
            it.date.month == currentMonth &&
            it.date.year == currentYear
        }

        // Group by day of month
        val txsByDay = monthlyTransactions.groupBy { it.date.day }

        // Cumulative arrays
        val cumulativeExpenses = mutableListOf<Float>()
        val cumulativeIncome = mutableListOf<Float>()
        val cumulativeNet = mutableListOf<Float>()

        var runningExpense = 0L
        var runningIncome = 0L
        var runningNet = 0L

        for (day in 1..currentDay) {
            val dayTxs = txsByDay[day] ?: emptyList()

            val dayExpense = dayTxs.filter { it.type == TransactionType.Expense }.sumOf { it.amount }
            val dayIncome = dayTxs.filter { it.type == TransactionType.Income }.sumOf { it.amount }

            runningExpense += dayExpense
            runningIncome += dayIncome
            runningNet += (dayIncome - dayExpense)

            cumulativeExpenses.add(runningExpense.toFloat())
            cumulativeIncome.add(runningIncome.toFloat())
            cumulativeNet.add(runningNet.toFloat())
        }

        val seriesData = when (chartFilter) {
            ChartFilter.Expense -> if (cumulativeExpenses.isEmpty()) listOf(0f) else cumulativeExpenses
            ChartFilter.Income -> if (cumulativeIncome.isEmpty()) listOf(0f) else cumulativeIncome
            ChartFilter.Net -> if (cumulativeNet.isEmpty()) listOf(0f) else cumulativeNet
        }

        modelProducer.runTransaction {
            lineSeries {
                series(seriesData)
            }
        }
    }

    val isDark = LocalThemeIsDark.current.value
    val expenseColor = if (isDark) Color(0xFFE57373) else Color(0xFFD32F2F)
    val incomeColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
    val netColor = if (isDark) Color(0xFFAAC7FF) else Color(0xFF1565C0)

    val lineColor = remember(chartFilter, isDark) {
        when (chartFilter) {
            ChartFilter.Expense -> expenseColor
            ChartFilter.Income -> incomeColor
            ChartFilter.Net -> netColor
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (chartFilter) {
                        ChartFilter.Expense -> "Cumulative Spending"
                        ChartFilter.Income -> "Cumulative Income"
                        ChartFilter.Net -> "Net Balance Trend"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedType == null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChartFilterChip(
                            label = "Expense",
                            selected = chartFilter == ChartFilter.Expense,
                            selectedColor = expenseColor,
                            onClick = { chartFilter = ChartFilter.Expense }
                        )
                        ChartFilterChip(
                            label = "Income",
                            selected = chartFilter == ChartFilter.Income,
                            selectedColor = incomeColor,
                            onClick = { chartFilter = ChartFilter.Income }
                        )
                        ChartFilterChip(
                            label = "Net",
                            selected = chartFilter == ChartFilter.Net,
                            selectedColor = netColor,
                            onClick = { chartFilter = ChartFilter.Net }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                                areaFill = LineCartesianLayer.AreaFill.single(
                                    Fill(
                                        Brush.verticalGradient(
                                            listOf(lineColor.copy(alpha = 0.24f), Color.Transparent)
                                        )
                                    )
                                ),
                                interpolator = LineCartesianLayer.Interpolator.cubic()
                            )
                        )
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        label = rememberTextComponent(
                            TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        ),
                        line = rememberLineComponent(
                            fill = Fill(MaterialTheme.colorScheme.outlineVariant),
                            thickness = 1.dp
                        ),
                        tick = rememberLineComponent(
                            fill = Fill(MaterialTheme.colorScheme.outlineVariant),
                            thickness = 1.dp
                        ),
                        guideline = rememberLineComponent(
                            fill = Fill(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                            thickness = 1.dp
                        ),
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            val absVal = if (value < 0) -value else value
                            val sign = if (value < 0) "-" else ""
                            if (absVal >= 1_000_000) {
                                "$sign${(absVal / 1_000_000).toInt()}M"
                            } else if (absVal >= 1_000) {
                                "$sign${(absVal / 1_000).toInt()}K"
                            } else {
                                value.toInt().toString()
                            }
                        }
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = rememberTextComponent(
                            TextStyle(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        ),
                        line = rememberLineComponent(
                            fill = Fill(MaterialTheme.colorScheme.outlineVariant),
                            thickness = 1.dp
                        ),
                        tick = rememberLineComponent(
                            fill = Fill(MaterialTheme.colorScheme.outlineVariant),
                            thickness = 1.dp
                        ),
                        guideline = null,
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            val day = value.toInt() + 1
                            "Day $day"
                        }
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun ChartFilterChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val containerColor = if (selected) selectedColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) selectedColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
    }
}
