package com.yollpoll.nmb.view.activity

import android.app.Application
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.net.http.RetrofitIntercept
import com.yollpoll.nmb.MR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityMainBinding
import com.yollpoll.nmb.net.BASE_URL
import com.yollpoll.nmb.net.HttpService
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_LAUNCHER
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by spq on 2022/6/23
 */
@Route(url = ROUTE_LAUNCHER)
@AndroidEntryPoint
@ViewModel(LauncherVM::class)
class LauncherActivity : NMBActivity<ActivityMainBinding, LauncherVM>() {
    val vm: LauncherVM by viewModels()
    override fun initViewModel()=vm
    override fun getLayoutId() = R.layout.activity_main

    @OnMessage
    fun gotoMain() {
        lifecycleScope.launch {
            val req = DispatchRequest.RequestBuilder().host("nmb").module("home").params(
                hashMapOf("from" to "from")
            ).build()
            DispatchClient.manager?.dispatch(this@LauncherActivity, req)
        }
    }

}
@HiltViewModel
class LauncherVM @Inject constructor(val app: Application) : FastViewModel(app) {
    init {
        viewModelScope.launch {
            "sasa".shortToast()
            delay(10000)
            sendEmptyMessage(MR.LauncherActivity_gotoMain)
        }
    }

}