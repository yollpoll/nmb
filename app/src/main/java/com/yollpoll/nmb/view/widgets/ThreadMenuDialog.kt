package com.yollpoll.nmb.view.widgets

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.yollpoll.base.NMBDialog
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.DialogThreadMenuBinding


class ThreadMenuDialog(
    private val action: MenuAction,
    private val context: Context,
    private val reply: (() -> Unit)? = null,
    private val report: (() -> Unit)? = null,
    private val copy: (() -> Unit)? = null,
    private val copyNo: (() -> Unit)? = null
) :
    NMBDialog<DialogThreadMenuBinding, AlertDialog>(context) {
    override fun getLayoutId() = R.layout.dialog_thread_menu

    override fun createDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        return builder.create()
    }

    override fun onInit(dialog: AlertDialog, binding: DialogThreadMenuBinding) {
        super.onInit(dialog, binding)
        when (action) {
            MenuAction.MENU_ACTION_HOME -> {
                binding.llReply.visibility = View.GONE
            }
            MenuAction.MENU_ACTION_REPLY -> {

            }
        }
        binding.llReply.setOnClickListener {
            reply?.invoke()
            this.dismiss()
        }
        binding.llReport.setOnClickListener {
            report?.invoke()
            this.dismiss()
        }
        binding.llCopy.setOnClickListener {
            copy?.invoke()
            this.dismiss()
        }
        binding.llCopyNo.setOnClickListener {
            copyNo?.invoke()
            this.dismiss()
        }
    }
}

enum class MenuAction {
    MENU_ACTION_HOME,
    MENU_ACTION_REPLY
}