package com.yollpoll.base

import android.app.ActionBar
import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.yollpoll.base.databinding.DialogLayoutBinding
import com.yollpoll.framework.extensions.dp2px

/**
 * Created by spq on 2022/11/11
 */
class CommonDialog(
    val title: String,
    val content: String,
    val mContext: Context,
    val onCancel: (() -> Unit)? = null,
    val onOk: (() -> Unit)? = null
) : NMBDialog<DialogLayoutBinding, AlertDialog>(mContext) {
    override fun getLayoutId() = R.layout.dialog_layout

    override fun createDialog(context: Context): AlertDialog {
        return AlertDialog.Builder(context).create()
    }

    override fun onInit(dialog: AlertDialog?, binding: DialogLayoutBinding?) {
        super.onInit(dialog, binding)
        binding?.let {
            it.dialog = this
            it.executePendingBindings()
        }
    }

    override fun onDialogShow(dialog: AlertDialog) {
        super.onDialogShow(dialog)
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()

        lp.copyFrom(dialog.window?.attributes)

        lp.width = mContext.dp2px(320F).toInt()

        lp.height = mContext.dp2px(200F).toInt()

        dialog.window?.attributes = lp
    }

    fun onOk() {
        onOk?.invoke()
        this.dismiss()
    }

    fun onCancel() {
        onCancel?.invoke()
        this.dismiss()
    }
}