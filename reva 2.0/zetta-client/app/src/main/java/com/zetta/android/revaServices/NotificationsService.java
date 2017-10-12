package com.zetta.android.revaServices;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.browse.notifications;
import com.zetta.android.lib.Interval;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ME on 2017/10/06.
 */

public class NotificationsService extends RevaWebsocketEndpoint {
    static final String SHARED_PREF_KEY_ENABLED = "NOTIFICATIONSSERVICE_KEY";
    public static boolean isNotificationsEnabled(Context context){
        SharedPreferences saved_values = context.getSharedPreferences(SHARED_PREF_KEY_ENABLED, MODE_PRIVATE);
        return saved_values.getBoolean(SHARED_PREF_KEY_ENABLED, true);
    }

    public static void toggleNotificationsEnabled(Context context){
        SharedPreferences saved_values = context.getSharedPreferences(SHARED_PREF_KEY_ENABLED, MODE_PRIVATE);
        SharedPreferences.Editor editor = saved_values.edit();
        boolean flag = !saved_values.getBoolean(SHARED_PREF_KEY_ENABLED, true);
        editor.putBoolean(SHARED_PREF_KEY_ENABLED, flag).commit();
    }

    public NotificationsService(Context context_, Worker worker_){
        context = context_;
        worker = worker_;
    }
    public final String SERVICE_NAME = "Notifications";
    @Override public String key(){return SERVICE_NAME;}

    Set<Integer> noteIdSet = new TreeSet<>();

    public static Notification parseNotification(Map<String, String> map){
        return new Notification(
            map.get("userUid"),
            map.get("deviceName"),
            map.get("message"),
            map.get("value"),
            map.get("noteLevel"),
            map.get("id")
        );
    }

    List<Notification> notificationList = new ArrayList<>();
    static public class Notification{
        public final String userUid;
        public final String deviceName;
        public final String message;
        public final double value;
        public final int level;
        public final int id;

        Notification(
                String userUid_,
                String deviceName_,
                String message_,
                String value_,
                String level_,
                String id_
        ){
            userUid = userUid_;
            deviceName = deviceName_;
            message = message_;
            value = Double.parseDouble(value_);
            level = Integer.parseInt(level_);
            id = Integer.parseInt(id_);
        }
    }
    @Override public final void onMessage(final LinkedTreeMap obj) {
        if(!isNotificationsEnabled(context)){
            return;
        }
        Log.d("---Notifications---", obj.toString());
        if(obj.containsKey("ThresholdDeviation")){
            Map<String, String> map = (Map<String, String>)obj.get("ThresholdDeviation");

            final Notification note = parseNotification(map);
            if(!noteIdSet.contains(note.id)) {
                noteIdSet.add(note.id);
                boolean isSelf = getService().getAuthId().compareTo(note.userUid) == 0;
                notifications.persistAndNotifyNotification(
                        context,
                        note.id,
                        note.deviceName + " alert" + (isSelf ? "" : " from " + note.userUid),
                        note.message,
                        note.deviceName,
                        note.level
                        );
            }
            worker.onNotification(note);
        }
    }
    final Context context;
    final Worker worker;
    public static class Worker{
        public void onNotification(Notification newNotification){}
    }
}
