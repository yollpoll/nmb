package com.yollpoll.nmb.model.repository

import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.model.bean.CookieBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CookieRepository @Inject constructor(val db: MainDB) : IRepository {
    suspend fun queryCookies(): List<CookieBean> = withContext(Dispatchers.IO) {
        db.getCookieDao().queryAll()
    }

    suspend fun insertCookie(vararg cookie: CookieBean) = withContext(Dispatchers.IO) {
        db.getCookieDao().insertAll(cookie.toList())
    }

    suspend fun updateCookie(cookie: CookieBean) = withContext(Dispatchers.IO) {
        db.getCookieDao().update(cookie)
    }
}