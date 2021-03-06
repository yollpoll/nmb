package com.yollpoll.nmb.adapter

import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.Html
import android.view.View
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.yollpoll.base.NmbPagingDataAdapter
import com.yollpoll.base.getAttrColor
import com.yollpoll.base.logE
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ItemForumBinding
import com.yollpoll.nmb.databinding.ItemThreadBinding
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.view.activity.ThreadDetailActivity
import com.yollpoll.nmb.view.widgets.getCommonGlideOptions
import com.yollpoll.utils.TransFormContent
import kotlinx.coroutines.launch
import org.xml.sax.XMLReader

class ThreadAdapter(
    home: Boolean = true,
    onItemLongClick: ((ArticleItem) -> Boolean)? = null,
    onUrlClick: ((String) -> Unit)? = null,
    onImageClick: ((ArticleItem, Int) -> Unit)? = null,
    onItemClick: (((ArticleItem) -> Unit))? = null,
) :
    NmbPagingDataAdapter<ArticleItem>(
        R.layout.item_thread,
        BR.bean, onBindDataBinding = { item, pos, binding ->
            if (null != item) {
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
                if(item.ReplyCount==null){
                    item.ReplyCount="0"
                }
                binding.llRoot.setOnClickListener {
                    onItemClick?.invoke(item)
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
//                        "tag: ${tag} outPut ${output}".logE()
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
        })