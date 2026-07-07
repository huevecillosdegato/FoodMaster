package com.foodmaster.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.foodmaster.app.domain.model.BaseUnit
import com.foodmaster.app.domain.model.DataSource
import com.foodmaster.app.domain.model.Product

/**
 * Product "ficha": a bottom sheet with the product's data and a macro breakdown
 * shown as coloured horizontal bars. Reusable anywhere a [Product] is on screen.
 *
 * @param extra optional context lines (e.g. inventory quantity / location / expiry).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    product: Product,
    onDismiss: () -> Unit,
    extra: (@Composable () -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .padding(end = 4.dp),
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleLarge)
                    product.brand?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    product.barcode?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (extra != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                extra()
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            val basisLabel = if (product.servingUnit == BaseUnit.VOLUME) "100 ml" else "100 g"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("Valores por $basisLabel", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "${macroValue(product.macrosPer100.kcal)} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (product.macrosPer100.isEmpty) {
                Text(
                    text = "No hay datos de macros para este producto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                MacroBars(
                    macros = product.macrosPer100,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            if (product.incomplete) {
                Text(
                    text = "Datos incompletos. Puedes completarlos dando de alta el producto de nuevo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Text(
                text = "Fuente: ${sourceLabel(product.source)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

private fun sourceLabel(source: DataSource): String = when (source) {
    DataSource.OPEN_FOOD_FACTS -> "Open Food Facts"
    DataSource.MANUAL -> "Alta manual"
    DataSource.RECEIPT -> "Ticket"
}
