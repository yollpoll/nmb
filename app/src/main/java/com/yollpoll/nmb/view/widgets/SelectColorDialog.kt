package com.yollpoll.nmb.view.widgets

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.yollpoll.base.NMBDialog
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.DialogSelectColorBinding

class SelectColorDialog(
    context: Context,
    private var red: Int = 0,
    private var blue: Int = 0,
    private var green: Int = 0,
    private val onSelect: (Int) -> Unit
) :
    NMBDialog<DialogSelectColorBinding, Dialog>(context) {
    override fun getLayoutId() = R.layout.dialog_select_color

    override fun createDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        val window = dialog.window
        val layoutParams = window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = layoutParams
        return dialog
    }

    override fun onInit(dialog: Dialog, binding: DialogSelectColorBinding) {
        super.onInit(dialog, binding)

        val sbRed = binding.seekRed
        val sbGreen = binding.seekGreen
        val sbBlue = binding.seekBlue
        val tvOk = binding.tvOk
        val tvCancel = binding.tvCancel

        binding.rlColor.setBackgroundColor(Color.rgb(red, green, blue))
        sbRed.progress = red * 100 / 255
        sbGreen.progress = green * 100 / 255
        sbBlue.progress = blue * 100 / 255
        tvOk.setOnClickListener {
            onSelect(Color.rgb(red, green, blue))
            dialog.dismiss()
        }
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        sbRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                red = progress * 255 / 100
                binding.rlColor.setBackgroundColor(Color.rgb(red, green, blue))
                //                imgCache.setImageBitmap(mDrawView.getBitmapCache());
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sbGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                green = progress * 255 / 100
                binding.rlColor.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sbBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                blue = progress * 255 / 100
                binding.rlColor.setBackgroundColor(Color.rgb(red, green, blue))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
}
