package com.yollpoll.nmb.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yollpoll.nmb.model.bean.MySpeechBean

@Dao
interface MySpeechDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll( mySpeechBean: List<MySpeechBean>)

    @Query("DELETE FROM myspeechbean")
    suspend fun clearAll()

    @Query("SELECT * FROM myspeechbean")
    suspend fun query(): List<MySpeechBean>

//    @Query("SELECT * FROM myspeechbean")
//    suspend fun query(): List<MySpeechBean>
}