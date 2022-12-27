package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.Bindable
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.nmb.*
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ThreadAdapter
import com.yollpoll.nmb.databinding.ActivityHomeBinding
import com.yollpoll.nmb.databinding.ItemForumBinding
import com.yollpoll.nmb.model.bean.*
import com.yollpoll.nmb.model.repository.ForumRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.net.realCover
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_HOME
import com.yollpoll.nmb.service.ThreadReplyService
import com.yollpoll.nmb.view.widgets.*
import com.yollpoll.utils.copyStr
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@Route(url = ROUTE_HOME)
@AndroidEntryPoint
class HomeActivity : NMBActivity<ActivityHomeBinding, HomeVm>() {
    private val vm: HomeVm by viewModels()
    private val actionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(
            this,
            mDataBinding.drawer,
            mDataBinding.layoutTitle.toolbar,
            R.string.open,
            R.string.close
        )
    }

    //板块列表
    private val forumManager = LinearLayoutManager(this)
    private val adapterForum = NmbPagingDataAdapter<ForumDetail>(
        R.layout.item_forum,
        BR.bean,
        onBindDataBinding = { item, pos, binding, _ ->
            (binding as ItemForumBinding).llForum.setOnClickListener { v ->
                //点击事件
                if (null == item) {
                    return@setOnClickListener
                }
                vm.selectForum(item)
                if (mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                }
                threadManager.scrollToPosition(0)
            }
            if (pos >= vm.forumSize - 1) {
                binding.line.visibility = View.GONE
            } else {
                binding.line.visibility = View.VISIBLE
            }
        })

    //串列表
    private val threadManager = LinearLayoutManager(this)

    private val adapterThread = ThreadAdapter(
        onItemLongClick = { article ->
            ThreadMenuDialog(MenuAction.MENU_ACTION_HOME, context, copyNo =
            {
                copyStr(context, article.id)
                "${article.id} 复制到剪切板".shortToast()
            }, copy = {
                copyStr(context, article.content)
                "复制到剪切板".shortToast()
            }, report = {
                lifecycleScope.launchWhenResumed {
                    gotoReportActivity(context, arrayListOf(article.id))
                }
            }, shield = {
                vm.shieldThread(article)
            }).show()
            true
        },
        onUrlClick = {
            vm.onThreadUrlClick(it)
        },
        onImageClick = { item, pos ->
            lifecycleScope.launch {
                gotoThreadImageActivity(context, 0, item.id)
//                ImageActivity.gotoImageActivity(
//                    context,
//                    0,
//                    listOf(item.id),
//                    listOf(item.img + item.ext)
//                )
            }
        }) { item ->
        lifecycleScope.launch {
            ThreadDetailActivity.gotoThreadDetailActivity(item.id, context)
        }
    }

    override fun getMenuLayout() = R.menu.menu_home
    override fun getLayoutId() = R.layout.activity_home
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
        //启动更新服务
        lifecycleScope.launch(Dispatchers.IO) {
            val intent = Intent(context, ThreadReplyService::class.java)
            bindService(intent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }

            }, BIND_AUTO_CREATE)
        }
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar)
        mDataBinding.drawer.addDrawerListener(actionBarDrawerToggle)
        //手势冲突处理
        mDataBinding.drawer.initLeftDrawerLayout(this)
        actionBarDrawerToggle.syncState()
        mDataBinding.fabAction.onTop = {
            mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT).let {
                if (it) {
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                }
            }
            mDataBinding.drawer.isDrawerOpen(Gravity.LEFT).let {
                if (it) {
                    mDataBinding.drawer.closeDrawer(Gravity.LEFT)
                }
            }
            threadManager.smoothScrollToPosition(mDataBinding.rvContent, RecyclerView.State(), 0)
            false
        }
        //初始化操作按钮
        mDataBinding.fabAction.onLeft = {
            mDataBinding.drawer.isDrawerOpen(Gravity.LEFT).let {
                if (it) {
                    mDataBinding.drawer.closeDrawer(Gravity.LEFT)
                } else {
                    mDataBinding.drawer.openDrawer(Gravity.RIGHT)
                }
            }
            false
        }
        mDataBinding.fabAction.onRight = {
            mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT).let {
                if (it) {
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                } else {
                    mDataBinding.drawer.openDrawer(Gravity.LEFT)
                }
            }
            false
        }
        mDataBinding.fabAction.onBottom = {
            mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT).let {
                if (it) {
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                }
            }
            mDataBinding.drawer.isDrawerOpen(Gravity.LEFT).let {
                if (it) {
                    mDataBinding.drawer.closeDrawer(Gravity.LEFT)
                }
            }
            refreshThread()
            false
        }
        mDataBinding.fabAction.onClick = {
            "onCLick".logI()
            lifecycleScope.launch {
                gotoNewThreadActivity(context, vm.curForumDetail.id)
            }
            true
        }
        mDataBinding.rvContent.adapter = adapterThread
        mDataBinding.rvContent.layoutManager = threadManager
        //rvForum
        mDataBinding.rvForum.adapter = adapterForum
        mDataBinding.rvForum.layoutManager = forumManager
        //refresh
        mDataBinding.refresh.init(this) {
            refreshThread()
        }
    }

    //数据处理
    private fun initData() {
        vm.getAnnouncement()
        adapterThread.addLoadStateListener {
            when (it.refresh) {
                is LoadState.Loading -> {
                    "refresh Loading".logI()
                    mDataBinding.refresh.isRefreshing = true
                }
                is LoadState.NotLoading -> {
                    "refresh NotLoading".logI()
                    if (mDataBinding.refresh.isRefreshing) {
                        mDataBinding.refresh.isRefreshing = false
                        threadManager.smoothScrollToPosition(
                            mDataBinding.rvContent,
                            RecyclerView.State(),
                            0
                        )
                    }
                }
                is LoadState.Error -> {
                    mDataBinding.refresh.isRefreshing = false
                    "刷新失败".shortToast()
                }
            }
        }

        lifecycleScope.launch {
            launch {
                vm.forumList.collectLatest {
                    adapterForum.submitData(it)
                }
            }
            vm.threadPager.observe(this@HomeActivity, Observer {
                launch {
                    it.flow.cachedIn(lifecycleScope).collectLatest {
                        adapterThread.submitData(it)
                    }
                }
            })
            //加载封面
            launch {
                vm.refreshCover()
            }
        }
    }

    @OnMessage(key = ACTION_REFRESH_FORUM)
    fun onRefreshForum() {
        adapterForum.refresh()
    }

    //一些全局的ui变化事件
    override fun onUiActionEvent(action: String, data: Any?) {
        super.onUiActionEvent(action, data)
        when (action) {
            ACTION_SELECT_THEME -> {
                //切换主题刷新界面
                mDataBinding.refresh.isRefreshing = true
                mDataBinding.rvContent.adapter = adapterThread
                refreshThread()
            }
            ACTION_NO_IMG -> {
                //无图模式
                adapterThread.notifyDataSetChanged()
            }
            ACTION_NO_COOKIE -> {
                //饼干混乱模式
                adapterThread.notifyDataSetChanged()
            }
        }
    }

    /**
     * 新建串
     */
    fun createArticle(view: View) {

    }


    fun gotoCookie() {
        lifecycleScope.launch {
            gotoCookieActivity(context)
        }
    }

    fun gotoCollecion() {
        lifecycleScope.launch { gotoCollectionActivity(context) }
    }

    fun gotoMySpeech() {
        lifecycleScope.launch { gotoMySpeechActivity(context) }
    }

    fun gotoSetting() {
        lifecycleScope.launch {
            gotoSetting(context)
        }
    }

    fun gotoAuthor() {
        lifecycleScope.launch {
            gotoAuthor(context)
        }
    }

    fun showCover() {
        lifecycleScope.launch {
            gotoImageActivityByUrl(context, realCover)
//            ImageActivity.gotoImageActivity(context, 0, arrayListOf("封面"), arrayListOf(realCover))
        }
    }

    fun gotoVisitHistory() {
        lifecycleScope.launch {
            gotoHistory(context)
        }
    }

    fun gotoDonate(){
        lifecycleScope.launch {
            gotoImageByResource(context,R.mipmap.ic_donate)
        }
    }

    fun gotoOfficial(){
        lifecycleScope.launch {
            DispatchClient.manager?.dispatch(context,DispatchRequest.UrlBuilder(OFFICIAL_WEB).build())
        }
    }

    //封面图片刷新
    @OnMessage
    fun onRefreshCover() {
        Glide.with(context).asBitmap().load(realCover)
            .into(mDataBinding.root.findViewById(R.id.iv_cover))
    }

    @OnMessage
    fun showAnnouncement() {
        AnnouncementDialog(vm.announcement.value?.content, context) { url ->
            if (url.startsWith(">>")) {
                try {
                    gotoThreadDetail(url.split(".")[1])
                } catch (e: Exception) {
                    "找不到串".shortToast()
                }
            }
        }.show()
    }

    @OnMessage
    fun gotoThreadDetail(id: String) {
        lifecycleScope.launch {
            ThreadDetailActivity.gotoThreadDetailActivity(id, context)
        }
    }


    //刷新串
    @OnMessage
    fun refreshThread() {
        adapterThread.refresh()
        if (!mDataBinding.refresh.isRefreshing) {
            mDataBinding.refresh.isRefreshing = true
        }
    }

    //处理标题栏
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_forum -> {
                if (mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                } else {
                    mDataBinding.drawer.openDrawer(Gravity.RIGHT)
                }
                return true
            }
            R.id.action_announcement -> {
                showAnnouncement()
            }
            R.id.action_search -> {
                "功能未开放".shortToast()
            }
        }
        // navigation icon的点击交给actionbardrawertoggle来处理
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    var onBackPress=false

    override fun onBackPressed() {
        if(onBackPress){
            super.onBackPressed()
        }else{
            "再按一次返回桌面".shortToast()
            onBackPress=true
            lifecycleScope.launch {
                delay(3000)
                onBackPress=false
            }
        }
    }

}

//viewModel
@HiltViewModel
class HomeVm @Inject constructor(
    val app: Application,
    val repository: HomeRepository,
    val forumRepository: ForumRepository
) :
    FastViewModel(app) {
    //当前板块Id
    private var curForumId = MutableLiveData(-1)

    //当前板块
    var curForumDetail: ForumDetail =
        ForumDetail(null, null, "-1", null, "", "时间线", null, null, null, null)

    //标题
    @Bindable
    var title: String = "时间线1"

    //公告
    private val _announcement: MutableLiveData<Announcement> = MutableLiveData()
    val announcement: LiveData<Announcement> = _announcement


    init {
        curForumId.value = -1
    }

    //板块列表
    val forumList =
        getCommonPager {
            object : BasePagingSource<ForumDetail>() {
                override fun getRefreshKey(state: PagingState<Int, ForumDetail>): Int {
                    return 1
                }

                override suspend fun load(pos: Int): List<ForumDetail> {
                    if (pos > 1) {
                        return arrayListOf()
                    }
                    val forumList = forumRepository.getShowForum()

                    selectForum(forumList.first {
                        it.id == "4"
                    })

                    forumSize = forumList.size
                    return forumList
                }
            }
        }.flow

    //板块总数
    var forumSize = 1


    //串列表
    private val _threadPager = MutableLiveData<Pager<Int, ArticleItem>>(getCommonPager {
        repository.getTimeLinePagingSource(curForumId.value!!)
    })

    val threadPager: LiveData<Pager<Int, ArticleItem>> = _threadPager

    //刷新封面
    suspend fun refreshCover() {
        try {
            repository.refreshCover()

            sendEmptyMessage(MR.HomeActivity_onRefreshCover)
        } catch (e: Exception) {
//            "封面加载失败".logI()
        }
    }

    //选择板块
    fun selectForum(forum: ForumDetail) {
        viewModelScope.launch {
            title = forum.name ?: "匿名版"
            notifyPropertyChanged(BR.title)
            curForumDetail = forum
            curForumId.value = forum.id.toInt()
            when (forum.id) {
                "-1" -> {
                    //时间线1
                    _threadPager.value = getNMBCommonPager { repository.getTimeLinePagingSource(1) }
                }
                "-2" -> {
                    //时间线2
                    _threadPager.value = getNMBCommonPager { repository.getTimeLinePagingSource(2) }
                }
                else -> {
                    //普通串
                    _threadPager.value =
                        getNMBCommonPager {
                            repository.getThreadsPagingSource(forum.id)
                        }
                }
            }
        }

    }

    //获取公告
    fun getAnnouncement() {
        viewModelScope.launch {
            try {
                val res = repository.getAnnouncement()
                _announcement.value = res
                val lastShow = getLongByDataStore(KEY_SHOW_ANNOUNCEMENT, -1)
                val now = Date().time
                if (res.enable && ((lastShow == -1L) || (now - lastShow >= 24 * 60 * 60 * 1000))) {
                    saveLongToDataStore(KEY_SHOW_ANNOUNCEMENT, now)
                    sendEmptyMessage(MR.HomeActivity_showAnnouncement)
                }
            } catch (e: Exception) {

            }

        }
    }

    //url点击
    fun onThreadUrlClick(url: String) {
        if (url.startsWith("/t/")) {
            url.split("/").let {
                if (it.size > 2) {
                    sendMessage(MR.HomeActivity_gotoThreadDetail, it[2])
                }
            }
        } else if (url.startsWith(">>No.")) {
        }
    }

    //屏蔽串
    fun shieldThread(item: ArticleItem) {
        viewModelScope.launch {
            repository.addArticle(item)
            repository.addShieldThread(item.id)
            sendEmptyMessage(MR.HomeActivity_refreshThread)
        }
    }

}