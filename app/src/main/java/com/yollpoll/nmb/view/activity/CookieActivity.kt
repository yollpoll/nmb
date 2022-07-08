package com.yollpoll.nmb.view.activity

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.lifecycleScope
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.NMBActivity
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityCookieBinding
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_COOKIE
import com.yollpoll.nmb.router.ROUTE_QR
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.Cookie
import java.util.*
import javax.inject.Inject

suspend fun gotoCookieActivity(context: Context) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("cookie").build()
    DispatchClient.manager?.dispatch(context, req)
}

const val REQUEST_PERMISSION = 1234

@AndroidEntryPoint
@Route(url = ROUTE_COOKIE)
class CookieActivity : NMBActivity<ActivityCookieBinding, CookieVM>() {
    private val qrPermission = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val vm: CookieVM by viewModels()
    override fun getLayoutId() = R.layout.activity_cookie
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true)
    }

    private fun initData() {

    }

    //添加饼干
    fun addCookie() {
        val needRequest = arrayListOf<String>()
        qrPermission.forEach {
            val res = PermissionChecker.checkSelfPermission(context, it)
            if (res == PERMISSION_DENIED) {
                needRequest.add(it)
            }
        }
        if (needRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this, needRequest.toTypedArray(), REQUEST_PERMISSION
            )
        } else {
            //已经获取所有权限
            startAddCookie()
        }
    }

    //添加饼干
    private fun startAddCookie() {
        lifecycleScope.launch {
            DispatchClient.manager?.dispatch(context, DispatchRequest.UrlBuilder(ROUTE_QR).build())
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION -> {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PERMISSION_DENIED) {
                        //权限未通过
                        if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                            "添加饼干需要文件读写权限".shortToast()
                        } else if (permissions[i] == Manifest.permission.CAMERA) {
                            "添加饼干需要相机".shortToast()
                        }
                        return
                    }
                }
                //权限通过,开始添加饼干
                startAddCookie()
            }
        }
    }
}

@HiltViewModel
class CookieVM @Inject constructor(val app: Application) : FastViewModel(app) {

}