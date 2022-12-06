package com.yollpoll.nmb.view.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.message.MessageManager
import com.yollpoll.base.CommonDialog
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.getAttrColor
import com.yollpoll.base.logI
import com.yollpoll.extensions.isDarkMod
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.utils.*
import com.yollpoll.nmb.*
import com.yollpoll.nmb.databinding.ActivitySettingBinding
import com.yollpoll.nmb.model.bean.DarkMod
import com.yollpoll.nmb.model.repository.UserRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_SETTING
import com.yollpoll.nmb.view.widgets.InputDialog
import com.yollpoll.nmb.view.widgets.SelectColorDialog
import com.yollpoll.skin.SkinTheme
import com.yollpoll.utils.copyStr
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

suspend fun gotoSetting(context: Context) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("setting").build()
    DispatchClient.manager?.dispatch(context, req)
//    DispatchClient.manager?.dispatch(context, DispatchRequest.UrlBuilder(ROUTE_SETTING).build())
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
        vm.init(context.getAttrColor(R.attr.colorOnSecondaryContainer))
    }

    private fun initView() {
        initTitle(mDataBinding.headerTitle.toolbar, true)
        mDataBinding.switchLog.setOnCheckedChangeListener { view, checked ->
            vm.openCrashHandler(checked)
        }
        initUITheme()
        initDarkMod()
        initCollection()
        mDataBinding.layoutMyCookieColor.setOnLongClickListener {
            vm.resetCookieColor(context.getAttrColor(R.attr.colorOnSecondaryContainer))
            true
        }
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
            }.filterIndexed { index: Int, s: String ->
                //暂时过滤复古怀旧和其他
                return@filterIndexed index <= 1
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


    fun changeCollectionId(view: View) {
        CommonDialog(
            "警告",
            "不建议修改订阅id,错误的订阅id会导致使用他人订阅的情况,请使在订阅页面使用导入功能。仍要修改请点击确定，旧的订阅号会保存到剪切板。",
            context
        ) {
            InputDialog(context, "修改订阅ID", "订阅ID", vm.collectionId) {
                it?.let {
                    vm.saveCollectionId(it)
                    copyStr(context, mDataBinding.tvCollectionId.text.toString())
                }
            }.show()
        }.show()
    }

    private fun initCollection() {
        mDataBinding.rlCollection.setOnLongClickListener {
            copyStr(context, mDataBinding.tvCollectionId.text.toString())
            "复制到剪切板".shortToast()
            true
        }
    }

    /*
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

    fun changeCookieColor(view: View) {
        vm.cookieColor?.let { color ->
            SelectColorDialog(context, color.red, color.blue, color.green) {
                vm.changeCookieColor(it)
            }.show()
        } ?: run {
            SelectColorDialog(context) {
                vm.changeCookieColor(it)
            }.show()
        }
    }

    @OnMessage
    fun notifyCookieColor(color: Int) {
        val shape = GradientDrawable()
        shape.cornerRadius = context.dp2px(20f)
        shape.setColor(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            shape.setStroke(
                context.dp2px(1f).toInt(),
                context.getColor(R.color.md_theme_dark_outline)
            )
        }
        mDataBinding.ivCookieColor.background = shape
    }

    fun gotoShield(view: View) {
        lifecycleScope.launch {
            gotoShieldActivity(context)
        }
    }

    fun gotoForumSetting(view: View) {
        lifecycleScope.launch {
            gotoForumSettingActivity(context)
        }
    }

    fun gotoDraftBox(view: View) {
        lifecycleScope.launch {
            gotoDraftActivity(context)
        }
    }
}

@HiltViewModel
class SettingVm @Inject constructor(
    val app: Application,
    val userRepository: UserRepository,
    val crashHandler: MyCrashHandler
) :
    FastViewModel(app) {
    var firstLoad = true

    @Bindable
    var collectionId: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.collectionId)
        }

    @Bindable
    var curDarkMode: DarkMod = DarkMod.AUTO

    @Bindable
    var openLog: Boolean = false

    @Bindable
    var cookieMod: Boolean = false
        set(value) {
            field = value
            setNoCookie(value)
            notifyPropertyChanged(BR.cookieMod)
        }

    @Bindable
    var noImgMod: Boolean = false
        set(value) {
            field = value
            setNoImage(value)
            notifyPropertyChanged(BR.noImgMod)
        }

    @Bindable
    var thumbBigImg: Boolean = false
        set(value) {
            field = value
            setBigImg(value)
            notifyPropertyChanged(BR.thumbBigImg)
        }

    var cookieColor: Int? = null
        set(value) {
            field = value
            sendMessage(MR.SettingActivity_notifyCookieColor, value)
        }

    fun init(defaultCookieColor: Int) {
        viewModelScope.launch {
            initLog()
            collectionId = userRepository.getCollectionId()
            cookieMod = getBoolean(KEY_NO_COOKIE, false)
            noImgMod = getBoolean(KEY_NO_IMG, false)
            thumbBigImg = getBoolean(KEY_BIG_IMG, false)
            cookieColor =
                getInt(KEY_COOKIE_COLOR, defaultCookieColor)
            firstLoad = false
        }
    }

    //保存新的collectionId
    fun saveCollectionId(id: String) {
        collectionId = id
        userRepository.updateCollectionId(id)
    }

    private fun initLog() {
        viewModelScope.launch {
            openLog = getBoolean(KEY_OPEN_CRASH_HANDLER, false)
            notifyPropertyChanged(BR.openLog)
        }
    }

    fun openCrashHandler(open: Boolean) {
        viewModelScope.launch {
            openLog = open
            putBoolean(KEY_OPEN_CRASH_HANDLER, open)
        }
    }

    private fun setNoCookie(check: Boolean) {
        if (firstLoad) return
        viewModelScope.launch {
            putBoolean(KEY_NO_COOKIE, check)
            sendMessage(ACTION_NO_COOKIE, check)
        }
    }

    private fun setNoImage(check: Boolean) {
        if (firstLoad) return
        viewModelScope.launch {
            putBoolean(KEY_NO_IMG, check)
            sendMessage(ACTION_NO_IMG, check)
        }
    }

    private fun setBigImg(check: Boolean) {
        if (firstLoad) return
        viewModelScope.launch {
            putBoolean(KEY_BIG_IMG, check)
            sendMessage(ACTION_BIG_IMG, check)
        }
    }

    fun changeCookieColor(color: Int) {
        cookieColor = color
        putInt(KEY_COOKIE_COLOR, color)
    }

    //恢复饼干颜色
    fun resetCookieColor(color: Int) {
        val sp = app.getSharedPreferences(CONFIG_FILE_NAME, Activity.MODE_PRIVATE)
        sp.edit().remove(KEY_COOKIE_COLOR).apply()
        cookieColor = getInt(KEY_COOKIE_COLOR, color)
    }
}

