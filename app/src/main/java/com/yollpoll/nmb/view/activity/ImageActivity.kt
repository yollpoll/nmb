package com.yollpoll.nmb.view.activity

import android.app.Activity
import android.app.Application
import android.content.Context
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
import com.bumptech.glide.request.target.Target
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
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ImagePagerAdapter
import com.yollpoll.nmb.databinding.ActivityImageBinding
import com.yollpoll.nmb.databinding.FragmentImageBinding
import com.yollpoll.nmb.net.imgUrl
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_IMAGE
import com.yollpoll.utils.ImageDownloader
import com.yollpoll.utils.getCurrentDate
import com.yollpoll.utils.saveImageToMediaStore
import com.yollpoll.utils.share
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
@Route(url = ROUTE_IMAGE)
class ImageActivity : NMBActivity<ActivityImageBinding, ImageVm>() {
    companion object {
        suspend fun gotoImageActivity(
            context: Context,
            cur: Int,
            imgNames: List<String>,
            urls: List<String>
        ) {
            val req = DispatchRequest.RequestBuilder().host("nmb").module("img").params(
                hashMapOf(
                    "names" to URLEncoder.encode(imgNames.toListJson(), "UTF-8"),
                    "urls" to URLEncoder.encode(urls.toListJson(), "UTF-8"),
                    "cur" to cur.toString()
                )
            ).build()
            DispatchClient.manager?.dispatch(context, req)
        }
    }

    val vm: ImageVm by viewModels()

    @Extra
    var urls: String = ""

    @Extra
    var cur: String = "0"//在数组中的位置

    @Extra
    var names: String = ""

    private val onPageChangeListener: OnPageChangeListener by lazy {
        OnPageChangeListener {
            vm.cur = it + 1
        }
    }

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
                        vm.downloadImg()
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
        mDataBinding.viewpager.unregisterOnPageChangeCallback(onPageChangeListener)
    }

    private fun initData() {
        vm.initData(urls, names, cur.toInt() + 1)
        val adapter = ImagePagerAdapter(vm.imageList, this)
        mDataBinding.viewpager.adapter = adapter
        mDataBinding.viewpager.setCurrentItem(cur.toInt(), false)
        mDataBinding.viewpager.registerOnPageChangeCallback(onPageChangeListener)
    }

    private fun initView() {
//        mDataBinding.viewpager.requestDisallowInterceptTouchEvent(true)
//        mDataBinding.viewpager.setPageTransformer(ZoomOutPageTransformer())
        initTitle(mDataBinding.layoutHead.toolbar, showBackBtn = true) {
            this.finish()
        }
    }


}

@HiltViewModel
class ImageVm @Inject constructor(val app: Application) : FastViewModel(app) {
    @Bindable
    val title = "图片浏览"

    @Bindable
    var subTitle = "1/1"
        get() {
            return cur.toString() + "/" + imageList.size
        }

    @Bindable
    var cur: Int = 1
        //显示用的序号
        set(value) {
            field = value
            notifyChange()
        }
    val imageList = arrayListOf<String>()
    private val nameList = arrayListOf<String>()
    private val localUri = hashMapOf<String, String>()//本地的保存路径
    fun initData(json: String, names: String, cur: Int) {
        if (json.isEmpty()) return
        this.cur = cur
        URLDecoder.decode(json, "UTF-8").toListBean<String>()?.let {
            imageList.addAll(it)
        }
        URLDecoder.decode(names, "UTF-8").toListBean<String>()?.let {
            nameList.addAll(it)
        }
    }

    suspend fun downloadImg(): String = withContext(Dispatchers.IO) {
        val path = saveImageToMediaStore(
            imgUrl + imageList[cur - 1],
            nameList[cur - 1],
            app
        )
        localUri[imgUrl + imageList[cur - 1]] = path
        return@withContext path
    }


    fun share(context: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUri[imgUrl + imageList[cur - 1]]?.let {
                //这张图片已经保存在本地了
                share("匿名版", it, context)
            } ?: run {
                val path = downloadImg()
                localUri[imgUrl + imageList[cur - 1]] = path
                share("匿名版", path, context)
            }
        }
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
        Glide.with(requireContext()).asBitmap().load(imgUrl + url)
            .into(mDataBinding.ivContent)
    }
}

class ImageFragmentVM(app: Application) : FastViewModel(app) {

}
