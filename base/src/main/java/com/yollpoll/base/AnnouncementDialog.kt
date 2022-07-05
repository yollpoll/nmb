package com.yollpoll.base

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.yollpoll.base.databinding.DialogCommonBinding
import com.yollpoll.framework.widgets.BaseDialog

class AnnouncementDialog(val content: String?, val context: Context) :
    NMBDialog<DialogCommonBinding, AlertDialog>(context) {
    override fun getLayoutId() = R.layout.dialog_common

    override fun createDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        return builder.create()
    }

    override fun onInit(dialog: AlertDialog, binding: DialogCommonBinding) {
        binding.content = content
    }

    override fun onDialogDismiss(dialog: AlertDialog) {
    }

    override fun onDialogShow(dialog: AlertDialog) {
    }

}