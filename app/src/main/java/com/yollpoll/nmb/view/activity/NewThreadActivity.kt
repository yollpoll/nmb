package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.Bindable
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.startActivityForResult
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityNewthreadBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_NEW_THREAD
import com.yollpoll.nmb.view.widgets.REQ_CAMERA
import com.yollpoll.nmb.view.widgets.REQ_CROP_PHOTO
import com.yollpoll.nmb.view.widgets.REQ_PHOTO
import com.yollpoll.nmb.view.widgets.emoji.ChooseEmojiDialogFragment
import com.yollpoll.nmb.view.widgets.showChoosePicDialog
import com.yollpoll.utils.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ACTION_NEW = "new"
const val ACTION_REPLAY = "replay"
suspend fun gotoNewThreadActivity(context: Context) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("new_thread").params(
        hashMapOf(
            "action" to ACTION_NEW
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

suspend fun gotoRelyThreadActivity(context: Context) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("new_thread").params(
        hashMapOf(
            "action" to ACTION_REPLAY
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

@AndroidEntryPoint
@Route(url = ROUTE_NEW_THREAD)
class NewThreadActivity : NMBActivity<ActivityNewthreadBinding, NewThreadVm>() {
    private var cameraUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_PHOTO -> {
                //相册
                if (resultCode != RESULT_OK) {
                    return
                }
                val uri = data?.data
                if (null == uri) {
                    "获取图片失败".shortToast()
                    return
                }
                vm.selectPhoto(uri)
            }
            REQ_CAMERA -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(cameraUri, "image/*")
                intent.putExtra("scale", true)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
                this@NewThreadActivity.startActivityForResult(intent, REQ_CROP_PHOTO)
            }
            REQ_CROP_PHOTO -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                if (null == cameraUri) {
                    "图片裁剪失败".shortToast()
                    return
                }
                vm.selectPhoto(cameraUri!!)
            }
        }
    }

    @Extra
    lateinit var action: String
    private val vm: NewThreadVm by viewModels()
    override fun getLayoutId() = R.layout.activity_newthread
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true)
        when (action) {
            ACTION_NEW -> {
                vm.title = "新串"
            }
            ACTION_REPLAY -> {
                vm.title = "回复"
            }
        }
        vm.selectedImg.observe(this) {
            it?.let {
                mDataBinding.ivSelected.visibility = View.VISIBLE
                mDataBinding.ivSelected.setImageBitmap(it)
            } ?: kotlin.run {
                mDataBinding.ivSelected.visibility = View.GONE
            }
        }
    }

    //选择表情
    fun chooseEmoji() {
        ChooseEmojiDialogFragment { word: String?, id: Int, fragment: DialogFragment? ->
            if (word?.isEmpty() != false) {
                fragment?.dismiss()
                //选择了图片
                if (id == 0) return@ChooseEmojiDialogFragment
                lifecycleScope.launch(Dispatchers.IO) {
                    val cacheBitmap = BitmapFactory.decodeResource(resources, id)
                    vm.selectImg(cacheBitmap)
                }
            } else {
                //选择了文字
                vm.threadContent += word
                fragment?.dismiss()
            }
        }.show(supportFragmentManager, "chooseEmoji")
    }

    //选择图片
    fun choosePic() {
        showChoosePicDialog(this) {
            this.cameraUri = it
        }
    }


}

@HiltViewModel
class NewThreadVm @Inject constructor(val app: Application) : FastViewModel(app) {
    //当前显示的图片
//    var selectPicBitMap: Bitmap? = null
//        set(value) {
//            field = value
//
//        }
    private val selectedImgFlow = MutableStateFlow<Bitmap?>(null)
    val selectedImg = selectedImgFlow.asLiveData()

    @Bindable
    var title: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }

    @Bindable
    var threadTitle: String = "无标题"
        set(value) {
            field = value
            notifyPropertyChanged(BR.threadTitle)
        }

    @Bindable
    var threadContent: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.threadContent)
        }

    fun submit() {
        if (threadContent.isEmpty())
            "文本内容为空".shortToast()
        "Title is $threadTitle content is $threadContent".logI()
    }

    //选择照片
    fun selectPhoto(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            "img uri:${uri.path}".logI()
            val path = getPathByUri(uri, app)
            val bitmap = compressBitmap(path, 2048, 2048)
            selectImg(bitmap)
        }
    }

    suspend fun selectImg(bitmap: Bitmap?) {
        selectedImgFlow.emit(bitmap)
    }
}