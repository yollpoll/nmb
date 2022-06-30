package com.yollpoll.nmb.view.activity

import android.app.ActionBar
import android.app.Activity
import android.app.Application
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logD
import com.yollpoll.base.logE
import com.yollpoll.framework.extensions.saveList
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.KEY_FORUM_LIST
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityHomeBinding
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.router.ROUTE_HOME
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.Field
import javax.inject.Inject
import kotlin.math.max

@Route(url = ROUTE_HOME)
@ViewModel(HomeVm::class)
@AndroidEntryPoint
class HomeActivity : NMBActivity<ActivityHomeBinding, HomeVm>() {
    private val vm:HomeVm by viewModels()
    private val actionBarDrawerToggle by lazy {
         ActionBarDrawerToggle(
        this,
        mDataBinding.drawer,
        mDataBinding.toolbar,
        R.string.open,
        R.string.close
    )
    }
    override fun getLayoutId() = R.layout.activity_home
    override fun initViewModel()=vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }
    private fun initView(){
        setSupportActionBar(mDataBinding.toolbar)
        mDataBinding.drawer.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        mDataBinding.fabAction.onTop={
            mDataBinding.rvContent.scrollToPosition(0)
            true
        }
        mDataBinding.fabAction.onLeft={
            mDataBinding.drawer.openDrawer(Gravity.RIGHT)
            mDataBinding.drawer.isDrawerOpen(Gravity.LEFT).let {
                if(it){
                    mDataBinding.drawer.closeDrawer(Gravity.LEFT)
                }
            }
            true
        }
        mDataBinding.fabAction.onRight={
            mDataBinding.drawer.openDrawer(Gravity.LEFT)
            mDataBinding.drawer.isDrawerOpen(Gravity.RIGHT).let {
                if(it){
                    mDataBinding.drawer.closeDrawer(Gravity.RIGHT)
                }
            }
            true
        }
        mDataBinding.fabAction.onBottom={
            mDataBinding.refresh.isRefreshing=true
            true
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
class HomeVm @Inject constructor(val app: Application,val repository: HomeRepository) : FastViewModel(app) {

    init {
        initData()
    }
    private fun initData() {
        viewModelScope.launch {
            getForumList()
        }
    }

    /**
     * 板块列表
     */
    private suspend fun getForumList() {
        try {
            val list = repository.getForumList()
            LogUtils.e("getListL: ${list.size}")
            saveList(KEY_FORUM_LIST, list)
            "${list.size}".shortToast()
        } catch (e: Exception) {
            LogUtils.e("getForumList error: ${e.message}")
        }
    }
}