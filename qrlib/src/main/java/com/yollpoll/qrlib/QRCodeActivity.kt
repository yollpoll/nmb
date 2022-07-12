package com.yollpoll.qrlib

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.qrlib.databinding.ActivityQrCodeBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.log

/**
 * date:2021/6/18
 * author:zhangteng
 * description:二维码扫描
 */
const val ROUTE_QR = "native://qrlib?module=qrcode"

@AndroidEntryPoint
@Route(url = ROUTE_QR)
class QRCodeActivity : NMBActivity<ActivityQrCodeBinding, QrVm>() {
    val vm: QrVm by viewModels()
    private lateinit var lifecycleCameraController: LifecycleCameraController
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initController()
    }

    @SuppressLint("ClickableViewAccessibility", "UnsafeOptInUsageError")
    private fun initController() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        lifecycleCameraController = LifecycleCameraController(this)
        lifecycleCameraController.bindToLifecycle(this)
        lifecycleCameraController.imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO
        lifecycleCameraController.setImageAnalysisAnalyzer(
            cameraExecutor,
            QRCodeAnalyser { barcodes, imageWidth, imageHeight ->
                if (barcodes.isEmpty()) {
                    return@QRCodeAnalyser
                }
                initScale(imageWidth, imageHeight)
                val list = ArrayList<RectF>()
                val strList = ArrayList<String>()

                barcodes.forEach { barcode ->
                    barcode.boundingBox?.let { rect ->
                        val translateRect = translateRect(rect)
                        list.add(translateRect)
                        Log.e(
                            "ztzt", "left：${translateRect.left}  +" +
                                    "  top：${translateRect.top}  +  right：${translateRect.right}" +
                                    "  +  bottom：${translateRect.bottom}"
                        )
                        Log.e("ztzt", "barcode.rawValue：${barcode.rawValue}")
                        strList.add(barcode.rawValue ?: "No Value")
                    }
                }
                judgeIntent(strList)
                mDataBinding.scanView.setRectList(list)

            })
        mDataBinding.previewView.controller = lifecycleCameraController
    }

    private fun judgeIntent(list: ArrayList<String>) {
        val sb = StringBuilder()
        list.forEach {
            sb.append(it)
            sb.append("\n")
        }
        intentToResult(sb.toString())
    }

    private fun intentToResult(result: String) {
        result.replace("\"", "\\\"")
//        "cookie:? {\"cookie\":\"%07R5%89%FB%CE%9F%CC%DF%E4%2A%0B6%816%9D%B1%0E%CA%BBG%60%95%9A\",\"name\":\"ODFaQ9k\"}".logE()
        intent.putExtra("cookie", result)
        setResult(RESULT_OK, intent)
        this.finish()
    }

    private var scaleX = 0f
    private var scaleY = 0f

    private fun translateX(x: Float): Float = x * scaleX
    private fun translateY(y: Float): Float = y * scaleY

    //将扫描的矩形换算为当前屏幕大小
    private fun translateRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    //初始化缩放比例
    private fun initScale(imageWidth: Int, imageHeight: Int) {
        Log.e("ztzt", "imageWidth：${imageWidth} + imageHeight：${imageHeight}")
        scaleY = mDataBinding.scanView.height.toFloat() / imageWidth.toFloat()
        scaleX = mDataBinding.scanView.width.toFloat() / imageHeight.toFloat()
        Log.e("ztzt", "scaleX：${scaleX} + scaleY：${scaleY}")
    }

    override fun getLayoutId() = R.layout.activity_qr_code

    override fun initViewModel() = vm
}

@HiltViewModel
class QrVm @Inject constructor(app: Application) : FastViewModel(app)