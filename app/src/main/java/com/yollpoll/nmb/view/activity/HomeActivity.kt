package com.yollpoll.nmb.view.activity

import android.app.ActionBar
import android.app.Activity
import android.app.Application
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.NmbPagingDataAdapter
import com.yollpoll.base.logD
import com.yollpoll.base.logE
import com.yollpoll.framework.extensions.saveList
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.BasePagingDataAdapter
import com.yollpoll.framework.paging.BasePagingSource
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.KEY_FORUM_LIST
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityHomeBinding
import com.yollpoll.nmb.databinding.ItemForumBinding
import com.yollpoll.nmb.databinding.ItemThreadBinding
import com.yollpoll.nmb.model.bean.*
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.router.ROUTE_HOME
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.reflect.Field
import javax.inject.Inject
import kotlin.math.max

@Route(url = ROUTE_HOME)
@ViewModel(HomeVm::class)
@AndroidEntryPoint
class HomeActivity : NMBActivity<ActivityHomeBinding, HomeVm>() {
    private val vm: HomeVm by viewModels()
    private val actionBarDrawerToggle by lazy {
        ActionBarDrawerToggle(
            this,
            mDataBinding.drawer,
            mDataBinding.toolbar,
            R.string.open,
            R.string.close
        )
    }

    //板块列表
    private val adapterForum = BasePagingDataAdapter<ForumDetail>(
        R.layout.item_forum,
        BR.bean,
        onBindDataBinding = { _, binding ->
            (binding as ItemForumBinding).llForum.setOnClickListener { v ->

            }
        })
    private val adapterThread = NmbPagingDataAdapter<ArticleItem>(
        R.layout.item_thread,
        BR.bean,
        onBindDataBinding = { item,_, binding->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                (binding as ItemThreadBinding).tvContent.text=Html.fromHtml(item?.content,FROM_HTML_MODE_COMPACT)
            }else{
                (binding as ItemThreadBinding).tvContent.text=Html.fromHtml(item?.content)
            }
        })

    override fun getLayoutId() = R.layout.activity_home
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        setSupportActionBar(mDataBinding.toolbar)
        mDataBinding.drawer.addDrawerListener(actionBarDrawerToggle)
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
            mDataBinding.rvContent.scrollToPosition(0)
            true
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
            true
        }
        mDataBinding.fabAction.onRight = {
            mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT).let {
                if (it) {
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                } else {
                    mDataBinding.drawer.openDrawer(Gravity.LEFT)
                }
            }
            true
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
            mDataBinding.refresh.isRefreshing = true
            true
        }
        //rvForum
        mDataBinding.rvForum.adapter = adapterForum
        mDataBinding.rvForum.layoutManager = LinearLayoutManager(this)
        //refresh
        mDataBinding.refresh.setColorSchemeColors(
            getAttrColor(R.attr.colorPrimary),
            getAttrColor(R.attr.colorSecondary),
            getAttrColor(R.attr.colorTertiary),
        )
        mDataBinding.refresh.setOnRefreshListener {
            adapterThread.refresh()
        }

        mDataBinding.rvContent.adapter=adapterThread
        mDataBinding.rvContent.layoutManager = LinearLayoutManager(this)
    }

    private fun initData() {
        lifecycleScope.launchWhenResumed {
            vm.forumList.collectLatest {
                adapterForum.submitData(it)
            }
            vm.threadList.collectLatest {
                adapterThread.submitData(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // navigation icon的点击交给actionbardrawertoggle来处理
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

@HiltViewModel
class HomeVm @Inject constructor(val app: Application, val repository: HomeRepository) :
    FastViewModel(app) {
    //板块列表
    val forumList = getCommonPager {
        object : BasePagingSource<Forum>() {
            override suspend fun load(pos: Int): List<Forum> {
                if (pos > 1) {
                    return arrayListOf()
                }
                return repository.getForumList()
            }
        }
    }.flow.flatMapConcat {
        flowOf(it.flatMap { forum ->
            forum.forums
        })
    }

    //串列表
    var threadList =
        getCommonPager { repository.getTimeLinePagingSource() }.flow.cachedIn(viewModelScope)
}