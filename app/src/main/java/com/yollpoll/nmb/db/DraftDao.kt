package com.yollpoll.nmb.db

import androidx.room.*
import com.yollpoll.nmb.model.bean.DraftBean
import kotlinx.coroutines.flow.Flow

/**
 * Created by spq on 2022/12/6
 */
@Dao
interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<DraftBean>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg bean: DraftBean)

    @Delete
    suspend fun delete(element: DraftBean)

    @Delete
    suspend fun deleteList(elements: List<DraftBean>)

    @Delete
    suspend fun deleteSome(vararg elements: DraftBean)

    @Update
    suspend fun update(vararg element: DraftBean)

    @Query("SELECT * FROM draft ORDER BY update_time DESC")
    suspend fun query(): List<DraftBean>

    @Query("SELECT * FROM draft ORDER BY update_time DESC")
    fun queryFlow(): Flow<List<DraftBean>>
}