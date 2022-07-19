package com.yollpoll.nmb.model.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity
data class MySpeechBean(
    val admin: Int,
    val content: String,
    val email: String,
    @PrimaryKey
    val id: Int,
    val name: String,
    val now: String,
    val resto: Int,
    val sage: Int,
    val title: String,
    val user_hash: String
)