package com.yollpoll.business.view.activity

import android.app.Application
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.ViewModel
import com.yollpoll.base.NMBActivity
import com.yollpoll.business.R
import com.yollpoll.business.ROUTE_HOME
import com.yollpoll.business.databinding.ActivityHomeBinding
import com.yollpoll.framework.extensions.saveBean
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.utils.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@Route(url = ROUTE_HOME)
@ViewModel(HomeVm::class)
class HomeActivity : NMBActivity<ActivityHomeBinding, HomeVm>() {
    override fun getLayoutId() = R.layout.activity_home
}

class HomeVm constructor(app: Application) : FastViewModel(app) {
    init {
        "vm".shortToast()
    }
}