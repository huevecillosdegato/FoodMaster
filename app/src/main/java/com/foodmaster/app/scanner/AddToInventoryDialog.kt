package com.foodmaster.app.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.MeasureUnit
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.StorageLocation
import com.foodmaster.app.ui.QuantityField
import com.foodmaster.app.ui.display
import com.foodmaster.app.ui.label
import com.foodmaster.app.ui.parseAmount
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class InventoryEntry(
    val quantity: Quantity,
    val location: StorageLocation,
    val expirationDate: LocalDate?,
    val lowStockThreshold: Quantity?,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddToInventoryDialog(
    product: Product,
    onConfirm: (InventoryEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val defaultUnit = if (product.servingUnit == BaseUnit.VOLUME) MeasureUnit.MILLILITER else MeasureUnit.GRAM

    var amountText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(defaultUnit) }
    var location by remember { mutableStateOf(StorageLocation.DESPENSA) }

    var lowStockEnabled by remember { mutableStateOf(false) }
    var lowStockText by remember { mutableStateOf("") }

    var expiration by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)
    val lowStockAmount = parseAmount(lowStockText)
    val valid = amount != null && amount > 0.0 &&
        (!lowStockEnabled || (lowStockAmount != null && lowStockAmount > 0.0))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir al inventario") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(product.name)

                QuantityField(
                    amountText = amountText,
                    onAmountChange = { amountText = it },
                    unit = unit,
                    onUnitChange = { unit = it },
                    allowedUnits = MeasureUnit.entries,
                    label = "Cantidad",
                )

                Text("Ubicación")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StorageLocation.entries.forEach { loc ->
                        FilterChip(
                            selected = location == loc,
                            onClick = { location = loc },
                            label = { Text(loc.label()) },
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(expiration?.let { "Caducidad: ${it.display()}" } ?: "Añadir caducidad")
                    }
                    if (expiration != null) {
                        TextButton(onClick = { expiration = null }) { Text("Quitar") }
                    }
                }

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = lowStockEnabled, onCheckedChange = { lowStockEnabled = it })
                    Text("Avisar cuando baje de")
                }
                if (lowStockEnabled) {
                    QuantityField(
                        amountText = lowStockText,
                        onAmountChange = { lowStockText = it },
                        unit = unit,
                        onUnitChange = { unit = it },
                        allowedUnits = MeasureUnit.forDimension(unit.base),
                        label = "Umbral",
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onConfirm(
                        InventoryEntry(
                            quantity = Quantity(amount!!, unit),
                            location = location,
                            expirationDate = expiration,
                            lowStockThreshold = if (lowStockEnabled && lowStockAmount != null) {
                                Quantity(lowStockAmount, unit)
                            } else {
                                null
                            },
                        ),
                    )
                },
            ) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )

    if (showDatePicker) {
        val state = rememberDatePickerStateSafe(expiration)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        expiration = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberDatePickerStateSafe(initial: LocalDate?) =
    androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initial
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli(),
    )
