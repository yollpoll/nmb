package com.yollpoll.nmb.net

import com.yollpoll.arch.log.LogUtils
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.net.http.RetrofitIntercept
import okhttp3.OkHttpClient
import retrofit2.Retrofit

val launcherRetrofitFactory by lazy {
    RetrofitFactory(object : RetrofitIntercept {
        override fun baseUrl(): String {
            return BASE_URL
        }

        override fun okHttpClient(client: OkHttpClient) {
        }

        override fun okHttpClientBuilder(builder: OkHttpClient.Builder) {
            builder.addInterceptor(CoverImgInterceptor())

        }


        override fun retrofit(retrofit: Retrofit) {
        }

        override fun retrofitBuilder(builder: Retrofit.Builder) {
//            builder.addConverterFactory(MoshiConverterFactory.create())
        }

    })
}
val commonRetrofitFactory by lazy {
    RetrofitFactory(object : RetrofitIntercept {
        override fun baseUrl(): String {
            return BASE_URL
        }

        override fun okHttpClient(client: OkHttpClient) {
        }

        override fun okHttpClientBuilder(builder: OkHttpClient.Builder) {
            builder.addInterceptor(LoggerInterceptor())
            builder.addInterceptor(CoverImgInterceptor())
            builder.addInterceptor(NMBInterceptor())
            builder.cookieJar(LocalCookieJar())

        }


        override fun retrofit(retrofit: Retrofit) {
        }

        override fun retrofitBuilder(builder: Retrofit.Builder) {
//            builder.addConverterFactory(MoshiConverterFactory.create())
        }

    })

}