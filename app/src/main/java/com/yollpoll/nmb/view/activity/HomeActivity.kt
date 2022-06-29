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
import com.yollpoll.nmb.databinding.ActivityHomeBinding
import com.yollpoll.nmb.di.HomeRepositoryAnnotation
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.router.ROUTE_HOME
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Route(url = ROUTE_HOME)
@ViewModel(HomeVm::class)
@AndroidEntryPoint
class HomeActivity : NMBActivity<ActivityHomeBinding, HomeVm>() {
    val vm:HomeVm by viewModels()
    override fun getLayoutId() = R.layout.activity_home
    override fun onResume() {
        super.onResume()
        try {
//            vm.initData()
        }catch (e:Exception){
            ToastUtil.showLongToast("xxxxx")
        }
//        lifecycleScope.launch(Dispatchers.IO){
//            repository.getForumList()
//        }
    }

    override fun initViewModel()=vm
}
@HiltViewModel
class HomeVm @Inject constructor(val app: Application) : FastViewModel(app) {
    @Inject lateinit var repository: HomeRepository

    fun initData() {
        ToastUtil.showLongToast("sasa")
        viewModelScope.launch {
//            getForumList()
        }
    }

//    /**
//     * 板块列表
//     */
//    private suspend fun getForumList() {
//        try {
//            if(null==repository){
//                "asa".shortToast()
//            }
//            val list = repository.getForumList()
//            LogUtils.e("getListL: ${list.size}")
//            saveList(KEY_FORUM_LIST, list)
//            "sasas".shortToast()
//        } catch (e: Exception) {
//            LogUtils.e("getForumList error: ${e.message}")
//        }
//    }
}