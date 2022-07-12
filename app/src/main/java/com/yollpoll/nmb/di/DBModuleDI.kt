package com.yollpoll.nmb.di

import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.net.launcherRetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DBModuleDI {
    @Singleton
    @Provides
    fun provideDB(): MainDB {
        return MainDB.getInstance()
    }
}