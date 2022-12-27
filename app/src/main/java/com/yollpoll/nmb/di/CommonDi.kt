package com.yollpoll.nmb.di

import com.yollpoll.framework.dispatch.DispatcherManager
import com.yollpoll.framework.net.http.RetrofitFactory
import com.yollpoll.ilog.Ilog
import com.yollpoll.log.ILogImpl
import com.yollpoll.nmb.Iqr
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.qrlib.QrUtils
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by spq on 2022/1/10
 */
@Module
@InstallIn(SingletonComponent::class)
class DispatchClientDi {

    @Provides
    fun getManager(): DispatcherManager {
        return DispatchClient.manager
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LogDi {
    @Binds
    abstract fun bindILog(ilog: ILogImpl): Ilog
}

@Module
@InstallIn(SingletonComponent::class)//生命周期
abstract class QrDi {
    @Binds
    abstract fun bindILog(iqr: QrUtils): Iqr
}