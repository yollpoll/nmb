package com.yollpoll.business.di

import com.yollpoll.business.model.repository.LauncherRepository
import com.yollpoll.framework.net.http.RetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class LauncherViewModelDI {
    @ViewModelScoped
    @Provides
    fun provideRepository(@LauncherRetrofitFactory retrofitFactory: RetrofitFactory) =
        LauncherRepository(retrofitFactory)
}

