package com.yollpoll.nmb.di

import com.yollpoll.framework.dispatch.DispatcherManager
import com.yollpoll.ilog.Ilog
import com.yollpoll.log.ILogImpl
import com.yollpoll.nmb.router.DispatchClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Created by spq on 2022/1/10
 */
@Module
@InstallIn(SingletonComponent::class)
class DispatchClientDi {

    @Provides
    fun getManager(): DispatcherManager {
        return DispatchClient.manager!!
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LogDi {
    @Binds
    abstract fun bindILog(ilog: ILogImpl): Ilog
}