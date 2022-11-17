package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.message.MessageManager
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.*
import com.yollpoll.nmb.databinding.ActivitySettingBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_SETTING
import com.yollpoll.skin.SkinTheme
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
        initView()
    }

    private fun initView() {
        initTitle(mDataBinding.headerTitle.toolbar, true)
        mDataBinding.switchLog.setOnCheckedChangeListener { view, checked ->
            vm.openCrashHandler(checked)
        }
        //uiTheme
        var mThemeAdapter = ArrayAdapter<String>(
            this,
            R.layout.item_for_custom_spinner,
            SkinTheme.values().map {
                return@map when (it) {
                    SkinTheme.NULL -> "无"
                    SkinTheme.MATERIAL -> "Material Design"
                    SkinTheme.OLD_SCHOOL -> "复古怀旧"
                    SkinTheme.OTHER -> "其他"
                }
            }
        )
        mDataBinding.spinner.adapter = mThemeAdapter
        mDataBinding.spinner.setSelection(SkinTheme.values().indexOf(App.INSTANCE.appSkinTheme))
        mDataBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                MessageManager.getInstance().sendMessage(ACTION_SELECT_THEME,SkinTheme.values()[position])
                App.INSTANCE.appSkinTheme = SkinTheme.values()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    fun selectTheme(view: View) {

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
            openLog = open
            app.putBoolean(KEY_OPEN_CRASH_HANDLER, open)
        }
    }
}