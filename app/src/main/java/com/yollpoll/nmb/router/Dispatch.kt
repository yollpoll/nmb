package com.yollpoll.nmb.router

import com.yollpoll.framework.dispatch.DispatchInterceptor
import com.yollpoll.framework.dispatch.DispatchResponse
import com.yollpoll.framework.dispatch.DispatcherManager
import com.yollpoll.framework.extensions.toMapJson
import com.yollpoll.nmb.BuildConfig
import java.util.logging.Logger

/**
 * Created by spq on 2022/1/10
 */
private const val TAG = "Dispatch"

object DispatchClient {
    var manager: DispatcherManager? = null
        get() {
            if (null == field) {
                field = DispatcherManager.ManagerBuilder().apply {
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
//        LogUtils.i("DispatchLog: url is $url params is ${param.toMapJson()}")
        return chain.proceed(request)
    }
}
