package com.yollpoll.nmb.adapter

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
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

class ThreadAdapter(
    home: Boolean = true,
    onItemLongClick: ((ArticleItem) -> Boolean)? = null,
    onUrlClick: ((String) -> Unit)? = null,
    onImageClick: ((ArticleItem, Int) -> Unit)? = null,
    onItemClick: (((ArticleItem) -> Unit))? = null,
) :
    NmbPagingDataAdapter<ArticleItem>(
        R.layout.item_thread,
        BR.bean, onBindDataBinding = { item, pos, binding, payLoad ->
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
                        Glide.with(context)
                            .asBitmap()
                            .apply(getCommonGlideOptions(context))
                            .load(imgThumbUrl + item.img + item.ext)
                            .into(this)
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
                            val color=context.getAttrColor(
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
                    Html.fromHtml(item.content,
                        HtmlCompat.FROM_HTML_MODE_COMPACT,
                        {
//                            "image: ${it}".logE()
                            null
                        }
                    ) { opening, tag, output, xmlReader ->
//                        if (tag == "h") {
//                            output.setSpan(
//                                BackgroundColorSpan(context.getAttrColor(R.attr.colorPrimary)),
//                                0,
//                                output.length,
//                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                            )
//                            output.setSpan(
//                                ForegroundColorSpan(context.getAttrColor(R.attr.colorPrimary)),
//                                0,
//                                output.length,
//                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                            )
//                            output.setSpan(
//                                object : ClickableSpan() {
//                                    override fun updateDrawState(ds: TextPaint) {
//                                    }
//                                    override fun onClick(widget: View) {
//                                        "clock".shortToast()
//                                        //背景颜色
//                                        output.setSpan(
//                                            BackgroundColorSpan(context.getColor(R.color.transparent)),
//                                            0,
//                                            output.length,
//                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                                        )
//                                        //文字颜色
//                                        val color=context.getAttrColor(android.R.attr.textColor)
//                                        val colorHex="#FF"+Integer.toHexString(color).let {
//                                            var color=it
//                                            while (color.length<6){
//                                                color= "0$color"
//                                            }
//                                            return@let color
//                                        }
//                                        val realColor=Color.parseColor(colorHex)
//                                        output.setSpan(
//                                            ForegroundColorSpan(realColor),
//                                            0,
//                                            output.length,
//                                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                                        )
//                                    }
//                                },
//                                0,
//                                output.length,
//                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                            )
//                        }
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
                    binding.tvReplyCount.visibility = View.GONE
                    binding.tvContent.maxLines = Int.MAX_VALUE
                } else {
                    binding.tvReplyCount.visibility = View.VISIBLE
                    binding.tvContent.maxLines = 10
                }
                binding.ivContent.setOnClickListener {
                    onImageClick?.invoke(item, pos)
                }
                if (item.title.isEmpty()) {
                    binding.tvTitle.text = "无标题"
                } else {
                    binding.tvTitle.text = item.title
                }
                if (item.ReplyCount?.isEmpty() == true) {
                    binding.tvReplyCount.text = "回复:0"
                } else {
                    binding.tvReplyCount.text = "回复:${item.ReplyCount}"
                }
                binding.llRoot.setOnLongClickListener {
                    return@setOnLongClickListener onItemLongClick?.invoke(item) ?: false
                }
            }
        }, itemSame = { old, new ->
            old.id == new.id
        }, contentSame = { old, new ->
            val same =
                old.tagColor == new.tagColor && old.content == new.content && old.title == new.title
                        && old.user_hash == new.user_hash && old.now == new.now
            same
        }, getChangePayload = { old, new ->
            Bundle().apply {
                new.tagColor?.let {
                    if (old.tagColor != new.tagColor) {
                        putInt("tagColor", it)
                    }
                }

            }
        })