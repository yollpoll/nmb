package com.yollpoll.nmb.view.widgets

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.yollpoll.base.NMBDialog
import com.yollpoll.framework.extensions.dp2px
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.DialogImportCollectionBinding

/**
 * Created by spq on 2022/11/8
 */
class ImportCollectionDialog(
    private val mContext: Context?,
    private val onOk: ((String?) -> Unit)? = null
) :
    NMBDialog<DialogImportCollectionBinding, Dialog>(mContext) {
    public var no: String? = null

    override fun getLayoutId() = R.layout.dialog_import_collection

    override fun createDialog(context: Context?) = Dialog(mContext)

    override fun onInit(dialog: Dialog?, binding: DialogImportCollectionBinding?) {
        super.onInit(dialog, binding)
        binding?.dialog = this
    }

    override fun onDialogShow(dialog: Dialog) {
        super.onDialogShow(dialog)
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()

        lp.copyFrom(dialog.window?.attributes)

        lp.width = mContext.dp2px(320F).toInt()

        lp.height = mContext.dp2px(200F).toInt()

        lp.horizontalMargin

        dialog.window?.attributes = lp
    }

    fun onOkClick() {
        onOk?.invoke(no)
        this.dismiss()
    }

    fun onCancelClick() {
        this.dismiss()
    }

}