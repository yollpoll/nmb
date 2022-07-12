package com.yollpoll.base

import android.annotation.SuppressLint
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.yollpoll.framework.extensions.dp2px

/**
 * Created by 鹏祺 on 2017/7/3.
 */
@SuppressLint("ValidFragment")
open class BaseDialogFragment : DialogFragment(), View.OnClickListener {
    override fun onStart() {
        super.onStart()
        val window = dialog!!.window
        val layoutParams = window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.horizontalMargin = requireContext().dp2px(50f)
        window.attributes = layoutParams
    }

    override fun onClick(v: View) {}
}