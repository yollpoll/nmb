package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.arch.util.AppUtils
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.extensions.toJsonBean
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.nmb.App
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.MR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ExampleLoadStateAdapter
import com.yollpoll.nmb.adapter.ThreadAdapter
import com.yollpoll.nmb.databinding.ActivityThreadDetailBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_THREAD_DETAIL
import com.yollpoll.nmb.view.widgets.*
import com.yollpoll.utils.copyStr
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
@Route(url = ROUTE_THREAD_DETAIL)
class ThreadDetailActivity : NMBActivity<ActivityThreadDetailBinding, ThreadDetailVM>() {
    @Extra
    lateinit var id: String

    companion object {
        suspend fun gotoThreadDetailActivity(
            id: String,
            context: Context
        ) {
            val req = DispatchRequest.RequestBuilder().host("nmb").module("thread_detail").params(
                hashMapOf("id" to id)
            ).build()
            DispatchClient.manager?.dispatch(context, req)
        }
    }

    private val vm by viewModels<ThreadDetailVM>()
    override fun getMenuLayout() = R.menu.menu_article_detail
    override fun getLayoutId() = R.layout.activity_thread_detail
    override fun initViewModel() = vm
    private val mManager = LinearLayoutManager(this)
    private val mAdapter = ThreadAdapter(false, onItemLongClick = { article ->
        ThreadMenuDialog(MenuAction.MENU_ACTION_REPLY, context, reply = {
            lifecycleScope.launchWhenResumed {
                gotoLinkActivity(context, vm.id, arrayListOf(article.id))
            }
        }, copy = {
            copyStr(context, article.content)
            "复制到剪切板".shortToast()
        }, report = {
            lifecycleScope.launchWhenResumed {
                gotoReportActivity(context, arrayListOf(article.id))
            }
        }).show()
        true
    }, onUrlClick = {
        vm.onUrlClick(it)
    }, onImageClick = { item, _ ->
        lifecycleScope.launch {
            //浏览大图
            ImageActivity.gotoImageActivity(
                context,
                cur = vm.findImgIndex(item.id),
                vm.imgList.map { it.id },
                vm.imgList.map { it.img + it.ext }
            )
        }
    })


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_page -> {
                SelectPageDialog(vm.refreshPage, vm.allPage, context) {
                    vm.selectPage(it)
                }.show()
                return true
            }
            R.id.action_reply -> {
                lifecycleScope.launch {
                    gotoRelyThreadActivity(context, id)
                }
            }
            R.id.action_collect -> {
                vm.collect()
            }
            R.id.action_report -> {

            }
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true) {
            this.finish()
        }
        //初始化列表
        mDataBinding.rvContent.layoutManager = mManager
        mDataBinding.rvContent.adapter = mAdapter
        //刷新状态
        mAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.Loading -> mDataBinding.swipeRefresh.isRefreshing = true
                is LoadState.NotLoading -> mDataBinding.swipeRefresh.isRefreshing = false
                is LoadState.Error -> {
                    mDataBinding.swipeRefresh.isRefreshing = false
                    "刷新失败".shortToast()
                }
            }
        }
        mDataBinding.swipeRefresh.init(this) {
            //加载前一页
            vm.loadPre()
        }
    }

    private fun initData() {
        vm.id = id
        vm.refreshPage = 1
        lifecycleScope.launch {
            launch {
                vm.getPager().collectLatest { data ->
                    mAdapter.submitData(data)
                }
            }
        }
//        mAdapter.withLoadStateHeaderAndFooter(
//            header = ExampleLoadStateAdapter(mAdapter::retry),
//            footer = ExampleLoadStateAdapter(mAdapter::retry)
//        )
    }

    @OnMessage
    fun gotoSelf(id: String) {
        lifecycleScope.launch {
            ThreadDetailActivity.gotoThreadDetailActivity(id, context)
        }
    }

    @OnMessage
    fun showLinkArticle(articleItem: ArticleItem) {
        LinkArticleDialog(context, articleItem, onImgClick = {
            //浏览大图
            lifecycleScope.launch {
                ImageActivity.gotoImageActivity(
                    context,
                    cur = 0,
                    arrayListOf(articleItem.id),
                    arrayListOf(articleItem.img + articleItem.ext)
                )
            }
        }) {
            vm.onUrlClick(it)
        }.show()
    }

    //刷新到指定页面
    @OnMessage
    fun refresh() {
        mAdapter.refresh()
//        mManager.smoothScrollToPosition(mDataBinding.rvContent, RecyclerView.State(), 0)
    }
}

@HiltViewModel
class ThreadDetailVM @Inject constructor(
    val app: Application,
    val repository: ArticleDetailRepository
) : FastViewModel(app) {
    @Bindable
    var title: String = "无标题"

    var id: String = ""

    var refreshPage: Int = 1//这个是表示当前刷新操作刷新到哪一页
    var curPage: Int = 1//这是正常load页面时加载的页面，代表最新加载的页面

    var allPage: Int = 1

    //缓存用来引用
    val cache = linkedMapOf<String, ArticleItem>()

    //图片列表(有图片的item)
    val imgList: List<ArticleItem>
        get() {
            return cache.filter {
                it.value.img.isNotEmpty()
            }.map { it.value }
        }

    //找到图片的序列 pos表示当前item的序号
    fun findImgIndex(id: String): Int {
        var index = 0
        for (i in imgList.indices) {
            if (imgList[i].id == id) {
                index = i
            }
        }
        return index
    }


    fun getPager(): Flow<PagingData<ArticleItem>> {
        return getCommonPager {
            object : NMBBasePagingSource<ArticleItem>(selectedPage = {
                //pagingData跳页是调用refresh的时候根据initKey来加载，这里返回当前page作为初始化页面
                refreshPage
            }) {
                override suspend fun load(pos: Int): List<ArticleItem> {
                    curPage = pos
                    val data = repository.getArticleDetail(id, pos)
                    val res = arrayListOf<ArticleItem>()
                    try {
                        if (pos == 1) {
                            val head = data.copy()
                            initData(head)
                            res.add(head)
                        }
                    } catch (e: Exception) {
                        e.message?.logI()
                    }

                    data.Replies?.let {
                        it.filter {
                            return@filter it.id != "9999999"||it.user_hash.lowercase()!="tips"
                        }.forEach { reply ->
                            if (reply.user_hash == data.user_hash) {
                                reply.master = "1"
                            } else {
                                reply.master = "0"
                            }
                            //缓存
                            cache[reply.id] = reply
                        }
                        res.addAll(it)
                    }
                    return res
                }
            }
        }.flow.cachedIn(viewModelScope)
    }

    //初始化一些信息
    private fun initData(head: ArticleItem) {
        title = head.title
        notifyPropertyChanged(BR.title)
        head.master = "1"
        cache[head.id] = head
        allPage = if (head.ReplyCount == null) {
            1
        } else {
            (head.ReplyCount!!.toInt() / PAGE_SIZE) + 1
        }

    }

    //点击了文本中的连接
    fun onUrlClick(url: String) {
        if (url.startsWith("/t/")) {
            url.split("/").let {
                if (it.size > 2) {
                    sendMessage(MR.ThreadDetailActivity_gotoSelf, it[2])
                }
            }
        } else if (url.startsWith(">>No.")) {
            //引用串
            val id = url.split(".")[1]
            cache[id]?.let {
                sendMessage(MR.ThreadDetailActivity_showLinkArticle, it)
            } ?: sendMessage(MR.ThreadDetailActivity_gotoSelf, id)
        }
    }

    //翻页
    fun selectPage(page: Int) {
        refreshPage = page
        sendEmptyMessage(MR.ThreadDetailActivity_refresh)
    }

    //刷新数据
    fun refreshCur() {
        refreshPage = curPage
        sendEmptyMessage(MR.ThreadDetailActivity_refresh)
    }

    //向前加载一页
    fun loadPre() {
        if (refreshPage > 1) {
            refreshPage--
        } else {
            refreshPage = 1
        }
        sendEmptyMessage(MR.ThreadDetailActivity_refresh)
    }

    fun collect() {
//        if(App.INSTANCE.cookie==null){
//            "当前未绑定饼干，收藏将和设备绑定".shortToast()
//        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val res = repository.collect(App.INSTANCE.androidId, id)
                withContext(Dispatchers.Main) {
                    res.shortToast()
                }
            }
        }
    }
}