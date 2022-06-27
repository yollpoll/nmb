package com.yollpoll.log;

import android.content.Context;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by spq on 2021/7/5
 */
public class LogTools {
    static SimpleDateFormat logSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 保存log到本地
     *
     * @param name    文件名
     * @param log     日志内容
     * @param context context
     */
    public static synchronized boolean saveLog(String name, String log, Context context) {
        File file = FileUtils.createFile(name, FileUtils.DirType.LOG, context);
        return MKFileIOUtils.writeFileFromString(file, log, true);
    }
}
