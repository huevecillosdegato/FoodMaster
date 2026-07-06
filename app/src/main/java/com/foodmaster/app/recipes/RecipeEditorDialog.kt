package com.foodmaster.app.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.foodmaster.app.domain.model.MeasureUnit
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.RecipeIngredient
import com.foodmaster.app.ui.QuantityField
import com.foodmaster.app.ui.display
import com.foodmaster.app.ui.parseAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorDialog(
    catalog: List<Product>,
    onSave: (name: String, servings: Int, steps: List<String>, ingredients: List<RecipeIngredient>) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var servingsText by remember { mutableStateOf("1") }
    var stepsText by remember { mutableStateOf("") }
    val ingredients = remember { mutableListOf<RecipeIngredient>().toMutableStateList() }

    val servings = servingsText.toIntOrNull() ?: 0
    val canSave = name.isNotBlank() && servings >= 1 && ingredients.isNotEmpty()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Nueva receta") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                            }
                        },
                        actions = {
                            TextButton(
                                enabled = canSave,
                                onClick = {
                                    onSave(
                                        name.trim(),
                                        servings,
                                        stepsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
                                        ingredients.toList(),
                                    )
                                },
                            ) { Text("Guardar") }
                        },
                    )
                },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = servingsText,
                        onValueChange = { servingsText = it.filter(Char::isDigit) },
                        label = { Text("Raciones") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    HorizontalDivider()
                    Text("Ingredientes")

                    ingredients.forEachIndexed { index, ingredient ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${ingredient.product.name} — ${ingredient.quantity.display()}",
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { ingredients.removeAt(index) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Quitar")
                            }
                        }
                    }

                    if (catalog.isEmpty()) {
                        Text("Escanea y guarda productos primero para poder añadirlos.")
                    } else {
                        AddIngredientRow(
                            catalog = catalog,
                            onAdd = { ingredients.add(it) },
                        )
                    }

                    HorizontalDivider()
                    OutlinedTextField(
                        value = stepsText,
                        onValueChange = { stepsText = it },
                        label = { Text("Pasos (uno por línea)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIngredientRow(
    catalog: List<Product>,
    onAdd: (RecipeIngredient) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Product?>(null) }
    var amountText by remember { mutableStateOf("100") }
    var unit by remember { mutableStateOf(MeasureUnit.GRAM) }
    val amount = parseAmount(amountText)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selected?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Producto") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                catalog.forEach { product ->
                    DropdownMenuItem(
                        text = { Text(product.name) },
                        onClick = {
                            selected = product
                            expanded = false
                        },
                    )
                }
            }
        }

        QuantityField(
            amountText = amountText,
            onAmountChange = { amountText = it },
            unit = unit,
            onUnitChange = { unit = it },
            allowedUnits = MeasureUnit.entries,
            label = "Cantidad",
        )

        OutlinedButton(
            enabled = selected != null && amount != null && amount > 0.0,
            onClick = {
                val product = selected ?: return@OutlinedButton
                val value = amount ?: return@OutlinedButton
                onAdd(RecipeIngredient(product, Quantity(value, unit)))
                selected = null
                amountText = "100"
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Añadir ingrediente")
        }
    }
}
