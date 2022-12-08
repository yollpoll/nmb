package com.yollpoll.nmb.di

import com.yollpoll.arch.log.LogUtils
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.framework.net.http.RetrofitIntercept
import com.yollpoll.nmb.net.BASE_URL
import com.yollpoll.nmb.net.CoverImgInterceptor
import com.yollpoll.nmb.net.commonRetrofitFactory
import com.yollpoll.nmb.net.launcherRetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)//生命周期
class NetModuleDI {

    @Singleton
    @Provides
    @LauncherRetrofitFactory
    fun provideLauncherRetrofitFactory(): RetrofitFactory {
        return launcherRetrofitFactory
    }

    @Singleton
    @Provides
    @CommonRetrofitFactory
    fun provideCommonRetrofitFactory(): RetrofitFactory {
        return commonRetrofitFactory
    }

}