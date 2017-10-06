package com.zetta.android.revaServices;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ME on 2017/10/06.
 */

public class NotificationsService extends RevaWebsocketEndpoint {
    public NotificationsService(Context context_, Worker worker_){
        context = context_;
        worker = worker_;
    }
    public final String SERVICE_NAME = "Notifications";
    @Override public String key(){return SERVICE_NAME;}


    List<Notification> notificationList = new ArrayList<>();
    public class Notification{
        public final String userUid;
        public final String deviceName;
        public final String message;
        public final double value;
        public final int level;

        Notification(
                String userUid_,
                String deviceName_,
                String message_,
                String value_,
                String level_
        ){
            userUid = userUid_;
            deviceName = deviceName_;
            message = message_;
            value = Double.parseDouble(value_);
            level = Integer.parseInt(level_);
        }
    }
    @Override public final void onMessage(final LinkedTreeMap obj) {
        Log.d("---Notifications---", obj.toString());
        if(obj.containsKey("ThresholdDeviation")){
            Map<String, String> map = (Map<String, String>)obj.get("ThresholdDeviation");

            final Notification notification = new Notification(
                    map.get("userUid"),
                    map.get("deviceName"),
                    map.get("message"),
                    map.get("value"),
                    map.get("noteLevel")
            );
            notificationList.add(notification);
            if(context instanceof Activity) {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        worker.onNotification(notification);
                    }
                });
            }else{
                worker.onNotification(notification);
            }
        }
    }
    final Context context;
    final Worker worker;
    public static class Worker{
        public void onNotification(Notification newNotification){}
    }
}
