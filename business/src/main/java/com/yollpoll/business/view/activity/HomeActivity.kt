package com.yollpoll.business.view.activity

import android.app.Application
import androidx.lifecycle.lifecycleScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.business.R
import com.yollpoll.business.ROUTE_HOME
import com.yollpoll.business.databinding.ActivityHomeBinding
import com.yollpoll.framework.extensions.saveBean
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.utils.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
@ViewModel(HomeVm::class)
@Route(url = ROUTE_HOME)
class HomeActivity: FastActivity<ActivityHomeBinding, HomeVm>() {
    override fun getContentViewId()= R.layout.activity_home
}

class HomeVm(app: Application): FastViewModel(app){

}