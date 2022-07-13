package com.yollpoll.nmb.net

import android.util.Log
import com.yollpoll.base.logI
import okhttp3.Interceptor
import okhttp3.Response

private const val TAG = "CoverImgInterceptor"

class CoverImgInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val orgUrl = chain.request().url().toString()
            "url:${orgUrl}".logI()
            val response = chain.proceed(chain.request())
            val realUrl = response.request().url().toString()
            if (orgUrl == COVER) {
                Log.d(TAG, "intercept: realUrl$realUrl")
                //拦截到封面请求
                realCover = realUrl
            }
            return response
        } catch (e: Exception) {

        }
        return chain.proceed(chain.request())
    }
}