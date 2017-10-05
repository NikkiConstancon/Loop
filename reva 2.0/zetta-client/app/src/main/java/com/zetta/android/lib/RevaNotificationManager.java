package com.zetta.android.lib;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.zetta.android.R;
import com.zetta.android.browse.MainActivity;

/**
 * Created by ME on 2017/10/05.
 */

public class RevaNotificationManager {
    private RevaNotificationManager(){}
    static RevaNotificationManager instance = new RevaNotificationManager();
    static RevaNotificationManager getInstance(){return instance;}

    static final String CHANNEL_ID = "RevaNotificationManager";

    public void doOnNewIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey(CHANNEL_ID ))
            {
                String msg = extras.getString(CHANNEL_ID );
                int foo = 0;
            }
        }
    }


    public void create(Context context, NotificationCompat.Builder builder){
        Intent dismissIntent = new Intent(context, MainActivity.class);
        dismissIntent.setAction(Intent.ACTION_DEFAULT);
        dismissIntent.putExtra(CHANNEL_ID , "STUFF");
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        /*
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_heart)
                        .setContentTitle("ReVA Alert")
                        .setContentText("Deviations from the norm")
                        .setDefaults(Notification.DEFAULT_ALL) // must requires VIBRATE permission
                        .setPriority(NotificationCompat.PRIORITY_HIGH)//must give priority to High, Max which will considered as heads-up notification
                        .setAutoCancel(true);
        */
        builder.setChannelId("adf");

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(contentIntent);

//set intents and pending intents to call service on click of "dismiss" action button of notification
// Gets an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//to post your notification to the notification bar with a id. If a notification with same id already exists, it will get replaced with updated information.
        notificationManager.notify(0, builder.build());
    }
}
