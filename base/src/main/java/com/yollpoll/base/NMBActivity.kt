package com.yollpoll.base

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.widget.Toolbar
import androidx.core.view.LayoutInflaterCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import com.yollpoll.arch.message.liveeventbus.LiveEventBus
import com.yollpoll.arch.message.liveeventbus.NonType
import com.yollpoll.arch.message.liveeventbus.ObserverWrapper
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.ACTION_NO_COOKIE
import com.yollpoll.nmb.ACTION_NO_IMG
import com.yollpoll.nmb.ACTION_SELECT_THEME
import com.yollpoll.skin.SkinInflaterFactory
import com.yollpoll.skin.SkinTheme

/**
 * Created by spq on 2022/6/22
 */
abstract class NMBActivity<BIND : ViewDataBinding, VM : FastViewModel> : FastActivity<BIND, VM>() {
    //兼容底层框架和hilt的冲突
    override fun getContentViewId() = getLayoutId()
    abstract fun getLayoutId(): Int
    override fun getViewModel(): VM {
        mViewModel = initViewModel()
        return mViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(layoutInflater, SkinInflaterFactory)
        super.onCreate(savedInstanceState)
        initActionEvent()
    }


    private fun initActionEvent() {
        LiveEventBus.use(ACTION_SELECT_THEME, SkinTheme::class.java)
            .observe(this, object : ObserverWrapper<SkinTheme>() {
                override fun isSticky() = true
                override fun onChanged(value: SkinTheme?) {
                    value?.let {
                        onUiActionEvent(ACTION_SELECT_THEME, value)
                    }
                }

                override fun mainThread() = true

            })
        LiveEventBus.use(ACTION_NO_COOKIE, Boolean::class.java)
            .observe(this, object : ObserverWrapper<Boolean>() {
                override fun isSticky() = true
                override fun onChanged(value: Boolean) {
                    onUiActionEvent(ACTION_NO_COOKIE, value)
                }

                override fun mainThread() = true

            })
        LiveEventBus.use(ACTION_NO_IMG, Boolean::class.java)
            .observe(this, object : ObserverWrapper<Boolean>() {
                override fun isSticky() = true
                override fun onChanged(value: Boolean) {
                    onUiActionEvent(ACTION_NO_IMG, value)
                }

                override fun mainThread() = true

            })
    }


    /**
     * 获取attr中的颜色
     */
    open fun getAttrColor(id: Int): Int {
        return context.getAttrColor(id)
    }

    /**
     * 初始化标题栏
     */
    fun initTitle(
        toolbar: Toolbar,
        showBackBtn: Boolean = false,
        onBackClick: ((View) -> Unit)? = { this.finish() }
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(showBackBtn)
        toolbar.setNavigationOnClickListener {
            onBackClick?.invoke(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuId = getMenuLayout()
        return if (null != menuId) {
            menuInflater.inflate(menuId, menu)
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    open fun getMenuLayout(): Int? {
        return null
    }

    //一些全局通知的ui事件
    open fun onUiActionEvent(action: String, data: Any? = null) {}

    abstract fun initViewModel(): VM


}