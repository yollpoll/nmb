package com.yollpoll.nmb

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.Barcode

interface Iqr {
    fun analysisQr(uri: Uri,
                   context: Context,
                   onResult: ((List<Barcode>) -> Unit)? = null,
                   onErr: ((Exception) -> Unit)?,
                   onComplete: (() -> Unit)?)

}