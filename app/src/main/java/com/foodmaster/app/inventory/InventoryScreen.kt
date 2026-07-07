package com.foodmaster.app.inventory

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodmaster.app.consume.ConsumeDialog
import com.foodmaster.app.domain.model.InventoryItem
import com.foodmaster.app.ui.display
import com.foodmaster.app.ui.label

@Composable
fun InventoryScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = viewModel(factory = InventoryViewModel.Factory),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var consuming by remember { mutableStateOf<InventoryItem?>(null) }

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(contentPadding).padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Tu inventario está vacío.\nEscanea un producto y añádelo.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items, key = { it.id }) { item ->
                InventoryRow(
                    item = item,
                    onConsume = { consuming = item },
                    onDelete = { viewModel.delete(item) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }

    consuming?.let { item ->
        ConsumeDialog(
            product = item.product,
            stock = item.quantity,
            onConfirm = { amount ->
                viewModel.consume(item, amount)
                consuming = null
            },
            onDismiss = { consuming = null },
        )
    }
}

@Composable
private fun InventoryRow(
    item: InventoryItem,
    onConsume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.quantity.display(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(onClick = {}, label = { Text(item.location.label()) })
                    item.expirationDate?.let { date ->
                        AssistChip(onClick = {}, label = { Text("Cad. ${date.display()}") })
                    }
                }
            }
            IconButton(onClick = onConsume) {
                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Consumir")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
            }
        }
    }
}
