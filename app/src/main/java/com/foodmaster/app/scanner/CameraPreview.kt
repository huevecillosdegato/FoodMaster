package com.foodmaster.app.scanner

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

/**
 * Full-screen CameraX preview that continuously runs [BarcodeAnalyzer].
 *
 * Barcodes are detected automatically as frames arrive — the caller receives each
 * fresh detection through [onBarcode]. Backpressure uses KEEP_ONLY_LATEST so ML Kit
 * always works on the most recent frame instead of building a queue.
 */
@Composable
fun CameraPreview(
    onBarcode: (value: String, format: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Single-threaded executor dedicated to image analysis.
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    // Held so we can release the camera when this composable leaves the screen.
    val cameraProviderRef = remember { AtomicReference<ProcessCameraProvider?>() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderRef.set(cameraProvider)

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(analysisExecutor, BarcodeAnalyzer(onBarcode))
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis,
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        onRelease = {
            // Unbind the camera and release the analysis executor when the composable
            // leaves composition; otherwise the camera stays bound to the Activity
            // lifecycle and keeps running (green "camera in use" indicator) after you
            // leave the scanner.
            cameraProviderRef.getAndSet(null)?.unbindAll()
            analysisExecutor.shutdown()
        },
    )
}
