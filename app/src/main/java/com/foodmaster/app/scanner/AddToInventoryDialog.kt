package com.foodmaster.app.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.MeasureUnit
import com.foodmaster.app.domain.model.Money
import com.foodmaster.app.domain.model.Portion
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.domain.model.StorageLocation
import com.foodmaster.app.ui.QuantityField
import com.foodmaster.app.ui.display
import com.foodmaster.app.ui.formatNumber
import com.foodmaster.app.ui.label
import com.foodmaster.app.ui.parseAmount
import com.foodmaster.app.ui.parseMoney
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class InventoryEntry(
    val quantity: Quantity,
    val location: StorageLocation,
    val expirationDate: LocalDate?,
    val lowStockThreshold: Quantity?,
    val netContent: Quantity?,
    val portion: Portion?,
    val unitPrice: Money?,
)

private enum class PortionMode { GRAMS, PER_PACK }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddToInventoryDialog(
    product: Product,
    onConfirm: (InventoryEntry) -> Unit,
    onDismiss: () -> Unit,
) {
    val dimensionUnits = MeasureUnit.forDimension(product.servingUnit)
    val baseDefault = if (product.servingUnit == BaseUnit.VOLUME) MeasureUnit.MILLILITER else MeasureUnit.GRAM

    var amountText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(MeasureUnit.PIECE) }
    var location by remember { mutableStateOf(StorageLocation.DESPENSA) }

    var priceText by remember { mutableStateOf("") }

    var lowStockEnabled by remember { mutableStateOf(false) }
    var lowStockText by remember { mutableStateOf("") }

    var expiration by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Packaging (remembered on the product).
    var netContentText by remember { mutableStateOf(product.netContent?.let { formatNumber(it.amount) } ?: "") }
    var netContentUnit by remember { mutableStateOf(product.netContent?.unit ?: baseDefault) }
    var portionLabel by remember { mutableStateOf(product.portion?.label ?: "") }
    var portionMode by remember { mutableStateOf(PortionMode.GRAMS) }
    var portionGramsText by remember { mutableStateOf(product.portion?.let { formatNumber(it.quantity.amount) } ?: "") }
    var portionUnit by remember { mutableStateOf(product.portion?.quantity?.unit ?: baseDefault) }
    var portionsPerPackText by remember { mutableStateOf("") }

    val amount = parseAmount(amountText)
    val lowStockAmount = parseAmount(lowStockText)
    val netContent = parseAmount(netContentText)?.let { Quantity(it, netContentUnit) }

    val portion = buildPortion(
        mode = portionMode,
        label = portionLabel,
        gramsText = portionGramsText,
        portionUnit = portionUnit,
        perPackText = portionsPerPackText,
        netContent = netContent,
    )

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

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("Precio pagado (€, opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(expiration?.let { "Caducidad: ${it.display()}" } ?: "Añadir caducidad")
                    }
                    if (expiration != null) {
                        TextButton(onClick = { expiration = null }) { Text("Quitar") }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
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

                HorizontalDivider()
                Text("Datos del producto (se recuerdan)")

                QuantityField(
                    amountText = netContentText,
                    onAmountChange = { netContentText = it },
                    unit = netContentUnit,
                    onUnitChange = { netContentUnit = it },
                    allowedUnits = dimensionUnits,
                    label = "Peso/contenido del paquete",
                )

                OutlinedTextField(
                    value = portionLabel,
                    onValueChange = { portionLabel = it },
                    label = { Text("Nombre de la porción (p.ej. loncha)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Definir la porción por:")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = portionMode == PortionMode.GRAMS,
                        onClick = { portionMode = PortionMode.GRAMS },
                        label = { Text("Peso") },
                    )
                    FilterChip(
                        selected = portionMode == PortionMode.PER_PACK,
                        onClick = { portionMode = PortionMode.PER_PACK },
                        label = { Text("Nº por paquete") },
                    )
                }
                when (portionMode) {
                    PortionMode.GRAMS -> QuantityField(
                        amountText = portionGramsText,
                        onAmountChange = { portionGramsText = it },
                        unit = portionUnit,
                        onUnitChange = { portionUnit = it },
                        allowedUnits = dimensionUnits,
                        label = "Tamaño de la porción",
                    )

                    PortionMode.PER_PACK -> {
                        OutlinedTextField(
                            value = portionsPerPackText,
                            onValueChange = { portionsPerPackText = it.filter(Char::isDigit) },
                            label = { Text("Porciones por paquete") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        portion?.let { Text("→ 1 porción = ${it.quantity.display()}") }
                    }
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
                            netContent = netContent,
                            portion = portion,
                            unitPrice = parseMoney(priceText),
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

/** Resolve the portion from the chosen mode, or null when not defined. */
private fun buildPortion(
    mode: PortionMode,
    label: String,
    gramsText: String,
    portionUnit: MeasureUnit,
    perPackText: String,
    netContent: Quantity?,
): Portion? {
    val name = label.trim().ifBlank { "porción" }
    return when (mode) {
        PortionMode.GRAMS -> parseAmount(gramsText)
            ?.takeIf { it > 0.0 }
            ?.let { Portion(name, Quantity(it, portionUnit)) }

        PortionMode.PER_PACK -> {
            val n = perPackText.toIntOrNull()
            if (n != null && n > 0 && netContent != null) {
                val perBase = netContent.toBase() / n
                Portion(name, Quantity(perBase, netContent.unit.baseAsUnit()))
            } else {
                null
            }
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
