package com.foodmaster.app.meallog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.domain.model.MeasureUnit
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.ui.QuantityField
import com.foodmaster.app.ui.parseAmount

/**
 * Asks how much of a scanned [product] was eaten (in the product's own basis, g or
 * ml) and, optionally, what it cost. The item's macros are scaled from this amount.
 */
@Composable
fun ScanPortionDialog(
    product: Product,
    onAdd: (quantity: Quantity, price: Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    val allowedUnits = remember(product.servingUnit) {
        MeasureUnit.forDimension(product.servingUnit)
    }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(allowedUnits.first()) }
    var price by remember { mutableStateOf("") }

    val parsedAmount = parseAmount(amount)
    val valid = parsedAmount != null && parsedAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "¿Cuánto has comido?",
                    style = MaterialTheme.typography.bodyMedium,
                )
                QuantityField(
                    amountText = amount,
                    onAmountChange = { amount = it },
                    unit = unit,
                    onUnitChange = { unit = it },
                    allowedUnits = allowedUnits,
                    modifier = Modifier.fillMaxWidth(),
                )
                PriceField(price) { price = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onAdd(Quantity(parsedAmount!!, unit), parseAmount(price))
                },
            ) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

/**
 * Adds a barcode-less item (e.g. an apple). Macros are entered for the portion
 * directly, and the quantity is descriptive (any unit).
 */
@Composable
fun ManualMealItemDialog(
    onAdd: (name: String, quantity: Quantity, macros: Macros, price: Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(MeasureUnit.PIECE) }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val parsedAmount = parseAmount(amount)
    val valid = name.isNotBlank() && parsedAmount != null && parsedAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir a mano") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (p. ej. Manzana)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                QuantityField(
                    amountText = amount,
                    onAmountChange = { amount = it },
                    unit = unit,
                    onUnitChange = { unit = it },
                    allowedUnits = MeasureUnit.entries,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Macros de esta ración:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroInput("kcal", kcal, Modifier.weight(1f)) { kcal = it }
                    MacroInput("Prot (g)", protein, Modifier.weight(1f)) { protein = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroInput("Carb (g)", carbs, Modifier.weight(1f)) { carbs = it }
                    MacroInput("Grasa (g)", fat, Modifier.weight(1f)) { fat = it }
                }
                PriceField(price) { price = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onAdd(
                        name.trim(),
                        Quantity(parsedAmount!!, unit),
                        Macros(
                            kcal = parseAmount(kcal),
                            protein = parseAmount(protein),
                            carbs = parseAmount(carbs),
                            fat = parseAmount(fat),
                        ),
                        parseAmount(price),
                    )
                },
            ) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun PriceField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
        label = { Text("Precio (€, opcional)") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MacroInput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
    )
}
