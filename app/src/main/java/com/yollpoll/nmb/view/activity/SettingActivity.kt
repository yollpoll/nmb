package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.message.MessageManager
import com.yollpoll.base.NMBActivity
import com.yollpoll.extensions.isDarkMod
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.*
import com.yollpoll.nmb.databinding.ActivitySettingBinding
import com.yollpoll.nmb.model.bean.DarkMod
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
    var first = true
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
        initUITheme()
        initDarkMod()
    }

    /**
     * 初始化ui主题
     */
    private fun initUITheme() {
        val mThemeAdapter = ArrayAdapter<String>(
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
                if (!first) {
                    MessageManager.getInstance()
                        .sendMessage(ACTION_SELECT_THEME, SkinTheme.values()[position])
                    App.INSTANCE.appSkinTheme = SkinTheme.values()[position]
                    refreshUI()
                } else {
                    first = false
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    /**
     * 初始化黑暗模式
     */
    private fun initDarkMod() {
        val adapterDark: ArrayAdapter<String> =
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                context.resources.getStringArray(R.array.dark_mod_item)
            )
        mDataBinding.spinnerDarkMod.adapter = adapterDark

        mDataBinding.spinnerDarkMod.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> selectDarkMode(DarkMod.DARK)
                        1 -> selectDarkMode(DarkMod.LIGHT)
                        2 -> selectDarkMode(DarkMod.AUTO)
                        else -> selectDarkMode(DarkMod.AUTO)
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        context.isDarkMod().let {
            if (it) {
                mDataBinding.spinnerDarkMod.setSelection(0)
            } else {
                mDataBinding.spinnerDarkMod.setSelection(1)
            }
        }
    }

    /**
     * 选择黑暗模式
     * @param mod DarkMod
     */
    fun selectDarkMode(mod: DarkMod) {
        vm.curDarkMode = mod
        when (mod) {
            DarkMod.DARK -> {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            }
            DarkMod.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
            DarkMod.AUTO -> {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    /**
     * 刷新ui
     */
    fun refreshUI() {
        mDataBinding = DataBindingUtil.setContentView<ActivitySettingBinding>(
            this@SettingActivity,
            R.layout.activity_setting
        )
        first = true
        initView()
    }

}

@HiltViewModel
class SettingVm @Inject constructor(val app: Application, val crashHandler: MyCrashHandler) :
    FastViewModel(app) {

    @Bindable
    var curDarkMode: DarkMod = DarkMod.AUTO

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

