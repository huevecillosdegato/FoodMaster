package com.foodmaster.app.consume

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.MeasureUnit
import com.foodmaster.app.domain.model.Product
import com.foodmaster.app.domain.model.Quantity
import com.foodmaster.app.ui.QuantityField
import com.foodmaster.app.ui.display
import com.foodmaster.app.ui.parseAmount

private enum class ConsumeMode { PORTIONS, AMOUNT }

/**
 * Register consumption of [product]. If the product has a portion, the primary
 * input is "N portions" (with its gram equivalent shown); a free amount/unit is
 * always available too. Emits the amount to subtract via [onConfirm].
 */
@Composable
fun ConsumeDialog(
    product: Product,
    stock: Quantity?,
    onConfirm: (Quantity) -> Unit,
    onDismiss: () -> Unit,
) {
    val portion = product.portion
    val baseUnit = if (product.servingUnit == BaseUnit.VOLUME) MeasureUnit.MILLILITER else MeasureUnit.GRAM

    var mode by remember { mutableStateOf(if (portion != null) ConsumeMode.PORTIONS else ConsumeMode.AMOUNT) }
    var portionsText by remember { mutableStateOf("1") }
    var amountText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(baseUnit) }

    val consumeQty: Quantity? = when (mode) {
        ConsumeMode.PORTIONS -> {
            val n = parseAmount(portionsText)
            if (portion != null && n != null && n > 0.0) {
                Quantity(portion.quantity.amount * n, portion.quantity.unit)
            } else {
                null
            }
        }

        ConsumeMode.AMOUNT -> parseAmount(amountText)?.takeIf { it > 0.0 }?.let { Quantity(it, unit) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Consumir ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (stock != null) {
                    Text(
                        "En stock: ${stock.display()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (portion != null) {
                    FilterChip(
                        selected = mode == ConsumeMode.PORTIONS,
                        onClick = { mode = ConsumeMode.PORTIONS },
                        label = { Text("Porciones") },
                    )
                    FilterChip(
                        selected = mode == ConsumeMode.AMOUNT,
                        onClick = { mode = ConsumeMode.AMOUNT },
                        label = { Text("Cantidad") },
                    )
                }

                when (mode) {
                    ConsumeMode.PORTIONS -> {
                        OutlinedTextField(
                            value = portionsText,
                            onValueChange = { portionsText = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                            label = { Text("Nº de ${portion?.label ?: "porciones"}") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        consumeQty?.let { Text("= ${it.display()}") }
                    }

                    ConsumeMode.AMOUNT -> QuantityField(
                        amountText = amountText,
                        onAmountChange = { amountText = it },
                        unit = unit,
                        onUnitChange = { unit = it },
                        allowedUnits = MeasureUnit.entries,
                        label = "Cantidad consumida",
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = consumeQty != null,
                onClick = { consumeQty?.let(onConfirm) },
            ) { Text("Consumir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
