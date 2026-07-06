package com.foodmaster.app.scanner

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.foodmaster.app.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Barcode scanner screen.
 *
 * Requests the camera permission, shows a live preview, and detects barcodes
 * automatically. On a detection it shows a message with the scanned number.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        when {
            cameraPermission.status.isGranted -> {
                ScannerContent(onClose = onClose)
            }

            else -> {
                PermissionRequest(
                    onRequest = { cameraPermission.launchPermissionRequest() },
                    onClose = onClose,
                )
            }
        }
    }
}

@Composable
private fun ScannerContent(
    onClose: () -> Unit,
) {
    // Holds the most recent detection; while non-null we pause acting on new frames
    // and show the result message.
    var detectedCode by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onBarcode = { value, _ ->
                if (detectedCode == null) {
                    detectedCode = value
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Framing guide + hint.
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

        detectedCode?.let { code ->
            BarcodeResultDialog(
                code = code,
                onScanAgain = { detectedCode = null },
                onDone = onClose,
            )
        }
    }
}

@Composable
private fun BarcodeResultDialog(
    code: String,
    onScanAgain: () -> Unit,
    onDone: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onScanAgain,
        title = { Text(stringResource(R.string.barcode_detected)) },
        text = {
            Text(
                text = code,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
        },
        confirmButton = {
            TextButton(onClick = onScanAgain) {
                Text(stringResource(R.string.scan_again))
            }
        },
        dismissButton = {
            TextButton(onClick = onDone) {
                Text(stringResource(R.string.ok))
            }
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
                // Deep-link to app settings for the "denied permanently" case.
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
