package com.yollpoll.nmb.view.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.Bindable
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ContentView
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.base.MyDiff
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logI
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toListBean
import com.yollpoll.framework.extensions.toListJson
import com.yollpoll.framework.fast.FastFragment
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.utils.getBoolean
import com.yollpoll.nmb.*
import com.yollpoll.nmb.adapter.ImagePagerAdapter
import com.yollpoll.nmb.adapter.ThreadImagePagerAdapter
import com.yollpoll.nmb.databinding.ActivityImageBinding
import com.yollpoll.nmb.databinding.ActivityThreadImageBinding
import com.yollpoll.nmb.databinding.FragmentImageBinding
import com.yollpoll.nmb.model.bean.ImgTuple
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.net.imgUrl
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_IMAGE
import com.yollpoll.nmb.router.ROUTE_THREAD_IMAGE
import com.yollpoll.utils.saveImageToMediaStore
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

suspend fun gotoThreadImageActivity(context: Context, cur: Int, articleId: String) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("thread_img").params(
        hashMapOf(
            "id" to articleId,
            "cur" to cur.toString()
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

@AndroidEntryPoint
@Route(url = ROUTE_THREAD_IMAGE)
class ThreadImageActivity : NMBActivity<ActivityThreadImageBinding, ThreadImageVm>() {
    val vm: ThreadImageVm by viewModels()

    @Extra
    var id: String = ""

    @Extra
    var cur: String = "0"//在数组中的位置

    val adapter by lazy {
        return@lazy ThreadImagePagerAdapter(this)
    }


    private val onPageChangeListener: OnPageChangeListener by lazy {
        OnPageChangeListener {
            vm.cur = it
            vm.curImage = adapter.imageList[it]
        }
    }

    override fun getMenuLayout() = R.menu.menu_img
    override fun getLayoutId() = R.layout.activity_thread_image
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

    override fun onResume() {
        super.onResume()
        vm.loadDataToCache()
    }

    override fun onStop() {
        super.onStop()
        vm.cancelLoadData()
    }


    private fun initData() {
        lifecycleScope.launch {
            vm.initData(id, cur.toInt())
            mDataBinding.viewpager.adapter = adapter
            mDataBinding.viewpager.registerOnPageChangeCallback(onPageChangeListener)
            vm.imgFlow.collectLatest {
                vm.allSize = it.size
                adapter.submitData(it)
                mDataBinding.viewpager.setCurrentItem(vm.cur, false)
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
class ThreadImageVm @Inject constructor(
    private val app: Application,
    private val articleRepository: ArticleDetailRepository
) : FastViewModel(app) {
    private val opThumbBigImg = getBoolean(KEY_BIG_IMG, false)
    private val imgHead = if (opThumbBigImg) imgThumbUrl else imgUrl

    var allSize: Int = 1
        set(value) {
            field = value
            notifyChange()
        }

    @Bindable
    val title = "图片浏览"

    @Bindable
    var subTitle = "1/1"
        get() {
            return "${cur + 1}/$allSize"
        }

    @Bindable
    var cur: Int = 1
        //显示用的序号
        set(value) {
            field = value
            notifyChange()
        }

    //当前浏览的图片
    lateinit var curImage: ImgTuple

    lateinit var threadId: String

    val imgFlow by lazy {
        return@lazy articleRepository.getImagesFlow(id = threadId)
    }

    private val localUri = hashMapOf<String, String>()//本地的保存路径

    fun initData(id: String, cur: Int) {
        this.cur = cur
        this.threadId = id
        loadDataToCache()
    }

    suspend fun downloadImg(): String = withContext(Dispatchers.IO) {
        val path = saveImageToMediaStore(
            getImgUrl(imgHead + curImage.img + curImage.ext),
            curImage.img + curImage.ext,
            app
        )
        localUri[imgHead + curImage.img + curImage.ext] = path
        return@withContext path
    }


    fun share(context: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            localUri[imgHead + curImage.img + curImage.ext]?.let {
                //这张图片已经保存在本地了
                com.yollpoll.utils.share("匿名版", it, context)
            } ?: run {
                val path = downloadImg()
                localUri[imgHead + curImage.img + curImage.ext] = path
                com.yollpoll.utils.share("匿名版", path, context)
            }
        }
    }

    //记载数据到本地数据库
    fun loadDataToCache() {
        viewModelScope.launch {
            FlowEventBus.post(ACTION_UPDATE_THREAD_DETAIL, threadId)
        }
    }

    //取消加载
    fun cancelLoadData() {
        viewModelScope.launch {
            "ThreadReply cancel: $threadId".logI()
            FlowEventBus.post(ACTION_UPDATE_THREAD_DETAIL_CANCEL, threadId)
        }
    }
}