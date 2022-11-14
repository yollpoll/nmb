package com.yollpoll.nmb.model.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

/**
 * Created by spq on 2022/11/11
 */
@Entity
data class HistoryBean(
    val admin: String,
    val content: String,
    val email: String?,
    @PrimaryKey
    val id: Int,
    val name: String,
    val now: String,
    val resto: String?,
    val sage: String?,
    val title: String,
    val user_hash: String,
    @ColumnInfo(name = "update_time",defaultValue = "'CURRENT_TIMESTAMP'")
    var updateTime: Long,
)