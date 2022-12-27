package com.yollpoll.nmb.view.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.Bindable
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ContentView
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toListBean
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.fast.FastFragment
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.Iqr
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ImagePagerAdapter
import com.yollpoll.nmb.databinding.ActivityImageBinding
import com.yollpoll.nmb.databinding.FragmentImageBinding
import com.yollpoll.nmb.net.imgUrl
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_IMAGE
import com.yollpoll.utils.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Serializable
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

const val ACTION_FROM_URL = "from_url"
const val ACTION_FROM_PATH = "from_path"
const val ACTION_FROM_RESOURCE = "from_resource"
suspend fun gotoImageActivityByUrl(context: Activity, url: String) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("img").params(
        hashMapOf(
            "url" to URLEncoder.encode(url, "UTF-8"), "action" to ACTION_FROM_URL
        )
    ).build()
    DispatchClient.manager.dispatch(context, req)
}

suspend fun gotoImageActivityByFile(context: Activity, path: String) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("img").params(
        hashMapOf(
            "path" to path, "action" to ACTION_FROM_PATH
        )
    ).build()
    DispatchClient.manager.dispatch(context, req)
}

suspend fun gotoImageByResource(context: Context, resourceId: Int) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("img").params(
        hashMapOf(
            "action" to ACTION_FROM_RESOURCE, "resourceId" to resourceId.toString()
        )
    ).build()
    DispatchClient.manager.dispatch(context, req)

}

@AndroidEntryPoint
@Route(url = ROUTE_IMAGE)
class ImageActivity : NMBActivity<ActivityImageBinding, ImageVm>() {
    val vm: ImageVm by viewModels()

    @Extra
    var action: String = ACTION_FROM_URL

    @Extra
    var url: String = ""

    @Extra
    var path: String = ""

    @Extra
    var resourceId: String = "0"


    override fun getMenuLayout() = R.menu.menu_img
    override fun getLayoutId() = R.layout.activity_image
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
        initView()
        initData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_download -> {
                //下载图片
                lifecycleScope.launch {
                    try {
                        if (action == ACTION_FROM_RESOURCE) {
                            if (resourceId == "0")
                                return@launch
                            val bitmap =
                                BitmapFactory.decodeResource(context.resources, resourceId.toInt())
                            saveBitmapToSd(
                                bitmap, "donate.png", context.filesDir.absolutePath
                            )
                            bitmap.recycle()
                        } else {
                            vm.downloadImg()
                        }
                        "保存成功".shortToast()
                    } catch (e: Exception) {
                        "保存失败".shortToast()
                    }
                }
                return true
            }
            R.id.action_share -> {
                vm.share(context)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initData() {
        when (action) {
            ACTION_FROM_URL -> {
                Glide.with(context).load(url).into(mDataBinding.ivContent)
                vm.initData(url, path, action)
            }
            ACTION_FROM_PATH -> {
                Glide.with(context).load(File(path)).into(mDataBinding.ivContent)
                vm.initData(url, path, action)
            }
            ACTION_FROM_RESOURCE -> {
                if (resourceId != "0") {
//                    mDataBinding.ivContent.setImageResource(resourceId.toInt())
                    mDataBinding.ivContent.setImageResource(R.mipmap.ic_donate)
                }
                vm.initData(url, path, action)
            }
        }
    }

    private fun initView() {
        initTitle(mDataBinding.layoutHead.toolbar, showBackBtn = true) {
            this.finish()
        }
    }

}

@HiltViewModel
class ImageVm @Inject constructor(val app: Application) : FastViewModel(app) {

    @Bindable
    val title = "大图"

    @Bindable
    var subTitle = ""

    var url: String? = null

    var action: String? = null

    var path: String? = null


    fun initData(
        url: String? = null,
        path: String? = null,
        action: String? = null,
    ) {
        this.url = url
        this.path = path
        this.action = action
    }

    suspend fun downloadImg() = withContext(Dispatchers.IO) {
        when (action) {
            ACTION_FROM_URL -> {
                this@ImageVm.path = saveImageToMediaStore(
                    getImgUrl(url!!), "$url.jpg", app
                )
            }
        }
    }


    fun share(context: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            path?.let {
                //这张图片已经保存在本地了
                share("匿名版", it, context)
            } ?: run {
                downloadImg()
                share("匿名版", this@ImageVm.path!!, context)
            }
        }
    }

    fun doQr(){
//        qr.analysisQr()
    }

}

//监听页面变化
class OnPageChangeListener(private val onChange: ((Int) -> Unit)) :
    ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        onChange.invoke(position)
    }
}

@ViewModel(ImageFragmentVM::class)
@ContentView(R.layout.fragment_image)
class ImageFragment : FastFragment<FragmentImageBinding, ImageFragmentVM>() {
    @Extra
    var url = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).asBitmap().load(getImgUrl(url))
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    mDataBinding.ivContent.setImageBitmap(resource)
                    mDataBinding.progressBar.visibility = View.GONE
                }

            })


    }
}

class ImageFragmentVM(app: Application) : FastViewModel(app) {

}

fun getImgUrl(url: String): String {
    return if (url.startsWith("http")) {
        url
    } else {
        imgUrl + url
    }
}
