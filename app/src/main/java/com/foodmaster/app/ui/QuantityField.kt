package com.foodmaster.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.foodmaster.app.domain.model.MeasureUnit

/**
 * Amount text field + unit dropdown, used by the add-to-inventory and consume
 * dialogs. State is hoisted so callers own the values.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityField(
    amountText: String,
    onAmountChange: (String) -> Unit,
    unit: MeasureUnit,
    onUnitChange: (MeasureUnit) -> Unit,
    allowedUnits: List<MeasureUnit>,
    modifier: Modifier = Modifier,
    label: String = "Cantidad",
) {
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = modifier) {
        OutlinedTextField(
            value = amountText,
            onValueChange = { new -> onAmountChange(new.filter { it.isDigit() || it == '.' || it == ',' }) },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier
                .padding(start = 8.dp)
                .width(110.dp),
        ) {
            OutlinedTextField(
                value = unit.symbol,
                onValueChange = {},
                readOnly = true,
                label = { Text("Unidad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                allowedUnits.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("${option.symbol} · ${option.name.lowercase()}") },
                        onClick = {
                            onUnitChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

/** Parse the localized amount text (accepts comma or dot) into a Double. */
fun parseAmount(text: String): Double? = text.replace(',', '.').toDoubleOrNull()
