package com.gorthaur.financetracker.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorthaur.financetracker.core.model.CurrencyCode
import com.gorthaur.financetracker.core.util.Formatters
import com.gorthaur.financetracker.ui.screens.dashboard.CategorySlice
import com.gorthaur.financetracker.ui.screens.dashboard.MonthBar

/** Gráfico de tarta (donut) del reparto de gastos por categoría, con leyenda. */
@Composable
fun CategoryPieChart(
    slices: List<CategorySlice>,
    currency: CurrencyCode,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var startAngle = -90f
            val stroke = Stroke(width = size.minDimension * 0.22f)
            val inset = stroke.width / 2
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            slices.forEach { slice ->
                val sweep = slice.fraction * 360f
                drawArc(
                    color = slice.category.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = stroke
                )
                startAngle += sweep
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.take(6).forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(slice.category.color)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(slice.category.labelRes),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = Formatters.money(slice.amount, currency),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** Barras de ingresos (verde) vs gastos (rojo) para los últimos meses. */
@Composable
fun MonthlyBarChart(
    bars: List<MonthBar>,
    modifier: Modifier = Modifier,
    incomeColor: Color = Color(0xFF2E7D32),
    expenseColor: Color = Color(0xFFC62828)
) {
    val maxValue = bars.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 0.0
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            if (maxValue <= 0 || bars.isEmpty()) return@Canvas
            val groupWidth = size.width / bars.size
            val barWidth = groupWidth * 0.28f
            val gap = groupWidth * 0.1f
            bars.forEachIndexed { index, bar ->
                val groupStart = index * groupWidth
                val incomeHeight = (bar.income / maxValue).toFloat() * size.height
                val expenseHeight = (bar.expense / maxValue).toFloat() * size.height

                drawRect(
                    color = incomeColor,
                    topLeft = Offset(groupStart + groupWidth / 2 - barWidth - gap / 2, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight)
                )
                drawRect(
                    color = expenseColor,
                    topLeft = Offset(groupStart + groupWidth / 2 + gap / 2, size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/** Pequeña leyenda con un punto de color y una etiqueta. */
@Composable
fun LegendDot(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

/** Tarjeta compacta para mostrar un importe etiquetado en el panel. */
@Composable
fun SummaryCard(
    label: String,
    amount: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(accent.copy(alpha = 0.12f))
            .padding(16.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}
