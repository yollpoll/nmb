package com.yollpoll.nmb.net

import com.yollpoll.base.logI
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * 根据文件创建requestbody
 *
 * @param file
 * @return
 */
fun getRequestBody(file: File?): RequestBody? {
    if (file == null) {
        return null
    }
    // 创建 RequestBody，用于封装构建RequestBody
    return RequestBody.create(MediaType.parse("image/*"), file)
}

/**
 * 根据文件创建requestbody
 *
 * @param content
 * @return
 */
fun getRequestBody(content: String?): RequestBody? {
    if (content == null) {
        return null
    }
    "getReqbody ".logI()
    // 创建 RequestBody，用于封装构建RequestBody
    return RequestBody.create(MediaType.parse("text/plain"), content)
}