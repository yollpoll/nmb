package com.yollpoll.nmb.adapter

import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MaskFilterSpan
import android.view.View
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.yollpoll.base.NmbPagingDataAdapter
import com.yollpoll.base.getAttrColor
import com.yollpoll.base.logE
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.getBoolean
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.paging.BaseViewHolder
import com.yollpoll.framework.utils.getBoolean
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.KEY_NO_IMG
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ItemThreadBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.view.widgets.getCommonGlideOptions
import com.yollpoll.utils.MyClickableSpan
import com.yollpoll.utils.TransFormContent
import java.util.regex.Pattern


class ThreadAdapter(
    val home: Boolean = true,
    val onItemLongClick: ((ArticleItem) -> Boolean)? = null,
    val onUrlClick: ((String) -> Unit)? = null,
    val onImageClick: ((ArticleItem, Int) -> Unit)? = null,
    val onItemClick: (((ArticleItem) -> Unit))? = null,
) : NmbPagingDataAdapter<ArticleItem>(R.layout.item_thread, BR.bean, itemSame = { old, new ->
    old.id == new.id
}, contentSame = { old, new ->
    val same =
        old.tagColor == new.tagColor && old.content == new.content && old.title == new.title && old.user_hash == new.user_hash && old.now == new.now
    same
}, getChangePayload = { old, new ->
    Bundle().apply {
        new.tagColor?.let {
            if (old.tagColor != new.tagColor) {
                putInt("tagColor", it)
            }
        }

    }
}) {

    override fun onBindViewHolder(
        holder: BaseViewHolder<ArticleItem>, position: Int, payLoad: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payLoad)
        val pos = holder.bindingAdapterPosition
        val item = getItem(pos)
        val binding = holder.binding

        if (null != item) {
            binding as ItemThreadBinding
            val context = binding.root.context
            //图片加载
            val noImg = getBoolean(KEY_NO_IMG, false)
            binding.ivContent.apply {
                if (item.img.isEmpty()) {
                    this.visibility = View.GONE
                } else {
                    if (noImg) {
                        this.visibility = View.GONE
                        return@apply
                    } else {
                        this.visibility = View.VISIBLE
                    }
                    //图片加载
                    Glide.with(context).asBitmap().apply(getCommonGlideOptions(context))
                        .load(imgThumbUrl + item.img + item.ext).into(this)
                }
            }
            //回复数量
            if (item.ReplyCount == null) {
                item.ReplyCount = "0"
            }
            binding.llRoot.setOnClickListener {
                onItemClick?.invoke(item)
            }
            //admin、po的颜色
            item.admin.let {
                if (it == "1") {
                    binding.tvUser.setTextColor(context.resources.getColor(R.color.color_red))
                } else if (item.master != null && item.master == "1") {
                    binding.tvUser.setTextColor(context.resources.getColor(R.color.color_yellow_green))
                } else {
                    //设置标记颜色
                    item.tagColor?.let {
                        binding.tvUser.setTextColor(it)
                        binding.executePendingBindings()
                    } ?: run {
                        val color = context.getAttrColor(
                            R.attr.colorOnSecondaryContainer
                        )
                        "color: $color".logI()
                        binding.tvUser.setTextColor(
                            context.getAttrColor(
                                R.attr.colorOnSecondaryContainer
                            )
                        )
                    }
                }
            }
            val htmlContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(item.content, HtmlCompat.FROM_HTML_MODE_COMPACT, {
//                            "image: ${it}".logE()
                    null
                }) { opening, tag, output, xmlReader ->
                    if (tag == "h") {
//                        output.setSpan(
//                            BackgroundColorSpan(context.getAttrColor(R.attr.colorPrimary)),
//                            0,
//                            output.length,
//                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                        )
//                        output.setSpan(
//                            ForegroundColorSpan(context.getAttrColor(R.attr.colorPrimary)),
//                            0,
//                            output.length,
//                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                        )
                    }
                }
            } else {
                Html.fromHtml(item.content)
            }
            //文本修改为自定义编辑的模式
            TransFormContent.trans(
                htmlContent,
                binding.tvContent,
            ) { url ->
                onUrlClick?.invoke(url)
            }
            if (!home) {
                binding.tvContent.maxLines = Int.MAX_VALUE
                item.index?.run {
                    if (this == 0) {
                        binding.tvReplyCount.text = "主楼"
                    } else {
                        binding.tvReplyCount.text = "${item.index}楼"
                    }
                    binding.tvReplyCount.visibility = View.VISIBLE
                } ?: run {
                    binding.tvReplyCount.visibility = View.GONE
                }
            } else {
                binding.tvReplyCount.visibility = View.VISIBLE
                binding.tvContent.maxLines = 10

                if (item.ReplyCount?.isEmpty() == true) {
                    binding.tvReplyCount.text = "回复:0"
                } else {
                    binding.tvReplyCount.text = "回复:${item.ReplyCount}"
                }
            }
            binding.ivContent.setOnClickListener {
                onImageClick?.invoke(item, pos)
            }
            if (item.title.isEmpty()) {
                binding.tvTitle.text = "无标题"
            } else {
                binding.tvTitle.text = item.title
            }

            binding.llRoot.setOnLongClickListener {
                return@setOnLongClickListener onItemLongClick?.invoke(item) ?: false
            }
        }

    }
}