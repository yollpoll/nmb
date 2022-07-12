package com.yollpoll.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Message
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import java.io.File
import java.lang.Error
import java.lang.reflect.Field


/**
 * Created by 鹏祺 on 2017/5/18.
 */
class ImageDownloader {
    /**
     *   @param path 路径要注意权限
     */
    suspend fun download(
        imageName: String, path: String,
        url: String,
        context: Context,
    ): DownloadImgRes {
        var fileName: String? = null
        var bitmap: Bitmap? = null
        var file: File? = null
        bitmap = Glide.with(context)
            .asBitmap()
            .load(url)
            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .get()
        if (bitmap != null) {
            //保存图片，文件名跟随图片名
            fileName = saveBitmapToSd(bitmap, imageName, path)
            file = File(fileName)
        }
        return DownloadImgRes(bitmap, file)
    }
}

data class DownloadImgRes(val bitmap: Bitmap?, val file: File?)