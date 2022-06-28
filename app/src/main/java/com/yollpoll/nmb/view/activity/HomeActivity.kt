package com.yollpoll.nmb.view.activity

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.TAG
import com.yollpoll.framework.extensions.saveBean
import com.yollpoll.framework.extensions.saveList
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.utils.ToastUtil
import com.yollpoll.nmb.KEY_FORUM_LIST
import com.yollpoll.nmb.R
import com.yollpoll.nmb.ROUTE_HOME
import com.yollpoll.nmb.databinding.ActivityHomeBinding
import com.yollpoll.nmb.model.repository.HomeRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@Route(url = ROUTE_HOME)
@ViewModel(HomeVm::class)
class HomeActivity : NMBActivity<ActivityHomeBinding, HomeVm>() {
    override fun getLayoutId() = R.layout.activity_home
    override fun onResume() {
        super.onResume()
//        lifecycleScope.launch(Dispatchers.IO){
//            repository.getForumList()
//        }
    }
}
class HomeVm constructor(app: Application) : FastViewModel(app) {
    @Inject lateinit var repository: HomeRepository

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
            if(null==repository){
                "asa".shortToast()
            }
            val list = repository.getForumList()
            LogUtils.e("getListL: ${list.size}")
            saveList(KEY_FORUM_LIST, list)
        } catch (e: Exception) {
            LogUtils.e("getForumList error: ${e.message}")
        }
    }
}