package com.yollpoll.nmb.view.activity

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.util.SharedPreferencesUtils
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.base.*
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.dispatch.StartType
import com.yollpoll.framework.extensions.saveBean
import com.yollpoll.framework.extensions.shortToast
import com.yollpoll.framework.extensions.toJson
import com.yollpoll.framework.extensions.toJsonBean
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.*
import com.yollpoll.nmb.BR
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityCookieBinding
import com.yollpoll.nmb.databinding.ItemCookieBinding
import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.model.bean.CookieBean
import com.yollpoll.nmb.model.repository.CookieRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_COOKIE
import com.yollpoll.nmb.router.ROUTE_QR
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okio.ByteString.Companion.encode
import java.lang.Exception
import java.util.*
import javax.inject.Inject

suspend fun gotoCookieActivity(context: Context) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("cookie").build()
    DispatchClient.manager?.dispatch(context, req)
}

const val REQUEST_PERMISSION = 1234
const val REQ_ADD_QR = 1234

@AndroidEntryPoint
@Route(url = ROUTE_COOKIE)
class CookieActivity : NMBActivity<ActivityCookieBinding, CookieVM>() {
    private val qrPermission = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val mAdapter = BaseAdapter<CookieBean>(
        layoutId = R.layout.item_cookie,
        BR.cookie,
        onBindViewHolder = { item, pos, vh ->
            if (null == item) {
                return@BaseAdapter
            }
            if (item.used == 1) {
                (vh.binding as ItemCookieBinding).tvStatus.text = "使用中"
            } else {
                (vh.binding as ItemCookieBinding).tvStatus.text = "未使用"
            }
            vh.binding.root.setOnClickListener {
                if (null == item) {
                    "饼干异常".shortToast()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    vm.useCookie(item)
                }
            }
            vh.binding.executePendingBindings()
        }, contentSame = { old, new ->
            old.name === new.cookie
        })
    private val vm: CookieVM by viewModels()
    override fun getLayoutId() = R.layout.activity_cookie
    override fun initViewModel() = vm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ADD_QR -> {
                if (resultCode == RESULT_OK) {
                    val cookieJson = data?.getStringExtra("cookie")
                    cookieJson?.let { json ->
                        "添加成功".shortToast()
                        vm.addCookie(json)
                    } ?: kotlin.run {
                        "添加失败".shortToast()
                    }
                }
            }
        }
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true)
        mDataBinding.rvCookie.adapter = mAdapter
        mDataBinding.rvCookie.layoutManager = LinearLayoutManager(this)
    }

    private fun initData() {
        refreshCookieList()
    }

    @OnMessage
    fun refreshCookieList() {
        lifecycleScope.launch {
            val data = vm.loadCookieList()
            mAdapter.submitData(data)
        }
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
            DispatchClient.manager?.dispatch(
                context,
                DispatchRequest.UrlBuilder(ROUTE_QR).startType(
                    StartType.START_FOR_RESULT,
                    REQ_ADD_QR
                ).build()
            )
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
class CookieVM @Inject constructor(val app: Application, val repository: CookieRepository) :
    FastViewModel(app) {

    suspend fun loadCookieList(): List<CookieBean> {
        val list = repository.queryCookies()
        list.forEach {
            it.toJson().logE()
        }
        return repository.queryCookies()
    }

    //添加饼干
    fun addCookie(cookieJson: String) {
        viewModelScope.launch {
            try {
                val cookie = cookieJson.toJsonBean<CookieBean>()
                if (null == cookie) {
                    "cookie解析出错".shortToast()
                    return@launch
                }
                useCookie(cookie)
                sendEmptyMessage(MR.CookieActivity_refreshCookieList)
            } catch (e: Exception) {
                "cookie解析出错".shortToast()
            }

        }
    }

    suspend fun useCookie(cookie: CookieBean) = withContext(Dispatchers.IO) {
        val curCookie = App.INSTANCE.cookie
        curCookie?.let {
            it.used = 0
            repository.updateCookie(it)
        }
        cookie.used = 1
        App.INSTANCE.cookie = cookie
        repository.insertCookie(cookie)
        sendEmptyMessage(MR.CookieActivity_refreshCookieList)
    }
}