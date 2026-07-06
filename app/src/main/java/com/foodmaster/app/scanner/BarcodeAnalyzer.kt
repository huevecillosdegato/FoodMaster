package com.foodmaster.app.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX [ImageAnalysis.Analyzer] that feeds frames to ML Kit's on-device barcode
 * scanner and reports the first decoded value via [onBarcode].
 *
 * Detection is automatic — the caller only has to bind this analyzer to the camera;
 * no tap/shutter is involved. A debounce guards against firing repeatedly for the
 * same code while it stays in view.
 */
class BarcodeAnalyzer(
    private val onBarcode: (value: String, format: Int) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
            )
            .build()
    )

    // Debounce: ignore the same code if seen again within this window.
    private var lastValue: String? = null
    private var lastTimestampMs: Long = 0L
    private val debounceMs = 2_000L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val input = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )

        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val barcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                val value = barcode?.rawValue
                if (value != null && shouldEmit(value)) {
                    onBarcode(value, barcode.format)
                }
            }
            // Always close the proxy so the next frame can be delivered.
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun shouldEmit(value: String): Boolean {
        val now = System.currentTimeMillis()
        val isDuplicate = value == lastValue && (now - lastTimestampMs) < debounceMs
        lastValue = value
        lastTimestampMs = now
        return !isDuplicate
    }
}
