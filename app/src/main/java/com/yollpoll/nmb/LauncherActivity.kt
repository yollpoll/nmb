package com.yollpoll.nmb

import android.app.Application
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.router.Dispatcher
import com.yollpoll.framework.utils.ToastUtil
import com.yollpoll.nmb.databinding.ActivityMainBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_LAUNCHER
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by spq on 2022/6/23
 */
@AndroidEntryPoint
@ViewModel(LauncherVM::class)
@Route(url = ROUTE_LAUNCHER)
class LauncherActivity :NMBActivity<ActivityMainBinding,LauncherVM>(){
    override fun getContentViewId(): Int {
        return R.layout.activity_main
    }
    @OnMessage
    fun gotoMain(){
        lifecycleScope.launch {
            val req = DispatchRequest.RequestBuilder().host("business").module("home").params(
                hashMapOf("from" to "from")).build()
            DispatchClient.manager?.dispatch(context, req)
        }
    }
}
class LauncherVM constructor(app:Application):FastViewModel(app){
    init {
        viewModelScope.launch {

            delay(3000)
            sendEmptyMessage(MR.LauncherActivity_gotoMain)
        }
    }
}