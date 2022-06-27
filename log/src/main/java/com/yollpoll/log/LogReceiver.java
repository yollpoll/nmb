package com.yollpoll.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by spq on 2021/7/5
 */
public class LogReceiver extends BroadcastReceiver {
    private static final String TAG = "LogReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: 收到广播");
        LogService.startLogService(intent.getStringExtra("name"),
                intent.getStringExtra("content"),
                context);
    }
}
