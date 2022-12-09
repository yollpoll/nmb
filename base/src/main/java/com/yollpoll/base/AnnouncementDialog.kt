package com.yollpoll.base

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.text.Html
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.yollpoll.base.databinding.DialogCommonBinding
import com.yollpoll.framework.widgets.BaseDialog
import com.yollpoll.utils.TransFormContent

class AnnouncementDialog(
    val content: String?,
    val context: Context,
    private val onUrlClick: ((String) -> Unit)? = null
) :
    NMBDialog<DialogCommonBinding, AlertDialog>(context) {
    override fun getLayoutId() = R.layout.dialog_common

    override fun createDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        return builder.create()
    }

    override fun onInit(dialog: AlertDialog, binding: DialogCommonBinding) {
        //文本修改为自定义编辑的模式
        if (content?.isNotEmpty() == true) {
            TransFormContent.trans(
                Html.fromHtml(content),
                binding.tvDialog,
            ) { url ->
                onUrlClick?.invoke(url)
            }
        }
    }

    override fun onDialogDismiss(dialog: AlertDialog) {
    }

    override fun onDialogShow(dialog: AlertDialog) {
    }

}