package com.yollpoll.nmb.db

import androidx.room.*
import com.yollpoll.nmb.model.bean.ArticleItem
import com.yollpoll.nmb.model.bean.HistoryBean
import com.yollpoll.nmb.model.bean.MySpeechBean
import retrofit2.http.DELETE

/**
 * Created by spq on 2022/11/11
 */
@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll( mySpeechBean: List<HistoryBean>)

    @Query("DELETE FROM historybean")
    suspend fun clearAll()

    @Query("SELECT * FROM historybean ORDER BY update_time DESC")
    suspend fun query(): List<HistoryBean>

    @Delete
    suspend fun delete(bean:HistoryBean)

//    @Query("SELECT * FROM myspeechbean")
//    suspend fun query(): List<MySpeechBean>
}