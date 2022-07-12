package com.yollpoll.nmb.db

import androidx.room.Dao
import androidx.room.Query
import com.yollpoll.nmb.model.bean.CookieBean

@Dao
interface CookieDao : BaseDao<CookieBean> {
    @Query("SELECT * FROM COOKIEBEAN ORDER BY used DESC")
    suspend fun queryAll(): List<CookieBean>

    @Query("SELECT * FROM COOKIEBEAN WHERE USED LIKE 1 ")
    suspend fun queryUsed(): CookieBean?
}