package com.yollpoll.nmb.view.widgets

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.permissionx.guolindev.PermissionX
import com.yollpoll.arch.util.AppUtils.getPackageName
import com.yollpoll.base.getAttrColor
import com.yollpoll.framework.extensions.dp2px
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.nmb.R
import com.yollpoll.utils.createImageUri
import java.io.File
import java.io.IOException

/**
 * 左侧drawerlayout手势冲突处理
 */
fun DrawerLayout.initLeftDrawerLayout(context: Activity) {
    val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.windowManager.currentWindowMetrics.bounds.height()
    } else {
        context.windowManager.defaultDisplay.height
    }
    if (screenHeight == 0) {
        return
    }
    val rect = Rect(0, 0, 20, screenHeight / 2)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.systemGestureExclusionRects = listOf(rect)
    }
}

/**
 * 右侧drawelayout手势冲突处理
 */
fun DrawerLayout.initRightDrawerLayout(context: Activity) {
    val screenHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.windowManager.currentWindowMetrics.bounds.height()
    } else {
        context.windowManager.defaultDisplay.height
    }
    val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.windowManager.currentWindowMetrics.bounds.width()
    } else {
        context.windowManager.defaultDisplay.width
    }
    if (screenWidth == 0 || height == 0) {
        return
    }

    val rect = Rect(screenWidth - 20, 0, screenWidth, screenHeight / 2)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.systemGestureExclusionRects = listOf(rect)
    }
}

fun getCommonGlideOptions(context: Context): RequestOptions {
    return RequestOptions()
        .transform(
            CenterCrop(), RoundedCorners(
                context.dp2px(10f)
                    .toInt()
            )
        )
}

//初始化swiperefresh
fun SwipeRefreshLayout.init(context: Context, onRefresh: (() -> Unit)? = null) {
    this.setColorSchemeColors(
        context.getAttrColor(R.attr.colorPrimary),
        context.getAttrColor(R.attr.colorSecondary),
        context.getAttrColor(R.attr.colorTertiary),
    )
    this.setOnRefreshListener(onRefresh)
}

//viewpager2适配器
private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class ZoomOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        horzMargin + vertMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade the page relative to its size.
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}

const val REQ_PHOTO = 12345
const val REQ_CAMERA = 54321
const val REQ_CROP_PHOTO = 543210

//选择相册或者图片
fun showChoosePicDialog(
    activity: FragmentActivity,
    reqPhotoCode: Int = REQ_PHOTO,
    reqCameraCode: Int = REQ_CAMERA,
    onCamera: ((Uri) -> Unit)? = null
) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
    val view: View = LayoutInflater.from(activity).inflate(R.layout.alert_choose_photo, null)
    builder.setView(view)
    val alertDialog: AlertDialog = builder.create()
    alertDialog.show()
    val rlPhoto = view.findViewById<View>(R.id.rl_photo) as RelativeLayout
    val rlCamera = view.findViewById<View>(R.id.rl_camera) as RelativeLayout
    rlCamera.setOnClickListener {
        alertDialog.dismiss()
        PermissionX.init(activity).permissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            .onExplainRequestReason { scope, deniedList ->
                val message = "相机需要您同意以下权限才能正常使用"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }.request { allGranted, grantedList, deniedList ->
                if (allGranted) {
//                    val file = activity.externalCacheDir
//                    val imgFile = File(file, "temp.jpg")
//                    if (file!!.exists()) file.delete()
//                    try {
//                        imgFile.createNewFile()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    val uri = FileProvider.getUriForFile(
//                        activity, "com.yollpoll.nmb.fileprovider",
//                        imgFile
//                    )
                    val uri = createImageUri(activity) ?: return@request
                    onCamera?.invoke(uri)
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        uri
                    )
                    activity.startActivityForResult(
                        intent,
                        reqCameraCode
                    )
                } else {
                    "您拒绝了如下权限：$deniedList".shortToast()
                }
            }


    }

    rlPhoto.setOnClickListener {
        alertDialog.dismiss()
        PermissionX.init(activity).permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .onExplainRequestReason { scope, deniedList ->
                val message = "相册需要您同意以下权限才能正常使用"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }.request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    activity.startActivityForResult(
                        intent,
                        reqPhotoCode
                    )
                } else {
                    "您拒绝了如下权限：$deniedList".shortToast()
                }
            }
    }
}