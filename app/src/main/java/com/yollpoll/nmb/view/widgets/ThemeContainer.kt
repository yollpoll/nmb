package com.yollpoll.base

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import java.util.jar.Attributes

/**
 * Created by spq on 2022/11/14
 */
class ThemeContainer : ViewGroup {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributes,
        defStyleAttr
    )


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

    }

}