package com.yollpoll.nmb.view.widgets

import android.app.Dialog
import android.content.Context
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.DialogCompat
import com.yollpoll.base.NMBDialog
import com.yollpoll.framework.widgets.BaseDialog
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.DialogSelectPageBinding

class SelectPageDialog(
    private val cur: Int,
    private val max: Int,
    private val context: Context,
    private val onSelected: ((Int) -> Unit)? = null
) :
    NMBDialog<DialogSelectPageBinding, Dialog>(context) {
    var selectPage: Int = cur
    override fun getLayoutId() = R.layout.dialog_select_page

    override fun createDialog(context: Context) = Dialog(context)

    override fun onInit(dialog: Dialog?, binding: DialogSelectPageBinding) {
        binding.max = max
        binding.cur = cur
        binding.selected = selectPage
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectPage = progress + 1
                binding.selected = selectPage
                binding.executePendingBindings()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        binding.executePendingBindings()
        binding.btnOk.setOnClickListener {
            onSelected?.invoke(selectPage)
            this.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }
    }

}
