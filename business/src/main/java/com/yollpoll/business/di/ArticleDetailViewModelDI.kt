/*
 * Copyright (c) 2021. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.yollpoll.business.di

import com.yollpoll.business.model.repository.ArticleDetailRepository
import com.yollpoll.framework.net.http.RetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class ArticleDetailViewModelDI {
    @ViewModelScoped
    @Provides
    fun provideRepository(@CommonRetrofitFactory retrofitFactory: RetrofitFactory) =
        ArticleDetailRepository(retrofitFactory)
}