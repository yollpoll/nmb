package com.yollpoll.skin

import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.view.View
import com.google.gson.annotations.Until
import com.yollpoll.base.R
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.dp2px
import com.yollpoll.utils.setViewMargin

/**
 * Created by spq on 2022/11/16
 */
val MaterialItemLine = Pair<String, SkinHandler>(ITEM_LINE) { parent, name, view, attrs ->
    view.visibility = View.GONE
    view
}
val MaterialItem = Pair<String, SkinHandler>(ITEM) { parent, name, view, attrs ->
    "view name: $name".logI()
    val context = view.context
    val bg = view.context.resources.getDrawable(R.drawable.shape_material_item, null)
    view.background = bg
    val animator: StateListAnimator =
        AnimatorInflater.loadStateListAnimator(view.context, R.animator.material_item_animator)
    view.stateListAnimator = animator
    view
}
val MaterialSettingItem = Pair<String, SkinHandler>(SETTING_ITEM) { parent, name, view, attrs ->
    "view name: $name".logI()
    val context = view.context
    val bg = view.context.resources.getDrawable(R.drawable.shape_material_item, null)
    view.background = bg
    val animator: StateListAnimator =
        AnimatorInflater.loadStateListAnimator(view.context, R.animator.material_item_animator)
    view.stateListAnimator = animator
    view.setPadding(
        context.dp2px(10f).toInt(),
        context.dp2px(5f).toInt(),
        context.dp2px(10f).toInt(),
        context.dp2px(5f).toInt()
    )
    view
}
val MaterialDialogBg = Pair<String, SkinHandler>(DIALOG_BG) { parent, name, view, attrs ->
    view
}
