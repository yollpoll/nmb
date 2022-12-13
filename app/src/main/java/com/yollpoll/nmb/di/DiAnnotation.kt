package com.yollpoll.nmb.di

import javax.inject.Qualifier

//retrofitFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommonRetrofitFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LauncherRetrofitFactory

//repository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LauncherRepositoryAnnotation

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HomeRepositoryAnnotation

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticleDetailAnnotation

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppWidgetRepository