package com.foodmaster.app.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.Meal
import com.foodmaster.app.ui.macroValue
import java.time.LocalDate

@Composable
fun StatsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory),
) {
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    val todayTotal = meals.filter { it.date == today }.total()
    val weekTotal = meals.total()

    if (meals.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(contentPadding).padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Aún no hay comidas registradas.\nPrepara una receta para ver tus macros.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MacroSummaryCard(title = "Hoy", macros = todayTotal)
        MacroSummaryCard(title = "Últimos 7 días", macros = weekTotal)

        Text("Comidas recientes", style = MaterialTheme.typography.titleMedium)
        meals.forEach { meal ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(meal.recipeName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${meal.date} · ${meal.servings} raciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${macroValue(meal.computedMacros.kcal)} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroSummaryCard(title: String, macros: Macros) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${macroValue(macros.kcal)} kcal",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MacroPill("Proteína", macroValue(macros.protein, "g"))
                MacroPill("Carbos", macroValue(macros.carbs, "g"))
                MacroPill("Grasa", macroValue(macros.fat, "g"))
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun List<Meal>.total(): Macros =
    fold(Macros.ZERO) { acc, meal -> acc + meal.computedMacros }
