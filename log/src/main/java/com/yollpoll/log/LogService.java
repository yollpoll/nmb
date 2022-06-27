package com.yollpoll.log;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by spq on 2021/7/5
 */
public class LogService extends IntentService {
    private static final String TAG = "LogService";

    public static void startLogService(String name, String content, Context context) {
        Intent intent = new Intent(context, LogService.class);
        intent.putExtra("name", name);
        intent.putExtra("content", content);
        context.startService(intent);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public LogService() {
        super("aispeech-log");
    }

    @Override
    protected void onHandleIntent( Intent intent) {
        Log.d(TAG, "onHandleIntent: 开始保存文件");
        String name = intent.getStringExtra("name");
        String content = intent.getStringExtra("content");
        LogTools.saveLog(name, content, this);
    }
}
