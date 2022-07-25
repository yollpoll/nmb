package com.yollpoll.nmb.db

import androidx.room.*

@Dao
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(element: T)

    @Delete
    fun delete(element: T)

    @Delete
    fun deleteList(elements: List<T>)

    @Delete
    fun deleteSome(vararg elements: T)

    @Update
    fun update(element: T)

}