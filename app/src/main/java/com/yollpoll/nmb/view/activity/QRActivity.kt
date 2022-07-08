package com.yollpoll.nmb.view.activity

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.activity.viewModels
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityQrBinding
import com.yollpoll.nmb.router.ROUTE_QR
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@AndroidEntryPoint
@Route(url = ROUTE_QR)
class QRActivity : NMBActivity<ActivityQrBinding, QRVm>() {
    private lateinit var lifecycleCameraController: LifecycleCameraController
    private val vm: QRVm by viewModels()
    override fun getLayoutId() = R.layout.activity_qr
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
//        lifecycleCameraController = LifecycleCameraController(this)
//        lifecycleCameraController.bindToLifecycle(this)
//        lifecycleCameraController.imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO
//        lifecycleCameraController.setImageAnalysisAnalyzer(
//            cameraExecutor,
//            QRCodeAnalyser { barcodes, imageWidth, imageHeight ->
//                if (barcodes.isEmpty()) {
//                    return@QRCodeAnalyser
//                }
//                initScale(imageWidth, imageHeight)
//                val list = ArrayList<RectF>()
//                val strList = ArrayList<String>()
//
//                barcodes.forEach { barcode ->
//                    barcode.boundingBox?.let { rect ->
//                        val translateRect = translateRect(rect)
//                        list.add(translateRect)
//                        "left：${translateRect.left}  +" +
//                                "  top：${translateRect.top}  +  right：${translateRect.right}" +
//                                "  +  bottom：${translateRect.bottom}".logI()
//
//                        "barcode.rawValue：${barcode.rawValue}".logI()
//                        strList.add(barcode.rawValue ?: "No Value")
//                    }
//                }
//                judgeIntent(strList)
//                mDataBinding.scanView.setRectList(list)
//
//            })
//        mDataBinding.previewView.controller = lifecycleCameraController
    }

}

@HiltViewModel
class QRVm @Inject constructor(app: Application) : FastViewModel(app) {

}

class QRCodeAnalyser(private val listener: (List<Barcode>, Int, Int) -> Unit) :
    ImageAnalysis.Analyzer {
    //配置当前扫码格式
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC
        ).build()

    //获取解析器
    private val detector = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: kotlin.run {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { barCodes ->
                "barCodes: ${barCodes.size}".logE()
                if (barCodes.size > 0) {
                    listener.invoke(barCodes, imageProxy.width, imageProxy.height)
                    //接收到结果后，就关闭解析
                    detector.close()
                }
            }
            .addOnFailureListener { "Error: ${it.message}".logE() }
            .addOnCompleteListener { imageProxy.close() }
    }
}