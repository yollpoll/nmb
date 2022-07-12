package com.yollpoll.utils

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.yollpoll.framework.extensions.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 分享MediaStore文件
 * @param path mediaStore中返回的路径
 */
fun share(title: String, path: String, context: Activity) {
    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    val uri = Uri.parse(path)
    intent.type = "image/*"
    intent.putExtra(Intent.EXTRA_STREAM, uri)
    context.startActivity(Intent.createChooser(intent, title))
}

///**
// * 保存图片到mediastore
// */
//suspend fun saveImageToMediaStore(url: String, name: String, context: Context): String =
//    withContext(Dispatchers.IO) {
//        val bitmap = Glide.with(context).asBitmap().load(url)
//            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
//        return@withContext MediaStore.Images.Media.insertImage(
//            context.contentResolver, bitmap, name,
//            getCurrentDate()
//        )
//    }
//
///**
// * 保存图片到SD卡目录
// * 这个方法应当写在子线程
// *
// * @param bitmap
// * @param path   地址，应该用constant里面的常量
// */
////context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
//fun saveImageToSd(bitmap: Bitmap?, imageName: String, path: String): String? {
//    if (null == bitmap) return ""
//    //替换/
//    val cacheDir = File(path)
//    if (!cacheDir.exists()) {
//        try {
//            cacheDir.mkdirs()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//    val file = File(cacheDir.absolutePath, imageName)
//
//    var fos: FileOutputStream? = null
//    try {
//        fos = FileOutputStream(file)
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
//        fos.flush()
//    } catch (e: FileNotFoundException) {
//        e.printStackTrace()
//    } catch (e: IOException) {
//        e.printStackTrace()
//    } finally {
//        if (fos != null) {
//            try {
//                fos.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
//    return file.absolutePath
//}
fun getPathByUri(uri: Uri, context: Context): String? {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
        getPhotoPathFromContentUri(context, uri)
    } else {
        getFileByUri(uri, context)
    }
}

/**
 * 通过Uri返回File文件
 * 注意：通过相机的是类似content://media/external/images/media/97596
 * 通过相册选择的：file:///storage/sdcard0/DCIM/Camera/IMG_20150423_161955.jpg
 * 通过查询获取实际的地址
 *
 * @param uri
 * @return
 */
fun getFileByUri(uri: Uri, activity: Context): String? {
    var path: String? = null
    if ("file" == uri.scheme) {
        path = uri.encodedPath
        if (path != null) {
            path = Uri.decode(path)
            val cr = activity.contentResolver
            val buff = StringBuffer()
            buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=").append(
                "'$path'"
            ).append(")")
            val cur = cr.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(
                    MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA
                ), buff.toString(), null, null
            )
            var index = 0
            var dataIdx = 0
            cur!!.moveToFirst()
            while (!cur.isAfterLast) {
                index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID)
                index = cur.getInt(index)
                dataIdx = cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cur.getString(dataIdx)
                cur.moveToNext()
            }
            cur.close()
            if (index == 0) {
            } else {
                val u = Uri.parse("content://media/external/images/media/$index")
                println("temp uri is :$u")
            }
        }
        if (path != null) {
            return path
        }
    } else if ("content" == uri.scheme) {
        // 4.2.2以后
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity.contentResolver.query(uri, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            path = cursor.getString(columnIndex)
        }
        cursor.close()
        return path
    } else {
    }
    return null
}

/**
 * 4.4以后通过uri获取图片地址
 *
 * @param context
 * @param uri
 * @return
 */
fun getPhotoPathFromContentUri(context: Context?, uri: Uri?): String? {
    var photoPath: String? = ""
    if (context == null || uri == null) {
        return photoPath
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
            context,
            uri
        )
    ) {
        val docId = DocumentsContract.getDocumentId(uri)
        if (isExternalStorageDocument(uri)) {
            val split = docId.split(":").toTypedArray()
            if (split.size >= 2) {
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    photoPath =
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            }
        } else if (isDownloadsDocument(uri)) {
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(docId)
            )
            photoPath = getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val split = docId.split(":").toTypedArray()
            if (split.size >= 2) {
                val type = split[0]
                var contentUris: Uri? = null
                if ("image" == type) {
                    contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                photoPath = getDataColumn(context, contentUris, selection, selectionArgs)
            }
        }
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        photoPath = uri.path
    } else {
        photoPath = getDataColumn(context, uri, null, null)
    }
    return photoPath
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

private fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = MediaStore.Images.Media.DATA
    val projection = arrayOf(column)
    try {
        cursor =
            context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        if (cursor != null && !cursor.isClosed) cursor.close()
    }
    return null
}

/**
 * 更新图片库
 */
fun updatePhoto(context: Context, path: String?, fileName: String?) {
    // 其次把文件插入到系统图库
    try {
        MediaStore.Images.Media.insertImage(
            context.contentResolver,
            path, fileName, null
        )
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    context.sendBroadcast(
        Intent(
            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
            Uri.fromFile(File(path))
        )
    )
}

//获取当前日期
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(Date().time)
}