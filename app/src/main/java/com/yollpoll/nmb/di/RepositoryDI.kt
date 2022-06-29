package com.yollpoll.nmb.di

import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.HomeRepository
import com.yollpoll.nmb.model.repository.IRepository
import com.yollpoll.nmb.model.repository.LauncherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

//@Module
//@InstallIn(ViewModelComponent::class)
//abstract class LauncherRepositoryDI {
//    @Binds
//    @LauncherRepositoryAnnotation
//    abstract fun provideRepository(launcherRepository: LauncherRepository): IRepository
//
//    @Binds
//    @HomeRepositoryAnnotation
//    abstract fun provideHomeRepository(launcherRepository: HomeRepository): IRepository
//
//    @Binds
//    @ArticleDetailAnnotation
//    abstract fun provideArticleDetailRepository(repository: ArticleDetailRepository): IRepository
//}