package com.foodmaster.app.meallog

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foodmaster.app.domain.model.MealLog
import com.foodmaster.app.domain.model.MealLogItem
import com.foodmaster.app.scanner.CameraPreview
import com.foodmaster.app.ui.display
import com.foodmaster.app.ui.formatPrice
import com.foodmaster.app.ui.summaryLine
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.time.format.DateTimeFormatter

private val mealDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

/**
 * Meal log ("registro de comidas"). The user builds a meal by scanning products
 * and/or adding barcode-less items by hand, sees the running macros + price, and
 * saves it. Past meals are listed below.
 */
@Composable
fun MealLogScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: MealLogViewModel = viewModel(factory = MealLogViewModel.Factory),
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    var building by rememberSaveable { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var mealName by rememberSaveable { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        if (building) {
            MealBuilder(
                items = draft,
                totals = viewModel.draftTotals(draft),
                name = mealName,
                onNameChange = { mealName = it },
                onScan = { scanning = true },
                onAddManual = { showManualDialog = true },
                onRemove = viewModel::removeDraftItem,
                onSave = {
                    viewModel.saveDraft(mealName)
                    mealName = ""
                    building = false
                },
                onCancel = {
                    viewModel.clearDraft()
                    mealName = ""
                    building = false
                },
                contentPadding = contentPadding,
            )
        } else {
            MealHistory(
                meals = history,
                onStart = { building = true },
                onDelete = viewModel::deleteMeal,
                contentPadding = contentPadding,
            )
        }

        if (scanning) {
            ScanOverlay(
                viewModel = viewModel,
                onClose = {
                    scanning = false
                    viewModel.resetScan()
                },
                onSwitchToManual = {
                    scanning = false
                    viewModel.resetScan()
                    showManualDialog = true
                },
            )
        }

        if (showManualDialog) {
            ManualMealItemDialog(
                onAdd = { name, quantity, macros, price ->
                    viewModel.addManual(name, quantity, macros, price)
                    showManualDialog = false
                },
                onDismiss = { showManualDialog = false },
            )
        }
    }
}

@Composable
private fun MealBuilder(
    items: List<MealLogItem>,
    totals: MealLog,
    name: String,
    onNameChange: (String) -> Unit,
    onScan: () -> Unit,
    onAddManual: () -> Unit,
    onRemove: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre (opcional, p. ej. Desayuno)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item { TotalsCard(totals) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onScan, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Escanear")
                    }
                    OutlinedButton(onClick = onAddManual, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("A mano")
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Text(
                        text = "Aún no has añadido nada. Escanea un producto o añádelo a mano.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                itemsIndexed(items) { index, item ->
                    DraftItemRow(item = item, onRemove = { onRemove(index) })
                }
            }
        }

        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = contentPadding.calculateBottomPadding()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = onSave,
                enabled = items.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text("Guardar comida")
            }
        }
    }
}

@Composable
private fun TotalsCard(totals: MealLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total de la comida", style = MaterialTheme.typography.labelMedium)
            Text(
                text = totals.totalMacros.summaryLine(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "Precio: ${formatPrice(totals.totalPrice)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DraftItemRow(item: MealLogItem, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${item.quantity.display()} · ${item.macros.summaryLine()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.price != null) {
                    Text(
                        text = formatPrice(item.price),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Quitar")
            }
        }
    }
}

@Composable
private fun MealHistory(
    meals: List<MealLog>,
    onStart: () -> Unit,
    onDelete: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ElevatedButton(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  Registrar comida")
            }
        }

        if (meals.isEmpty()) {
            item {
                Text(
                    text = "Todavía no has registrado ninguna comida.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                )
            }
        } else {
            items(meals, key = { it.id }) { meal ->
                MealHistoryCard(meal = meal, onDelete = { onDelete(meal.id) })
            }
        }
    }
}

@Composable
private fun MealHistoryCard(meal: MealLog, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.name ?: "Comida",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = meal.dateTime.format(mealDateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Borrar")
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(meal.totalMacros.summaryLine(), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Precio: ${formatPrice(meal.totalPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            meal.items.forEach { item ->
                Text(
                    text = "· ${item.name} (${item.quantity.display()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ScanOverlay(
    viewModel: MealLogViewModel,
    onClose: () -> Unit,
    onSwitchToManual: () -> Unit,
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val scan by viewModel.scan.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermission.status.isGranted) {
            CameraPreview(
                onBarcode = { value, _ -> viewModel.onScan(value) },
                modifier = Modifier.fillMaxSize(),
            )

            if (scan is ScanLookup.Idle) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(160.dp)
                        .border(3.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "FoodMaster necesita la cámara para escanear.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { cameraPermission.launchPermissionRequest() },
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Conceder permiso") }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50)),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        when (val s = scan) {
            is ScanLookup.Loading -> Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Color.White) }

            is ScanLookup.Found -> ScanPortionDialog(
                product = s.product,
                onAdd = { quantity, price ->
                    viewModel.addScanned(s.product, quantity, price)
                    // Close the scanner (which resets the lookup) so the live camera
                    // can't re-detect and re-add the same barcode.
                    onClose()
                },
                onDismiss = viewModel::resetScan,
            )

            is ScanLookup.NotFound -> AlertDialog(
                onDismissRequest = viewModel::resetScan,
                title = { Text("Producto no encontrado") },
                text = { Text("No hay datos para el código ${s.barcode}. Puedes añadirlo a mano.") },
                confirmButton = {
                    TextButton(onClick = onSwitchToManual) { Text("Añadir a mano") }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::resetScan) { Text("Seguir escaneando") }
                },
            )

            is ScanLookup.Error -> AlertDialog(
                onDismissRequest = viewModel::resetScan,
                title = { Text("No se pudo consultar") },
                text = { Text(s.message ?: "Revisa tu conexión e inténtalo de nuevo.") },
                confirmButton = {
                    TextButton(onClick = viewModel::resetScan) { Text("Reintentar") }
                },
            )

            ScanLookup.Idle -> Unit
        }
    }
}
