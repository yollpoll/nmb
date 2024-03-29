package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
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
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.framework.utils.getBoolean
import com.yollpoll.framework.utils.getInt
import com.yollpoll.framework.utils.getString
import com.yollpoll.nmb.*
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ExampleLoadStateAdapter
import com.yollpoll.nmb.adapter.ThreadAdapter
import com.yollpoll.nmb.databinding.ActivityThreadDetailBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.CookieBean
import com.yollpoll.nmb.model.bean.HistoryBean
import com.yollpoll.nmb.model.bean.ImgTuple
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.CookieRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.model.repository.UserRepository
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_THREAD_DETAIL
import com.yollpoll.nmb.view.widgets.*
import com.yollpoll.utils.copyStr
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.Cookie
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
@Route(url = ROUTE_THREAD_DETAIL)
class ThreadDetailActivity : NMBActivity<ActivityThreadDetailBinding, ThreadDetailVM>() {
    @Extra
    lateinit var id: String

    companion object {
        suspend fun gotoThreadDetailActivity(
            id: String, context: Context
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
        val action = article.tagColor?.let {
            MenuAction.MENU_ACTION_REPLY_CANCEL_MARK
        } ?: MenuAction.MENU_ACTION_REPLY
        ThreadMenuDialog(action, context, reply = {
            lifecycleScope.launchWhenResumed {
                gotoLinkActivity(context, vm.id, arrayListOf(article.id))
            }
        }, mark = {
            article.tagColor?.let {
                SelectColorDialog(context, it.red, it.blue, it.green) { color ->
                    vm.markCookie(article.user_hash, color)
                }.show()
            } ?: run {
                SelectColorDialog(context) { color ->
                    vm.markCookie(article.user_hash, color)
                }.show()
            }
        }, cancelMark = {
            vm.cancelMarkCookie(article.user_hash)
        }, copyNo = {
            copyStr(context, article.id)
            "${article.id} 复制到剪切板".shortToast()
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
            val cur = vm.findImgIndex(item.id)
            gotoThreadImageActivity(context, cur, id)
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
            R.id.action_clear_cookie -> {
                vm.clearCookieMark(context.getAttrColor(R.attr.colorOnSecondaryContainer))
            }
            R.id.action_only_po -> {
                val checked = item.isChecked
                item.isChecked = !checked
                vm.onlyPo = !checked
                mAdapter.refresh()
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
        lifecycleScope.launch {
            val lastPage = getInt("${id}_page", 1)
            val lastIndex = getInt("${id}_index", default = 0)
            vm.init(id, lastPage, lastPage, context.getAttrColor(R.attr.colorOnSecondaryContainer))
        }
    }

    @OnMessage
    fun onHeadLoad() {
        //head已经加载完成
        lifecycleScope.launch {
            vm.getPager().collectLatest { data ->
                mAdapter.submitData(data)
            }
        }
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
                gotoThreadImageActivity(context, 0, articleItem.id)
            }
        }) {
            vm.onUrlClick(it)
        }.show()
    }

    //刷新到指定页面
    @OnMessage
    fun refresh() {
        mAdapter.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.savePageIndex(mManager.findLastCompletelyVisibleItemPosition())
    }

    override fun onStop() {
        super.onStop()
        vm.cancelLoadData()
    }

    override fun onResume() {
        super.onResume()
        //开始加载数据
        vm.loadDataToCache()
    }
}

@HiltViewModel
class ThreadDetailVM @Inject constructor(
    val app: Application,
    val repository: ArticleDetailRepository,
    val userRepository: UserRepository,
    val cookieRepository: CookieRepository
) : FastViewModel(app) {
    //混乱饼干模式下饼干映射
    private val cookieMap = hashMapOf<String, String>()

    private val myCookies = arrayListOf<CookieBean>()

    @Bindable
    var title: String = "无标题"

    var id: String = ""

    lateinit var head: ArticleItem

    var refreshPage: Int = 1//这个是表示当前刷新操作刷新到哪一页

    var curPage: Int = 1//这是正常load页面时加载的页面，代表最新加载的页面

    var allPage: Int = 1

    //缓存用来引用
    val cache = linkedMapOf<String, ArticleItem>()

    //图片列表(有图片的item)
    lateinit var imgList: List<ImgTuple>

    //饼干标记
    val tagMap = hashMapOf<String, Int>()

    //只看po
    var onlyPo = false

    //初始化数据
    fun init(id: String, refreshPage: Int, curPage: Int, myDefaultCookieColor: Int) {
        this.id = id
        this.refreshPage = refreshPage
        this.curPage = curPage
        viewModelScope.launch {
            myCookies.addAll(cookieRepository.queryCookies())
            try {
                head = repository.getArticle(id)
                initHead(head)
                sendEmptyMessage(MR.ThreadDetailActivity_onHeadLoad)
            } catch (e: Exception) {

            }
            //图片列表
            imgList = repository.getImagesList(id)
            //配置我的饼干的颜色
            val myCookieColor = getInt(KEY_COOKIE_COLOR, myDefaultCookieColor)
            cookieRepository.queryCookies().forEach {
                tagMap[it.name] = myCookieColor
            }
        }
    }

    //初始化一些信息
    private fun initHead(head: ArticleItem) {
        title = head.title
        notifyPropertyChanged(BR.title)
        head.master = "1"
        cache[head.id] = head
        allPage = if (head.ReplyCount == null) {
            1
        } else {
            (head.ReplyCount!!.toInt() / PAGE_SIZE)
        }
        if (!hasAddHistory) {
            saveHistory(head)
            hasAddHistory = true
        }
    }

    //找到图片的序列 pos表示当前item的序号
    suspend fun findImgIndex(id: String): Int {
        imgList = repository.getImagesList(this.id)
        for (i in imgList.indices) {
            if (imgList[i].id == id) {
                return i
            }
        }
        return 0
    }


    fun getPager(): Flow<PagingData<ArticleItem>> {
        return getNMBCommonPager {
            object : NMBBasePagingSource<ArticleItem>(startIndex = refreshPage, selectedPage = {
                //pagingData跳页是调用refresh的时候根据initKey来加载，这里返回当前page作为初始化页面
                refreshPage
            }) {
                override suspend fun load(pos: Int): List<ArticleItem> {
                    curPage = pos
                    val data: ArrayList<ArticleItem> = arrayListOf()
                    if (pos == 1) {
                        //当前第一页，加入head
                        data.add(0, head)
                    }
                    try {
                        data.addAll(repository.getArticleReply(head, pos) ?: return emptyList())
                    } catch (e: Exception) {
                        "加载失败: ${e.message}".logE()
                    }
                    return data.filter {
                        return@filter it.id != "9999999"
                    }.filter {
                        if (onlyPo) {
                            return@filter it.user_hash == head.user_hash
                        }
                        true
                    }.mapIndexed { index, reply ->
                        reply.index =index + (pos - 1) * PAGE_SIZE
                        changeCookieRandom(reply)
                        if (reply.user_hash == head.user_hash) {
                            reply.master = "1"
                        } else {
                            reply.master = "0"
                        }
                        reply.tagColor = tagMap[reply.user_hash]
                        //缓存
                        cache[reply.id] = reply
                        reply
                    }
                }
            }
        }.flow.cachedIn(viewModelScope)
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
                val res = repository.collect(userRepository.getCollectionId(), id)
                withContext(Dispatchers.Main) {
                    res.shortToast()
                }
            }
        }
    }

    //记录书签
    fun savePageIndex(index: Int) {
        GlobalScope.launch {
            //当前页面数
            val pageNo = index / PAGE_SIZE
            "save page: ${pageNo + refreshPage} index ${index - pageNo * PAGE_SIZE}".logD()
            //当前第几页
            saveIntToDataStore("${id}_page", pageNo + refreshPage)
            //当前第一条
            saveIntToDataStore("${id}_index", index - pageNo * PAGE_SIZE)
        }
    }

    var hasAddHistory: Boolean = false

    //添加历史记录
    fun saveHistory(bean: ArticleItem) {
        viewModelScope.launch {
            val history = HistoryBean(
                bean.admin,
                bean.content,
                bean.email,
                bean.id.toInt(),
                bean.name,
                bean.now,
                null,
                null,
                bean.title,
                bean.user_hash,
                updateTime = System.currentTimeMillis()
            )
            userRepository.addHistory(history)
        }
    }

    //混乱饼干
    private fun changeCookieRandom(article: ArticleItem) {
        val randomCookie = getBoolean(KEY_NO_COOKIE, false)
        if (!randomCookie) {
            return
        }
        myCookies.forEach {
            if (it.name == article.user_hash) {
                //自己的cookie不修改
                return
            }
        }
        if (article.admin == "1") {
            //红名特权不许乱改
            return
        }
        if (cookieMap.containsKey(article.user_hash)) {
            article.user_hash = cookieMap[article.user_hash]!!
        } else {
            val newCookie = randomCookie(article.user_hash)
            cookieMap[article.user_hash] = newCookie
            article.user_hash = newCookie
        }
    }

    //根据饼干随机生产映射
    private fun randomCookie(cookie: String): String {
        var newCookie: String = ""
        for (i in cookie.indices) {
            newCookie += ('a'..'z').random().toChar()
        }
        return newCookie
    }

    //添加饼干标记
    fun markCookie(cookie: String, color: Int) {
        tagMap[cookie] = color
        sendEmptyMessage(MR.ThreadDetailActivity_refresh)
    }

    //删除标记
    fun cancelMarkCookie(cookie: String) {
        tagMap.remove(cookie)
        sendEmptyMessage(MR.ThreadDetailActivity_refresh)
    }

    //清除所有标记
    fun clearCookieMark(defaultColor: Int) {
        viewModelScope.launch {
            tagMap.clear()
            val cookieColor = getInt(KEY_COOKIE_COLOR, defaultColor)
            cookieRepository.queryCookies().forEach {
                tagMap[it.name] = cookieColor
            }
            sendEmptyMessage(MR.ThreadDetailActivity_refresh)
        }
    }

    //记载数据到本地数据库
    fun loadDataToCache() {
        if (id == "0") {
            return
        }
        viewModelScope.launch {
            FlowEventBus.post(ACTION_UPDATE_THREAD_DETAIL, id)
        }
    }

    //取消加载
    fun cancelLoadData() {
        if (id == "0") {
            return
        }
        viewModelScope.launch {
            "ThreadReply cancel: $id".logI()
            FlowEventBus.post(ACTION_UPDATE_THREAD_DETAIL_CANCEL, id)
        }
    }


}