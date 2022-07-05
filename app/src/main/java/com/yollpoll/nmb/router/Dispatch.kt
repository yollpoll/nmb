package com.yollpoll.nmb.router

import android.content.Intent
import android.net.Uri
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.framework.dispatch.DispatchInterceptor
import com.yollpoll.framework.dispatch.DispatchResponse
import com.yollpoll.framework.dispatch.DispatcherManager
import com.yollpoll.framework.extensions.toMapJson
import com.yollpoll.nmb.App
import com.yollpoll.nmb.BuildConfig

/**
 * Created by spq on 2022/1/10
 */
private const val TAG = "Dispatch"

object DispatchClient {
    var manager: DispatcherManager? = null
        get() {
            if (null == field) {
                field = DispatcherManager.ManagerBuilder().apply {
                    this.addInterceptors(WebDispatchInterceptor)
                    if (BuildConfig.DEBUG) {
                        this.addInterceptors(DispatcherLogInterceptor)
                    }
                }
                    .build()
            }
            return field!!
        }
}

/**
 * 记录每次跳转日志
 */
object DispatcherLogInterceptor : DispatchInterceptor {
    override suspend fun intercept(chain: DispatchInterceptor.Chain): DispatchResponse {
        val request = chain.getRequest()
        val url = request.url
        val param = request.params
        LogUtils.i("DispatchLog: url is $url params is ${param.toMapJson<String, String>()}")
        return chain.proceed(request)
    }
}

/**
 * webview跳转拦截
 */
object WebDispatchInterceptor : DispatchInterceptor {
    override suspend fun intercept(chain: DispatchInterceptor.Chain): DispatchResponse {
        val request = chain.getRequest()
        val url = request.url
        if (url.startsWith("http") || url.startsWith("https")) {
            val uri: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(chain.getContext().packageManager) != null) {
                chain.getContext().startActivity(intent)
            } else {
                val resBuilder = DispatchResponse.Builder()
                resBuilder.result = false
                resBuilder.request = request
                resBuilder.params = hashMapOf("网址错误" to "result")
                return resBuilder.build()
            }
        }
        return chain.proceed(request)
    }

}
