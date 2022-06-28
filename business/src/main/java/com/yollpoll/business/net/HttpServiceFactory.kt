package com.yollpoll.business.net

import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.net.http.RetrofitIntercept
import okhttp3.OkHttpClient
import retrofit2.Retrofit

public val launcherRetrofitFactory by lazy {
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
public val commonRetrofitFactory by lazy {
    RetrofitFactory(object : RetrofitIntercept {
        override fun baseUrl(): String {
            //如果是空的会报错
            return requireNotNull(realUrl)
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