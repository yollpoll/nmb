package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import androidx.activity.viewModels
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityPayBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_DRAFT
import com.yollpoll.nmb.router.ROUTE_PAY
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * Created by spq on 2022/12/8
 */
suspend fun gotoPayActivity(context: Context) {
    val req = DispatchRequest.UrlBuilder(ROUTE_PAY).build()
    DispatchClient.manager?.dispatch(context, req)
}

@Route(url = ROUTE_PAY)
@AndroidEntryPoint
class PayActivity : NMBActivity<ActivityPayBinding, PayVm>() {
    val vm: PayVm by viewModels()
    override fun getLayoutId() = R.layout.activity_pay

    override fun initViewModel() = vm

}

@HiltViewModel
class PayVm(app: Application) : FastViewModel(app) {

}