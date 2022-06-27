package com.yollpoll.base

import androidx.databinding.ViewDataBinding
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel

/**
 * Created by spq on 2022/6/22
 */
open class NMBActivity<BIND : ViewDataBinding, VM : FastViewModel>:FastActivity<BIND,VM>() {
}