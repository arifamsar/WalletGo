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
import com.moneylite.core.domain.usecase.CategoryBudgetProgress
import com.moneylite.core.ui.theme.LocalThemeIsDark
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

@Composable
fun CategoryBudgetBarChart(
    categoryBudgets: List<CategoryBudgetProgress>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(categoryBudgets) {
        if (categoryBudgets.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    series(categoryBudgets.map { it.limitAmount.toFloat() })
                    series(categoryBudgets.map { it.usedAmount.toFloat() })
                }
            }
        }
    }

    if (categoryBudgets.isEmpty()) return

    val isDark = LocalThemeIsDark.current.value
    // Expressive chart colors
    val limitColor = if (isDark) MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    val spentColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary

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
                    text = "Budget vs actual spent",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendItem(color = limitColor, label = "Limit")
                    LegendItem(color = spentColor, label = "Spent")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            listOf(
                                rememberLineComponent(
                                    fill = Fill(limitColor),
                                    thickness = 8.dp,
                                    shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40)
                                ),
                                rememberLineComponent(
                                    fill = Fill(spentColor),
                                    thickness = 8.dp,
                                    shape = RoundedCornerShape(topStartPercent = 40, topEndPercent = 40)
                                )
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
                            if (value >= 1_000_000) {
                                "${(value / 1_000_000).toInt()}M"
                            } else if (value >= 1_000) {
                                "${(value / 1_000).toInt()}K"
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
                            categoryBudgets.getOrNull(value.toInt())?.categoryName ?: ""
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
