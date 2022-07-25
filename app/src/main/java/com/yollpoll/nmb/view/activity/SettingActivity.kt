package com.yollpoll.nmb.view.activity

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.Bindable
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.permissionx.guolindev.PermissionX
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.utils.getBoolean
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.KEY_OPEN_CRASH_HANDLER
import com.yollpoll.nmb.MyCrashHandler
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivitySettingBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_SETTING
import com.yollpoll.utils.saveBitmapTpMediaStore
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

suspend fun gotoSetting(context: Context) {
    DispatchClient.manager?.dispatch(context, DispatchRequest.UrlBuilder(ROUTE_SETTING).build())
}

@AndroidEntryPoint
@Route(url = ROUTE_SETTING)
class SettingActivity : NMBActivity<ActivitySettingBinding, SettingVm>() {
    val vm: SettingVm by viewModels()
    override fun getLayoutId() = R.layout.activity_setting
    override fun initViewModel() = vm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTitle(mDataBinding.headerTitle.toolbar, true)
        mDataBinding.switchLog.setOnCheckedChangeListener { view, checked ->
            vm.openCrashHandler(checked)
        }
    }
}

@HiltViewModel
class SettingVm @Inject constructor(val app: Application, val crashHandler: MyCrashHandler) :
    FastViewModel(app) {
    @Bindable
    var openLog: Boolean = false
    init {
        initLog()
    }

    private fun initLog() {
        viewModelScope.launch {
            openLog = app.getBoolean(KEY_OPEN_CRASH_HANDLER, false)
            notifyPropertyChanged(BR.openLog)
        }
    }

    fun openCrashHandler(open: Boolean) {
        viewModelScope.launch {
            openLog=open
            app.putBoolean(KEY_OPEN_CRASH_HANDLER, open)
        }
    }
}