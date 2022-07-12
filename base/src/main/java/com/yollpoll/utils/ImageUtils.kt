package com.yollpoll.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * 压缩图片
 *
 * @param path
 * @param destWidth
 * @param destHeight
 * @return
 */
suspend fun compressBitmap(path: String?, destWidth: Int, destHeight: Int): Bitmap? =
    withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val outWidth = options.outWidth
        val outHeight = options.outHeight
        var inSampleSize = 1
        while (outHeight / inSampleSize > destHeight || outWidth / inSampleSize > destWidth) {
            inSampleSize *= 2
        }
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return@withContext BitmapFactory.decodeFile(path, options)
    }

/**
 * 保存图片到mediastore
 */
suspend fun saveImageToMediaStore(url: String, name: String, context: Context): String =
    withContext(Dispatchers.IO) {
        val bitmap = Glide.with(context).asBitmap().load(url)
            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
        return@withContext MediaStore.Images.Media.insertImage(
            context.contentResolver, bitmap, name,
            getCurrentDate()
        )
    }

/**
 * 保存bitmap到共享目录
 */
suspend fun saveBitmapTpMediaStore(bitmap: Bitmap, name: String, context: Context): String =
    withContext(Dispatchers.IO) {
        return@withContext MediaStore.Images.Media.insertImage(
            context.contentResolver, bitmap, name,
            getCurrentDate()
        )
    }

/**
 * 保存图片到SD卡目录
 * 这个方法应当写在子线程
 *
 * @param bitmap
 * @param path   地址，应该用constant里面的常量
 */
//context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
suspend fun saveBitmapToSd(bitmap: Bitmap?, imageName: String, path: String): String? {
    if (null == bitmap) return ""
    //替换/
    val cacheDir = File(path)
    if (!cacheDir.exists()) {
        try {
            cacheDir.mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    val file = File(cacheDir.absolutePath, imageName)

    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        if (fos != null) {
            try {
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return file.absolutePath
}

/**
 * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
 * @return 图片的uri
 */
public fun createImageUri(context: Context): Uri? {
    //设置保存参数到ContentValues中
    val contentValues = ContentValues()
    //设置文件名
    contentValues.put(
        MediaStore.Images.Media.DISPLAY_NAME,
        System.currentTimeMillis().toString() + ""
    )
    //兼容Android Q和以下版本
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
        //TODO RELATIVE_PATH是相对路径不是绝对路径;照片存储的地方为：内部存储/Pictures/preventpro
        contentValues.put(
            MediaStore.Images.Media.RELATIVE_PATH,
            "Pictures/preventpro"
        )
    }
    //设置文件类型
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
    //执行insert操作，向系统文件夹中添加文件
    //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}


