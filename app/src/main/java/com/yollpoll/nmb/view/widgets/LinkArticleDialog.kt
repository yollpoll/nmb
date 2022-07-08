package com.yollpoll.nmb.view.widgets

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.View
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.yollpoll.base.NMBDialog
import com.yollpoll.base.getAttrColor
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ItemThreadBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.utils.TransFormContent

class LinkArticleDialog(
    private val context: Context,
    private val item: ArticleItem,
    private val onUrlClick: ((String) -> Unit)? = null
) :
    NMBDialog<ItemThreadBinding, Dialog>(context) {
    override fun getLayoutId() = R.layout.item_thread

    override fun createDialog(context: Context): Dialog = Dialog(context)

    override fun onInit(dialog: Dialog?, binding: ItemThreadBinding?) {
        binding?.bean = item
        binding?.executePendingBindings()
        binding as ItemThreadBinding
        val context = binding.root.context
        //图片加载
        binding.ivContent.apply {
            if (item.img.isEmpty()) {
                this.visibility = View.GONE
            } else {

                this.visibility = View.VISIBLE
                //图片加载
                Glide.with(context)
                    .asBitmap()
                    .apply(getCommonGlideOptions(context))
                    .load(imgThumbUrl + item.img + item.ext)
                    .into(this)
            }
        }
        //回复数量
        item.ReplyCount.apply {
            if (this.isNotEmpty()) {
                item.ReplyCount = "0"
            }
        }
        item.admin.let {
            if (it == "1") {
                binding.tvUser.setTextColor(context.resources.getColor(R.color.color_red))
            } else if (item.master != null && item.master == "1") {
                binding.tvUser.setTextColor(context.resources.getColor(R.color.color_yellow_green))
            } else {
                binding.tvUser.setTextColor(
                    context.getAttrColor(
                        R.attr.colorOnSecondaryContainer
                    )
                )
            }
        }
        val htmlContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(item.content,
                HtmlCompat.FROM_HTML_MODE_COMPACT,
                {
//                            "image: ${it}".logE()
                    null
                }
            ) { opening, tag, output, xmlReader ->
            }
        } else {
            Html.fromHtml(item.content)
        }

        TransFormContent.trans(
            htmlContent,
            binding.tvContent,
        ) { url ->
            onUrlClick?.invoke(url)
        }
        binding.tvReplyCount.visibility = View.GONE
        binding.lineBottom.visibility = View.GONE
    }

    override fun onDialogDismiss(dialog: Dialog?) {
    }

    override fun onDialogShow(dialog: Dialog?) {
    }
}