package com.yollpoll.nmb.di

import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.nmb.model.repository.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)//生命周期
@Module
class AppWidgetDI {
    @AppWidgetRepository
    @Provides
    @Singleton//作用域
    fun provideRepository(@CommonRetrofitFactory retrofitFactory: RetrofitFactory): HomeRepository =
        HomeRepository(retrofitFactory)
}