package edu.gvsu.art.gallery.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRScanner(val callback: QRCodeFoundCallback) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
            )
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)

        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.let { qrCode ->
                        Log.d("URL scanned", qrCode.url?.url.toString())
                        qrCode.url?.url?.let { callback(it) }
                    }
                }.addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}

typealias QRCodeFoundCallback = (qrCodeURL: String) -> Unit
