package com.yollpoll.nmb.view.activity

import android.app.ActionBar
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.customview.widget.ViewDragHelper
import androidx.databinding.Bindable
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.PermissionAuto
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.dispatch.OnBackListener
import com.yollpoll.framework.dispatch.StartType
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.BasePagingDataAdapter
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.framework.widgets.BaseDialog
import com.yollpoll.nmb.*
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.adapter.ThreadAdapter
import com.yollpoll.nmb.databinding.ActivityHomeBinding
import com.yollpoll.nmb.databinding.ItemForumBinding
import com.yollpoll.nmb.databinding.ItemThreadBinding
import com.yollpoll.nmb.model.bean.*
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.net.realCover
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_HOME
import com.yollpoll.nmb.view.widgets.*
import com.yollpoll.utils.copyStr
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.reflect.Field
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.max

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

    //????????????
    private val forumManager = LinearLayoutManager(this)
    private val adapterForum = NmbPagingDataAdapter<ForumDetail>(
        R.layout.item_forum,
        BR.bean,
        onBindDataBinding = { item, _, binding ->
            (binding as ItemForumBinding).llForum.setOnClickListener { v ->
                //????????????
                if (null == item) {
                    return@setOnClickListener
                }
                vm.selectForum(item)
                if (mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                }
                threadManager.scrollToPosition(0)
            }
        })

    //?????????
    private val threadManager = LinearLayoutManager(this)
    private val adapterThread = ThreadAdapter(onItemLongClick = { article ->
        ThreadMenuDialog(MenuAction.MENU_ACTION_HOME, context, copy = {
            copyStr(context, article.content)
            "??????????????????".shortToast()
        }, report = {
            lifecycleScope.launchWhenResumed {
                gotoReportActivity(context, arrayListOf(article.id))
            }
        }).show()
        true
    }, onUrlClick = {
        vm.onThreadUrlClick(it)
    }, onImageClick = { item, pos ->
        lifecycleScope.launch {
            ImageActivity.gotoImageActivity(
                context,
                0,
                listOf(item.id),
                listOf(item.img + item.ext)
            )
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
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar)
        mDataBinding.drawer.addDrawerListener(actionBarDrawerToggle)
        //??????????????????
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
        //?????????????????????
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

    //????????????
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
                    "refresh Error".logI()
                    mDataBinding.refresh.isRefreshing = false
                    "????????????".shortToast()
                }
            }
        }

        lifecycleScope.launchWhenResumed {
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
            //????????????
            launch {
                vm.refreshCover()
            }
        }
    }

    /**
     * ?????????
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

    }

    fun gotoAuthor() {
        lifecycleScope.launch {
            gotoAuthor(context)
        }
    }

    fun showCover() {
        lifecycleScope.launch {
            ImageActivity.gotoImageActivity(context, 0, arrayListOf("??????"), arrayListOf(realCover))
        }
    }

    //??????????????????
    @OnMessage
    fun onRefreshCover() {
        Glide.with(context).asBitmap().load(realCover)
            .into(mDataBinding.root.findViewById(R.id.iv_cover))
    }

    @OnMessage
    fun showAnnouncement() {
        AnnouncementDialog(vm.announcement.value?.content, context).show()
    }

    @OnMessage
    fun gotoThreadDetail(id: String) {
        lifecycleScope.launch {
            ThreadDetailActivity.gotoThreadDetailActivity(id, context)
        }
    }


    //?????????
    private fun refreshThread() {
        adapterThread.refresh()
        if (!mDataBinding.refresh.isRefreshing) {
            mDataBinding.refresh.isRefreshing = true
        }
    }

    //???????????????
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_forum) {
            if (mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT)) {
                mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
            } else {
                mDataBinding.drawer.openDrawer(Gravity.RIGHT)
            }
            return true
        } else if (item.itemId == R.id.action_announcement) {
            showAnnouncement()
        } else if (item.itemId == R.id.action_search) {
            "???????????????".shortToast()
        }
        // navigation icon???????????????actionbardrawertoggle?????????
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}

//viewModel
@HiltViewModel
class HomeVm @Inject constructor(val app: Application, val repository: HomeRepository) :
    FastViewModel(app) {
    //????????????Id
    private var curForumId = MutableLiveData(-1)

    //????????????
    var curForumDetail: ForumDetail =
        ForumDetail(null, null, "-1", null, null, "?????????", null, null, null, null)


    //??????
    @Bindable
    var title: String = "?????????1"

    //??????
    private val _announcement: MutableLiveData<Announcement> = MutableLiveData()
    val announcement: LiveData<Announcement> = _announcement


    init {
        curForumId.value = -1
    }

    //????????????
    val forumList =
        getCommonPager {
            object : BasePagingSource<Forum>() {
                override suspend fun load(pos: Int): List<Forum> {
                    if (pos > 1) {
                        return arrayListOf()
                    }
                    val list = repository.getForumList()
                    val cache = ArrayList(list).flatMap {
                        it.forums
                    }
                    //??????????????????
                    saveList(KEY_FORUM_LIST, cache)
                    return list
                }
            }
        }.flow.flatMapConcat {
            flowOf(it.flatMap { forum ->
                if (forum.sort == "1") {
                    val childForums = arrayListOf<ForumDetail>()
                    forum.forums.forEach { childForum ->
                        childForums.add(childForum.apply {
                            if (this.id == "-1") {
                                this.name = "?????????1"
                            }
                        })
                        if (childForum.id == "-1") {
                            //????????????????????????
                            childForums.add(
                                ForumDetail(
                                    null,
                                    null,
                                    "-2",
                                    null,
                                    null,
                                    "?????????2",
                                    "?????????2",
                                    "1",
                                    null,
                                    null
                                )
                            )
                        }
                    }
                    return@flatMap childForums
                }
                forum.forums
            })
        }.cachedIn(viewModelScope)

    //?????????
    private val _threadPager = MutableLiveData<Pager<Int, ArticleItem>>(getCommonPager {
        repository.getTimeLinePagingSource(curForumId.value!!)
    })

    val threadPager: LiveData<Pager<Int, ArticleItem>> = _threadPager

    //????????????
    suspend fun refreshCover() {
        try {
            repository.refreshCover()
            sendEmptyMessage(MR.HomeActivity_onRefreshCover)
        } catch (e: Exception) {
            "??????????????????".shortToast()
        }
    }

    //????????????
    fun selectForum(forum: ForumDetail) {
        viewModelScope.launch {
            title = forum.name ?: "?????????"
            notifyPropertyChanged(BR.title)
            curForumDetail = forum
            curForumId.value = forum.id.toInt()
            when (forum.id) {
                "-1" -> {
                    //?????????1
                    _threadPager.value = getNMBCommonPager { repository.getTimeLinePagingSource(1) }
                }
                "-2" -> {
                    //?????????2
                    _threadPager.value = getNMBCommonPager { repository.getTimeLinePagingSource(2) }
                }
                else -> {
                    //?????????
                    _threadPager.value =
                        getNMBCommonPager { repository.getThreadsPagingSource(forum.id) }
                }
            }
        }

    }

    //????????????
    fun getAnnouncement() {
        viewModelScope.launch {
            val res = repository.getAnnouncement()
            _announcement.value = res
            val lastShow = getLongByDataStore(KEY_SHOW_ANNOUNCEMENT, -1)
            val now = Date().time
            if (res.enable && ((lastShow == -1L) || (now - lastShow >= 24 * 60 * 60 * 1000))) {
                saveLongToDataStore(KEY_SHOW_ANNOUNCEMENT, now)
                sendEmptyMessage(MR.HomeActivity_showAnnouncement)
            }
        }
    }

    //url??????
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

}