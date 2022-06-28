package com.yollpoll.business.di

import com.yollpoll.business.model.repository.HomeRepository
import com.yollpoll.framework.net.http.RetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@InstallIn(ViewModelComponent::class)
@Module
class HomeModel {
    @Provides
    @ViewModelScoped
    fun provideRepository(@CommonRetrofitFactory retrofitFactory: RetrofitFactory): HomeRepository =
        HomeRepository(retrofitFactory)
}