package com.yollpoll.nmb.model.bean

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CookieBean(val cookie: String, val name: String)