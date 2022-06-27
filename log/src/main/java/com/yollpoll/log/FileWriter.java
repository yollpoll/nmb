package com.yollpoll.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件IO操作类
 * Created by wuwei on 17-5-23.
 */

public class FileWriter {
    private static final String TAG = "FileUtil";
    private File mFile = null;
    private FileOutputStream mFileOutputStream = null;


    public FileWriter() {
    }


    public synchronized void createFile(String filePath) {
        mFile = new File(filePath);
        File parentDir = mFile.getParentFile();
        if (parentDir != null) {
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
        }
        if (!mFile.exists()) {//若不存在，则新建
            try {
                mFile.createNewFile();
                mFileOutputStream = new FileOutputStream(mFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 写文件到本地
     */
    public synchronized void write(byte[] retData) {
        try {
            if (mFileOutputStream != null) {
                mFileOutputStream.write(retData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭文件输出流
     */
    public synchronized void closeFile() {
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mFileOutputStream != null) {
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mFileOutputStream = null;
                }
            }
        }
    }
}
