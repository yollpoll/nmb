package com.yollpoll.nmb.net

import com.yollpoll.nmb.APP_ID
import com.yollpoll.nmb.USER_AGENT
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class NMBInterceptor : Interceptor {
    //header
    private val headers = hashMapOf(
        "User-Agent" to USER_AGENT
    )
    private val commonParams = hashMapOf(
        "appid" to APP_ID
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val builder = oldRequest.newBuilder()
        injectParams(oldRequest, builder, commonParams)
        val newRequest = builder.headers(Headers.of(headers)).build()
        return chain.proceed(newRequest)
    }


    //注入参数
    private fun injectParams(
        request: Request,
        builder: Request.Builder,
        params: Map<String, String>
    ) {
        val httpUrlBuilder = request.url().newBuilder()
        params.forEach {
            httpUrlBuilder.addQueryParameter(it.key, it.value)
        }
        builder.url(httpUrlBuilder.build())
    }


}