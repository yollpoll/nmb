package com.yollpoll.nmb

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.arch.log.LogUtils
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by spq on 2022/6/23
 */
@AndroidEntryPoint
@Route(url = ROUTE_LAUNCHER)
@ViewModel(LauncherVM::class)
class LauncherActivity : NMBActivity<ActivityMainBinding, LauncherVM>() {
    @OnMessage
    fun gotoMain() {
        lifecycleScope.launch {
            val req = DispatchRequest.RequestBuilder().host("nmb").module("home").params(
                hashMapOf("from" to "from")
            ).build()
            DispatchClient.manager?.dispatch(this@LauncherActivity, req)
        }
    }

    override fun getLayoutId() = R.layout.activity_main
}

class LauncherVM(app: Application) : FastViewModel(app) {
    init {
        viewModelScope.launch {
            delay(1000)
            sendEmptyMessage(MR.LauncherActivity_gotoMain)
        }
    }
}