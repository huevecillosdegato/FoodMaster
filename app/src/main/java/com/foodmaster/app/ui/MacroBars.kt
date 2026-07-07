package com.foodmaster.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodmaster.app.domain.model.Macros

/** How much of a nutrient a food has, relative to typical thresholds. */
enum class NutrientLevel(val label: String) {
    LOW("Baja"),
    MODERATE("Media"),
    HIGH("Alta"),
}

// Traffic-light colors; legible in both light and dark themes.
private val LevelGreen = Color(0xFF2E9E4F)
private val LevelAmber = Color(0xFFE8A33D)
private val LevelRed = Color(0xFFD64545)

/**
 * One macro and how to judge/scale it (per 100 g / 100 ml).
 *
 * @param refMax bar length reference — the value that fills the bar completely.
 * @param lowMax at or below this the nutrient is [NutrientLevel.LOW].
 * @param highMin at or above this the nutrient is [NutrientLevel.HIGH].
 * @param higherIsBetter beneficial nutrients (protein, fibre) go green when high;
 *   "limit" nutrients (fat, sugar, salt…) go red when high.
 */
private data class MacroSpec(
    val label: String,
    val value: Double?,
    val unit: String,
    val refMax: Double,
    val lowMax: Double,
    val highMin: Double,
    val higherIsBetter: Boolean,
) {
    val level: NutrientLevel?
        get() = value?.let {
            when {
                it <= lowMax -> NutrientLevel.LOW
                it >= highMin -> NutrientLevel.HIGH
                else -> NutrientLevel.MODERATE
            }
        }

    val color: Color
        get() = when (level) {
            NutrientLevel.LOW -> if (higherIsBetter) LevelAmber else LevelGreen
            NutrientLevel.MODERATE -> LevelAmber
            NutrientLevel.HIGH -> if (higherIsBetter) LevelGreen else LevelRed
            null -> LevelAmber
        }

    val fraction: Float
        get() = value?.let { (it / refMax).toFloat().coerceIn(0f, 1f) } ?: 0f
}

/**
 * Horizontal macro bars for a per-100 [macros] block. Each bar is scaled by amount
 * and coloured green/amber/red by whether the food has a lot or a little of it.
 */
@Composable
fun MacroBars(macros: Macros, modifier: Modifier = Modifier) {
    val specs = listOf(
        MacroSpec("Proteína", macros.protein, "g", refMax = 30.0, lowMax = 5.0, highMin = 12.0, higherIsBetter = true),
        MacroSpec("Carbohidratos", macros.carbs, "g", refMax = 60.0, lowMax = 10.0, highMin = 40.0, higherIsBetter = false),
        MacroSpec("Grasa", macros.fat, "g", refMax = 40.0, lowMax = 3.0, highMin = 20.0, higherIsBetter = false),
        MacroSpec("Fibra", macros.fiber, "g", refMax = 15.0, lowMax = 2.0, highMin = 6.0, higherIsBetter = true),
        MacroSpec("Azúcares", macros.sugar, "g", refMax = 30.0, lowMax = 5.0, highMin = 12.5, higherIsBetter = false),
        MacroSpec("Sal", macros.salt, "g", refMax = 3.0, lowMax = 0.3, highMin = 1.5, higherIsBetter = false),
    ).filter { it.value != null }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        specs.forEach { MacroBarRow(it) }
    }
}

@Composable
private fun MacroBarRow(spec: MacroSpec) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(spec.label, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${macroValue(spec.value)} ${spec.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                spec.level?.let {
                    Text(
                        text = " · ${it.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = spec.color,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(6.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(spec.fraction)
                    .height(10.dp)
                    .background(spec.color, RoundedCornerShape(6.dp)),
            )
        }
    }
}
