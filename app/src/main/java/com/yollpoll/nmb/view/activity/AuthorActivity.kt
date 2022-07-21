package com.yollpoll.nmb.view.activity

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.AUTHOR
import com.yollpoll.nmb.EMAIL
import com.yollpoll.nmb.GIT
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityAuthorBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_AUTHOR
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

suspend fun gotoAuthor(context: Context) {
    DispatchClient.manager?.dispatch(context, DispatchRequest.UrlBuilder(ROUTE_AUTHOR).build())
}

@AndroidEntryPoint
@Route(url = ROUTE_AUTHOR)
class AuthorActivity : NMBActivity<ActivityAuthorBinding, AuthorVm>() {
    val vm: AuthorVm by viewModels()
    override fun getLayoutId() = R.layout.activity_author

    override fun initViewModel() = vm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTitle(mDataBinding.headerTitle.toolbar, showBackBtn = true)
    }
    fun gotoGit(){
        lifecycleScope.launchWhenResumed {
            DispatchClient.manager?.dispatch(context,DispatchRequest.UrlBuilder(GIT).build())
        }
    }
}

@HiltViewModel
class AuthorVm @Inject constructor(val app: Application) : FastViewModel(app) {
    val author = AUTHOR
    val git = GIT
    val email= EMAIL
}