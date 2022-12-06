package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.dp2px
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.framework.paging.getCommonPager
import com.yollpoll.framework.utils.getBoolean
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.KEY_NO_IMG
import com.yollpoll.nmb.MR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityDraftBinding
import com.yollpoll.nmb.databinding.ActivityDrawingBinding
import com.yollpoll.nmb.databinding.ItemDraftBinding
import com.yollpoll.nmb.databinding.ItemThreadBinding
import com.yollpoll.nmb.model.bean.DraftBean
import com.yollpoll.nmb.model.repository.DraftRepository
import com.yollpoll.nmb.net.imgThumbUrl
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_DRAFT
import com.yollpoll.nmb.router.ROUTE_HISTORY
import com.yollpoll.nmb.view.widgets.getCommonGlideOptions
import com.yollpoll.utils.TransFormContent
import com.yollpoll.utils.compressBitmap
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by spq on 2022/12/6
 */
const val ACTION_CONTINUE = "continue"
const val ACTION_SELECT = "select"

suspend fun gotoDraftActivity(context: Context, action: String = ACTION_CONTINUE) {
    val req = DispatchRequest.UrlBuilder(ROUTE_DRAFT).params(
        hashMapOf(
            "action" to action
        )
    ).build()
    DispatchClient.manager
    DispatchClient.manager?.dispatch(context, req)
}

@AndroidEntryPoint
@Route(url = ROUTE_DRAFT)
class DraftActivity : NMBActivity<ActivityDraftBinding, DraftVM>() {
    val vm: DraftVM by viewModels()

    @Extra
    lateinit var action: String
    override fun getLayoutId() = R.layout.activity_draft
    override fun initViewModel() = vm
    val adapter = BaseAdapter<DraftBean>(
        layoutId = R.layout.item_draft,
        variableId = BR.bean,
        onBindViewHolder = { item, pos, vh, payLoad ->
            if (null != item) {
                val binding = vh.binding as ItemDraftBinding
                val context = binding.root.context
                //图片加载
                item.img?.also { path ->
                    val file = File(path)
                    if (!file.exists()) {
                        binding.ivContent.visibility = View.GONE
                    } else {
                        Glide.with(context).asBitmap()
                            .apply(getCommonGlideOptions(context))
                            .load(file)
                            .into(binding.ivContent)
                        binding.ivContent.visibility = View.VISIBLE
                    }
                } ?: also {
                    binding.ivContent.visibility = View.GONE
                }

                binding.llRoot.setOnClickListener {
                    //点击事件
                    lifecycleScope.launch {
                        if (action == ACTION_SELECT) {
                            //选择内容
                            vm.selectDraft(item)
                            this@DraftActivity.finish()
                        } else {
                            gotoNewThreadWithDraft(context, draft = item)
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
                    }
                } else {
                    Html.fromHtml(item.content)
                }
                //文本修改为自定义编辑的模式
                TransFormContent.trans(
                    htmlContent,
                    binding.tvContent,
                ) { url ->
                    //url click
                }
                binding.tvContent.maxLines = Int.MAX_VALUE

                if (item.title.isNullOrEmpty()) {
                    binding.tvTitle.text = "无标题"
                } else {
                    binding.tvTitle.text = "标题:" + item.title
                }

                binding.llRoot.setOnLongClickListener {
                    //删除
                    CommonDialog("删除草稿", "是否删除草稿", context) {
                        vm.delDraft(item)
                    }.show()
                    true
                }
                binding.tvTime.text = draftDF.format(Date(item.updateTime))
                item.reply?.also {
                    binding.tvReply.text = ">>No." + item.reply
                    binding.tvReply.visibility = View.VISIBLE
                } ?: let {
                    binding.tvReply.visibility = View.GONE
                }
                binding.tvForum.text = "板块: " + item.fName
            }
        })
    val layoutManager = LinearLayoutManager(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTitle(mDataBinding.layoutHead.toolbar, true)
        lifecycleScope.launch {
            vm.draftList.collectLatest {
                adapter.submitData(it)
            }
        }
    }
}

@HiltViewModel
class DraftVM @Inject constructor(app: Application, val repository: DraftRepository) :
    FastViewModel(app) {
    val draftList = repository.getDraftFlow()

    fun delDraft(item: DraftBean) {
        viewModelScope.launch {
            repository.delDraft(item)
        }
    }

    fun selectDraft(draft: DraftBean) {
        sendMessage(MR.NewThreadActivity_onDraftSelect, draft)
    }

}

val draftDF = SimpleDateFormat("保存时间: yyyy-MM-dd hh:mm:ss")