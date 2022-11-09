package com.yollpoll.qrlib

import android.content.Context
import android.media.Image
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Deferred
import java.lang.Exception

/**
 * Created by spq on 2022/11/8
 */
fun analysisQr(
    uri: Uri,
    context: Context,
    onResult: ((List<Barcode>) -> Unit)? = null,
    onErr: ((Exception) -> Unit)?,
    onComplete: (() -> Unit)?
) {
    //配置当前扫码格式
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC
        ).build()

    //获取解析器
    val detector = BarcodeScanning.getClient(options)
    val image = InputImage.fromFilePath(context, uri)
    detector.process(image)
        .addOnSuccessListener {
            onResult?.invoke(it)
        }
        .addOnFailureListener {
            onErr?.invoke(it)
        }
        .addOnCompleteListener {
            onComplete?.invoke()
        }
}