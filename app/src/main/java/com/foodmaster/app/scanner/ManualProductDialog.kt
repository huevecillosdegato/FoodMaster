package com.foodmaster.app.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.Macros
import com.foodmaster.app.ui.parseAmount

/** Minimal manual product entry, used when a scanned barcode isn't found. */
@Composable
fun ManualProductDialog(
    barcode: String?,
    onSave: (name: String, brand: String?, servingUnit: BaseUnit, macros: Macros) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var basis by remember { mutableStateOf(BaseUnit.MASS) }
    var kcal by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alta manual") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (barcode != null) {
                    Text("Código: $barcode")
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marca (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Valores por 100:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = basis == BaseUnit.MASS,
                        onClick = { basis = BaseUnit.MASS },
                        label = { Text("100 g") },
                    )
                    FilterChip(
                        selected = basis == BaseUnit.VOLUME,
                        onClick = { basis = BaseUnit.VOLUME },
                        label = { Text("100 ml") },
                    )
                }

                MacroInput("kcal", kcal) { kcal = it }
                MacroInput("Proteína (g)", protein) { protein = it }
                MacroInput("Carbohidratos (g)", carbs) { carbs = it }
                MacroInput("Grasa (g)", fat) { fat = it }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onSave(
                        name.trim(),
                        brand.trim().ifBlank { null },
                        basis,
                        Macros(
                            kcal = parseAmount(kcal),
                            protein = parseAmount(protein),
                            carbs = parseAmount(carbs),
                            fat = parseAmount(fat),
                        ),
                    )
                },
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}

@Composable
private fun MacroInput(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}
