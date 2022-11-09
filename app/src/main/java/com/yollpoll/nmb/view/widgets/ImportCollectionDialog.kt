package com.yollpoll.nmb.view.widgets

import android.app.Dialog
import android.content.Context
import com.yollpoll.base.NMBDialog
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.DialogImportCollectionBinding

/**
 * Created by spq on 2022/11/8
 */
class ImportCollectionDialog(private val mContext: Context?, private val onOk: ((String?) -> Unit)? = null) :
    NMBDialog<DialogImportCollectionBinding, Dialog>(mContext) {
    public var no: String? = null

    override fun getLayoutId() = R.layout.dialog_import_collection

    override fun createDialog(context: Context?) = Dialog(mContext)

    override fun onInit(dialog: Dialog?, binding: DialogImportCollectionBinding?) {
        super.onInit(dialog, binding)
        binding?.dialog = this
    }

    fun onOkClick() {
        onOk?.invoke(no)
        this.dismiss()
    }

    fun onCancelClick() {
        this.dismiss()
    }

}