package com.yollpoll.nmb.net

import androidx.datastore.preferences.preferencesDataStore
import com.yollpoll.base.logI
import com.yollpoll.framework.extensions.getBean
import com.yollpoll.nmb.App
import com.yollpoll.nmb.model.bean.CookieBean
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.net.URL

val cookieStore = hashMapOf<String, MutableList<Cookie>>()

class LocalCookieJar : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        cookieStore[url.host()] = cookies
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        val builder = Cookie.Builder()
        val list = arrayListOf<Cookie>()
        App.INSTANCE.cookie?.run {
            "find cookie: ${this.name}".logI()
            val cookie =
                builder.name("userhash").value(this.cookie).domain(url.host()).build()
            list.add(cookie)
        }
        return list
    }
}