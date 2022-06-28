package com.yollpoll.nmb.di

import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.nmb.net.commonRetrofitFactory
import com.yollpoll.nmb.net.launcherRetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class NetModuleDI {

    @ViewModelScoped
    @Provides
    @LauncherRetrofitFactory
    fun provideLauncherRetrofitFactory(): RetrofitFactory {
        return launcherRetrofitFactory
    }

    @ViewModelScoped
    @Provides
    @CommonRetrofitFactory
    fun provideCommonRetrofitFactory(): RetrofitFactory {
        return commonRetrofitFactory
    }

}