package com.foodmaster.app.scanner

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.foodmaster.app.R
import com.foodmaster.app.domain.model.MeasureUnit
import com.foodmaster.app.domain.model.Product
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Barcode scanner screen. Requests the camera permission, shows a live preview,
 * detects barcodes automatically, looks the product up in Open Food Facts /
 * cache, and shows a product card (or a not-found / error message).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel = viewModel(factory = ScannerViewModel.Factory),
) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermission.status.isGranted) {
            ScannerContent(viewModel = viewModel, onClose = onClose)
        } else {
            PermissionRequest(
                onRequest = { cameraPermission.launchPermissionRequest() },
                onClose = onClose,
            )
        }
    }
}

@Composable
private fun ScannerContent(
    viewModel: ScannerViewModel,
    onClose: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onBarcode = { value, _ -> viewModel.onBarcode(value) },
            modifier = Modifier.fillMaxSize(),
        )

        // Framing guide + hint (only while actively scanning).
        if (state.phase == ScanPhase.Scanning) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(
                            width = 3.dp,
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(16.dp),
                        ),
                )
                Text(
                    text = stringResource(R.string.scanner_hint),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }

        // Close button, top-start.
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50)),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.scanner_close),
                tint = Color.White,
            )
        }

        // Result surface / overlays, driven by phase.
        when (state.phase) {
            ScanPhase.Loading -> LoadingOverlay(code = state.scannedCode)

            ScanPhase.ProductFound -> state.product?.let { product ->
                ProductCard(
                    product = product,
                    onScanAgain = viewModel::scanAgain,
                    onClose = onClose,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            ScanPhase.NotFound -> InfoDialog(
                title = stringResource(R.string.product_not_found_title),
                message = stringResource(R.string.product_not_found_message, state.scannedCode.orEmpty()),
                onScanAgain = viewModel::scanAgain,
                onClose = onClose,
            )

            ScanPhase.Error -> InfoDialog(
                title = stringResource(R.string.lookup_error_title),
                message = state.error ?: stringResource(R.string.lookup_error_generic),
                onScanAgain = viewModel::scanAgain,
                onClose = onClose,
            )

            ScanPhase.Scanning -> Unit
        }
    }
}

@Composable
private fun LoadingOverlay(code: String?) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            if (code != null) {
                Text(
                    text = code,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onScanAgain: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(end = 16.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (product.brand != null) {
                        Text(
                            text = product.brand,
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            val per = if (product.servingUnit == MeasureUnit.VOLUME) {
                stringResource(R.string.per_100ml)
            } else {
                stringResource(R.string.per_100g)
            }
            Text(text = per, style = MaterialTheme.typography.labelMedium)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MacroCell(stringResource(R.string.macro_kcal), product.macrosPer100.kcal, "")
                MacroCell(stringResource(R.string.macro_protein), product.macrosPer100.protein, "g")
                MacroCell(stringResource(R.string.macro_carbs), product.macrosPer100.carbs, "g")
                MacroCell(stringResource(R.string.macro_fat), product.macrosPer100.fat, "g")
            }

            if (product.incomplete) {
                Text(
                    text = stringResource(R.string.product_incomplete),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onScanAgain,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.scan_again))
                }
                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        }
    }
}

@Composable
private fun MacroCell(label: String, value: Double?, suffix: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (value != null) "${formatMacro(value)}$suffix" else "—",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatMacro(value: Double): String {
    val rounded = Math.round(value * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

@Composable
private fun InfoDialog(
    title: String,
    message: String,
    onScanAgain: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onScanAgain,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onScanAgain) { Text(stringResource(R.string.scan_again)) }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text(stringResource(R.string.done)) }
        },
    )
}

@Composable
private fun PermissionRequest(
    onRequest: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.permission_rationale),
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRequest,
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(stringResource(R.string.permission_grant))
        }
        FilledTonalButton(
            onClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                )
                context.startActivity(intent)
            },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.open_settings))
        }
        TextButton(
            onClick = onClose,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.scanner_close))
        }
    }
}
