package com.zetta.android.revawebsocketservice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by ME on 2017/09/02.
 */

public class RevaBootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, RevaWebSocketService.class);
        context.startService(serviceIntent);
    }
}
