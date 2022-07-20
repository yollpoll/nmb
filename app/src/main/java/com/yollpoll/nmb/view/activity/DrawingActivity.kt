package com.yollpoll.nmb.view.activity

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.permissionx.guolindev.PermissionX
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.dispatch.StartType
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityDrawingBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_DRAW
import com.yollpoll.nmb.view.widgets.*
import com.yollpoll.utils.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Created by 鹏祺 on 2017/6/19.
 * 涂鸦板
 */
const val REQUEST_DRAWING = 1214342
suspend fun gotoDrawing(context: Activity) {
//    DrawingActivity2.gotoDrawingActivity(context)
    val req = DispatchRequest.RequestBuilder().host("nmb").module("draw").startType(
        StartType.START_FOR_RESULT,
        REQUEST_DRAWING
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

@AndroidEntryPoint
@Route(url = ROUTE_DRAW)
class DrawingActivity : NMBActivity<ActivityDrawingBinding, DrawingVm>() {
    private var cameraUri: Uri? = null
    private val vm: DrawingVm by viewModels()
    override fun getLayoutId() = R.layout.activity_drawing

    override fun initViewModel() = vm

    override fun getMenuLayout() = R.menu.menu_drawer
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_drawer, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_submit -> {
                AlertDialog.Builder(this).setMessage(R.string.sure_submit)
                    .setPositiveButton(
                        "提交",
                        DialogInterface.OnClickListener { dialog, which -> submit() })
                    .setNegativeButton(
                        "取消",
                        DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    .create().show()
                true
            }
            R.id.menu_save -> {
                save()
                true
            }
            R.id.menu_set_bg -> {
                setBackground()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_PHOTO -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val uri = data?.data
                if (null == uri) {
                    "获取图片失败".shortToast()
                    return
                }
                lifecycleScope.launch {
                    val bitmap = withContext(Dispatchers.IO) {
                        val path = getPathByUri(uri, context)
                        return@withContext compressBitmap(path, 2048, 2048)
                    }
                    mDataBinding.drawView.setBackGround(bitmap)
                }
            }
            REQ_CAMERA -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(cameraUri, "image/*")
                intent.putExtra("scale", true)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
                this.startActivityForResult(intent, REQ_CROP_PHOTO)
            }
            REQ_CROP_PHOTO -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                if (null == cameraUri) {
                    "图片裁剪失败".shortToast()
                    return
                }
                lifecycleScope.launch {
                    val bitmap = withContext(Dispatchers.IO) {
                        val path = getPathByUri(cameraUri!!, context)
                        return@withContext compressBitmap(path, 2048, 2048)
                    }
                    mDataBinding.drawView.setBackGround(bitmap)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        initTitle(mDataBinding.headerTitle.toolbar, true)
        mDataBinding.fabMenu.setOnLongClickListener {
            if (mDataBinding.headerTitle.toolbar.visibility == View.VISIBLE) {
                dismissToolbar()
            } else {
                showToolbar()
            }
            true
        }
        mDataBinding.drawView.setCleanModeListener(onCleanModeChangerListener)
    }

    fun myFinish() {
        AlertDialog.Builder(this@DrawingActivity).setMessage(R.string.sure_exit)
            .setPositiveButton(
                "退出",
                DialogInterface.OnClickListener { dialog, which -> this@DrawingActivity.finish() })
            .setNegativeButton(
                "取消",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }).create()
            .show()
    }

    private fun initData() {
    }

    var red = 0
    var blue = 0
    var green = 0
    var rlColor: RelativeLayout? = null
    fun changeColor() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_choose_color)
        val window = dialog.window
        val layoutParams = window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = layoutParams
        dialog.show()
        val sbRed = dialog.findViewById<View>(R.id.seek_red) as SeekBar
        val sbGreen = dialog.findViewById<View>(R.id.seek_green) as SeekBar
        val sbBlue = dialog.findViewById<View>(R.id.seek_blue) as SeekBar
        val tvBrush = dialog.findViewById<View>(R.id.tv_brush) as TextView
        val tvBg = dialog.findViewById<View>(R.id.tv_bg) as TextView
        rlColor = dialog.findViewById<View>(R.id.rl_color) as RelativeLayout
        rlColor!!.setBackgroundColor(Color.rgb(red, green, blue))
        sbRed.progress = red * 100 / 255
        sbGreen.progress = green * 100 / 255
        sbBlue.progress = blue * 100 / 255
        tvBrush.setOnClickListener {
            mDataBinding.drawView.setPaintColor(Color.rgb(red, green, blue))
            dialog.dismiss()
        }
        tvBg.setOnClickListener {
            mDataBinding.drawView.setBackGround(Color.rgb(red, green, blue))
            dialog.dismiss()
        }
        sbRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                red = progress * 255 / 100
                rlColor!!.setBackgroundColor(Color.rgb(red, green, blue))
                //                imgCache.setImageBitmap( mDataBinding.drawView.getBitmapCache());
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sbGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                green = progress * 255 / 100
                rlColor!!.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sbBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blue = progress * 255 / 100
                rlColor!!.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun submit() {
        "提交".shortToast()

        val fileName = "draw_" + System.currentTimeMillis() + ".jpg"
        PermissionX.init(this@DrawingActivity).permissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
            .onExplainRequestReason { scope, deniedList ->
                val message = "需要您同意以下权限才能正常使用"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }.request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    lifecycleScope.launch {
                        val path = saveBitmapTpMediaStore(
                            mDataBinding.drawView.bitmapCache,
                            fileName,
                            context
                        )
                        val intent: Intent = intent
                        intent.putExtra("path", path)
                        setResult(RESULT_OK, intent)
                        this@DrawingActivity.finish()
                    }
                } else {
                    "您拒绝了如下权限：$deniedList".shortToast()
                }
            }
    }

    private fun save() {
        val fileName = "draw_" + System.currentTimeMillis() + ".jpg"
        PermissionX.init(this@DrawingActivity).permissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
            .onExplainRequestReason { scope, deniedList ->
                val message = "需要您同意以下权限才能正常使用"
                scope.showRequestReasonDialog(deniedList, message, "Allow", "Deny")
            }.request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    lifecycleScope.launch {
                        val path=saveBitmapTpMediaStore(mDataBinding.drawView.bitmapCache, fileName, context)
                        if(path.isNotEmpty()){
                            "保存成功".shortToast()
                        }else{
                            "保存失败".shortToast()
                        }
                    }
                } else {
                    "您拒绝了如下权限：$deniedList".shortToast()
                }
            }

    }

    private fun setBackground() {
        showChoosePicDialog(this) {
            this.cameraUri = it
        }
    }

    fun changeWidth() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_brush_width)
        val window = dialog.window
        val layoutParams = window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = layoutParams
        dialog.show()
        val viewLine: ChangeBurshWidthView =
            dialog.findViewById<View>(R.id.view_show_line) as ChangeBurshWidthView
        val seekWidth = dialog.findViewById<View>(R.id.seek_width) as SeekBar
        seekWidth.progress = mDataBinding.drawView.getPaintWidth()
        viewLine.setWidth(mDataBinding.drawView.getPaintWidth())
        dialog.setOnDismissListener { mDataBinding.drawView.setPaintWidth(seekWidth.progress) }
        seekWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                viewLine.setWidth(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun clean() {
        if (mDataBinding.drawView.isCleanMode()) {
            mDataBinding.drawView.cancelCleanMode()
        } else {
            mDataBinding.drawView.setCleanMode()
        }
    }

    //    private var imgCleaner: ImageView? = null
    private val onCleanModeChangerListener: DrawView.OnCleanModeChangerListener =
        DrawView.OnCleanModeChangerListener { isCleanMode ->
            if (isCleanMode) {
                mDataBinding.actionCleaner.setIcon(R.mipmap.icon_cleaner_fill)
            } else {
                mDataBinding.actionCleaner.setIcon(R.mipmap.icon_cleaner)
            }
        }

    fun clear() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.sure_clear)
            .setPositiveButton(
                "清空",
                DialogInterface.OnClickListener { dialog, which -> mDataBinding.drawView.clear() })
            .setNegativeButton(
                "取消",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }).create()
            .show()
    }

    var fullScreen = false
    fun fullScreen() {
        if (fullScreen) {
            showToolbar()
        } else {
            dismissToolbar()
        }
        fullScreen = !fullScreen
    }

    private fun dismissToolbar() {
        mDataBinding.actionFullScreen.setIcon(R.mipmap.ic_full_screen_selected)
        val size = (0 - resources.getDimension(R.dimen.height_toolbar))
        val animation = TranslateAnimation(0f, 0f, 0f, size)
        animation.duration = 300
        animation.fillAfter = true
        animation.interpolator = AccelerateDecelerateInterpolator()
        mDataBinding.headerTitle.toolbar.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                mDataBinding.headerTitle.toolbar.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    private fun showToolbar() {
        mDataBinding.actionFullScreen.setIcon(R.mipmap.ic_full_screen)
        val size = (0 - getResources().getDimension(R.dimen.height_toolbar)).toInt()
        val animation = TranslateAnimation(0f, 0f, size.toFloat(), 0f)
        animation.duration = 300
        animation.fillAfter = true
        animation.interpolator = AccelerateDecelerateInterpolator()
        mDataBinding.headerTitle.toolbar.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                mDataBinding.headerTitle.toolbar.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
    }


}

@HiltViewModel
class DrawingVm @Inject constructor(val app: Application) : FastViewModel(app) {

}