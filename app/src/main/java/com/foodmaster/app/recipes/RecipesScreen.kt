package com.foodmaster.app.recipes

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodmaster.app.domain.model.Recipe
import com.foodmaster.app.ui.summaryLine

@Composable
fun RecipesScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = viewModel(factory = RecipesViewModel.Factory),
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editing by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(contentPadding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Sin recetas todavía.\nCrea una con el botón +.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(recipes, key = { it.id }) { recipe ->
                    RecipeRow(
                        recipe = recipe,
                        perServingSummary = viewModel.macrosOf(recipe)
                            .times(1.0 / recipe.servings.coerceAtLeast(1))
                            .summaryLine(),
                        onPrepare = {
                            viewModel.prepare(recipe)
                            Toast.makeText(context, "Preparado: ${recipe.name}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { viewModel.delete(recipe) },
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { editing = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nueva receta")
        }
    }

    if (editing) {
        RecipeEditorDialog(
            catalog = catalog,
            onSave = { name, servings, steps, ingredients ->
                viewModel.save(name, servings, steps, ingredients)
                editing = false
            },
            onDismiss = { editing = false },
        )
    }
}

@Composable
private fun RecipeRow(
    recipe: Recipe,
    perServingSummary: String,
    onPrepare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp, end = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${recipe.servings} raciones · ${recipe.ingredients.size} ingredientes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
            Text(
                text = "Por ración: $perServingSummary",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Button(
                onClick = onPrepare,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Preparar (consume del inventario)")
            }
        }
    }
}
