package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.databinding.Bindable
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logI
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.startActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityWebBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.utils.copyStr
import javax.inject.Inject

suspend fun gotoWeb(context: Context, url: String, title: String? = "浏览器") {
    val intent = Intent(context, WebActivity::class.java)
    intent.putExtra("url", url)
    intent.putExtra("title", title)
    context.startActivity(intent)
}

/**
 * Created by 鹏祺 on 2017/6/29.
 */
class WebActivity : NMBActivity<ActivityWebBinding, WebVm>() {
    val vm: WebVm by viewModels()
    override fun getLayoutId() = R.layout.activity_web
    override fun initViewModel() = vm

    @Extra
    var url: String = ""

    @Extra
    var title: String? = "浏览器"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTitle(mDataBinding.headerTitle.toolbar, true) {
            if (mDataBinding.webView.canGoBack()) {
                mDataBinding.webView.goBack()
            } else {
                this@WebActivity.finish()
            }
        }
        initData()
    }

    override fun getMenuLayout() = R.menu.menu_web
    private fun initData() {
        vm.title = title ?: let { return@let "浏览器" }
        mDataBinding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
//                hideLoading()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
//                showLoading()
            }
        }
        val webSettings = mDataBinding.webView.settings
        webSettings.javaScriptEnabled = true
        mDataBinding.webView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy_url -> {
                copyStr(context, url)
                "复制到剪切板".shortToast()
            }
            R.id.browser -> {
                val uri: Uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }
        return true
    }

}

class WebVm @Inject constructor(val app: Application) : FastViewModel(app) {
    @Bindable
    var title: String = "浏览器"
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }
}