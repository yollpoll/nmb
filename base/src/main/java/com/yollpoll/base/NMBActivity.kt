package com.yollpoll.base

import android.util.TypedValue
import androidx.databinding.ViewDataBinding
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel

/**
 * Created by spq on 2022/6/22
 */
abstract class NMBActivity<BIND : ViewDataBinding, VM : FastViewModel>:FastActivity<BIND,VM>() {
    override  fun getContentViewId()=getLayoutId()
    abstract fun getLayoutId():Int
    override fun getViewModel(): VM {
        mViewModel=initViewModel()
        return mViewModel
    }
    abstract fun initViewModel():VM


    /**
     * 获取attr中的颜色
     */
    open fun getAttrColor(id:Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(id, typedValue, true)
        return typedValue.data
    }

}