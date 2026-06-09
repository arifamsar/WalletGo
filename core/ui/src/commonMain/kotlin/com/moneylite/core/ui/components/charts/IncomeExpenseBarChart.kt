package com.moneylite.core.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneylite.core.domain.model.TransactionType
import com.moneylite.core.domain.model.TransactionUiModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer




@Composable
fun IncomeExpenseBarChart(
    transactions: List<TransactionUiModel>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(transactions) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val weeklyIncome = mutableMapOf(1 to 0L, 2 to 0L, 3 to 0L, 4 to 0L, 5 to 0L)
            val weeklyExpense = mutableMapOf(1 to 0L, 2 to 0L, 3 to 0L, 4 to 0L, 5 to 0L)

            for (tx in transactions) {
                val week = ((tx.date.day - 1) / 7 + 1).coerceIn(1, 5)
                if (tx.type == TransactionType.Income) {
                    weeklyIncome[week] = (weeklyIncome[week] ?: 0L) + tx.amount
                } else {
                    weeklyExpense[week] = (weeklyExpense[week] ?: 0L) + tx.amount
                }
            }

            val incomeSeries = weeklyIncome.values.toList().map { it.toFloat() }
            val expenseSeries = weeklyExpense.values.toList().map { it.toFloat() }

            modelProducer.runTransaction {
                columnSeries {
                    series(incomeSeries)
                    series(expenseSeries)
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Cash Flow",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendItem(color = Color(0xFF2E7D32), label = "Income")
                    LegendItem(color = Color(0xFFC62828), label = "Expense")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Remember Vico components to avoid recreation on every recomposition
            val incomeFill = remember { Fill(Color(0xFF2E7D32)) }
            val expenseFill = remember { Fill(Color(0xFFC62828)) }

            val incomeLineComponent = rememberLineComponent(
                fill = incomeFill,
                thickness = 8.dp,
                shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40)
            )
            val expenseLineComponent = rememberLineComponent(
                fill = expenseFill,
                thickness = 8.dp,
                shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40)
            )

            val columnProviderList = remember(incomeLineComponent, expenseLineComponent) {
                listOf(incomeLineComponent, expenseLineComponent)
            }
            val columnProvider = remember(columnProviderList) {
                ColumnCartesianLayer.ColumnProvider.series(columnProviderList)
            }
            val columnLayer = rememberColumnCartesianLayer(columnProvider = columnProvider)

            val startAxisValueFormatter = remember {
                CartesianValueFormatter { _, value, _ ->
                    if (value >= 1_000_000) {
                        "${(value / 1_000_000).toInt()}M"
                    } else if (value >= 1_000) {
                        "${(value / 1_000).toInt()}K"
                    } else {
                        value.toInt().toString()
                    }
                }
            }

            val bottomAxisValueFormatter = remember {
                CartesianValueFormatter { _, value, _ ->
                    val week = value.toInt() + 1
                    "Week $week"
                }
            }

            val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            val axisLabelStyle = remember(axisLabelColor) {
                TextStyle(
                    color = axisLabelColor,
                    fontSize = 10.sp
                )
            }
            val startAxisLabel = rememberTextComponent(axisLabelStyle)
            val bottomAxisLabel = rememberTextComponent(axisLabelStyle)

            val axisLineColor = MaterialTheme.colorScheme.outlineVariant
            val axisLineFill = remember(axisLineColor) { Fill(axisLineColor) }
            val axisLine = rememberLineComponent(fill = axisLineFill, thickness = 1.dp)

            val tickFill = remember(axisLineColor) { Fill(axisLineColor) }
            val tickLine = rememberLineComponent(fill = tickFill, thickness = 1.dp)

            val guidelineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            val guidelineFill = remember(guidelineColor) { Fill(guidelineColor) }
            val startAxisGuideline = rememberLineComponent(fill = guidelineFill, thickness = 1.dp)

            val startAxis = VerticalAxis.rememberStart(
                label = startAxisLabel,
                line = axisLine,
                tick = tickLine,
                guideline = startAxisGuideline,
                valueFormatter = startAxisValueFormatter
            )

            val bottomAxis = HorizontalAxis.rememberBottom(
                label = bottomAxisLabel,
                line = axisLine,
                tick = tickLine,
                guideline = null,
                valueFormatter = bottomAxisValueFormatter
            )

            val chart = rememberCartesianChart(
                columnLayer,
                startAxis = startAxis,
                bottomAxis = bottomAxis
            )

            CartesianChartHost(
                chart = chart,
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
