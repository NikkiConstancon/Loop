package com.zetta.android.browse;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.zetta.android.R;
import com.zetta.android.revaServices.NotificationsService;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static android.graphics.Color.GRAY;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;

/**
 * Created by Hristian Vitrychenko on 11/08/2017.
 */

public class notifications extends android.support.v4.app.Fragment
{
    public static final String Tag = "notificationsFragment";
    private Button btnTest;
    private NotificationsAdapter adapter;
    private RecyclerView rv;
    private View view;
    private Context context;
    private int counter = 0;
    private int notifs = 0;
    static public final String sharedPrefId = "MyPref";


    /**
     * Overridden view creator for the notifications section
     * @param inflater the inflater of the view
     * @param container the container of the view
     * @param savedInstanceState the saved instance of the view
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notifications, container, false);

        context = getActivity();

        rv = (RecyclerView) view.findViewById(R.id.rvNotif);

        LinearLayoutManager linearLayout = new LinearLayoutManager(context);
        rv.setLayoutManager(linearLayout);

        list = new ArrayList<NotificationsObject>();

        //MyPref is the place holder for the username (change it with username of current user)
        SharedPreferences saved_values = context.getSharedPreferences(sharedPrefId, MODE_PRIVATE);

        String json;
        counter = saved_values.getInt("counter", -1);
        Gson g = new Gson();


        if(counter != -1) {
            for (int i = 0; i < counter; i++) {
                json = saved_values.getString(Integer.toString(i), "");
                NotificationsObject newNot = g.fromJson(json, NotificationsObject.class);
                list.add(newNot);
            }
        }

        adapter = new NotificationsAdapter(context, list, sharedPrefId);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();


        if(notificationsService == null) {
            notificationsService = new NotificationsService(
                    getActivity(),
                    new NotificationsService.Worker() {
                        @Override
                        public void onNotification(final NotificationsService.Notification note) {
                            Log.d("---Notifications---Note", "here");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    boolean isPatient = notificationsService.getService().getUserType() == RevaWebSocketService.USER_TYPE.PATIENT;
                                    addNotification(
                                            note.deviceName + " alert" + (isPatient ? "" : " from " + note.userUid),
                                            note.message,
                                            "?",
                                            note.level
                                    );
                                }
                            });
                        }
                    }
            );
        }
        notificationsService.bind(getContext());
        return view;
    }
    @Override public void onDestroyView(){
        super.onDestroyView();
        notificationsService.unbind(getContext());
        notificationsService = null;
    }

    public ArrayList<NotificationsObject> list;

    public void populateNotifications()
    {
        list = new ArrayList<NotificationsObject>();
        list.add(new NotificationsObject("Heart Rate", "reva has detected strong deviations from the norm. Please contact a medical professional immediately.", R.drawable.ic_heart, RED));
        list.add(new NotificationsObject("Temperature", "reva has detected moderate deviations from the norm. Consider contacting a medical professional.", R.drawable.ic_notifications_black_24dp, YELLOW));
        list.add(new NotificationsObject("Glucose", "reva has detected slight deviations from the norm. Please check on the patient.", R.drawable.ic_settings_black_24dp, GREEN));
        list.add(new NotificationsObject("Glucose", "reva has detected strong deviations from the norm. Please contact a medical professional immediately.", R.drawable.ic_dashboard_black_24dp, RED));
        list.add(new NotificationsObject("Glucose", "reva has detected moderate deviations from the norm. Consider contacting a medical professional.", R.drawable.ic_help_black_24dp, YELLOW));
    }

    public void addNotification(String title, String content, String resource, int level)
    {
        int res = getIconResource(resource);

        int severity = 0;

        if(level == 1)
        {
            severity = GREEN;
        }
        else if(level == 2)
        {
            severity = YELLOW;
        }
        else
        {
            severity = RED;
        }

        NotificationsObject newNotif = new NotificationsObject(title, content, res, severity);

        if(list == null || list.size() == 0)
        {
            list = new ArrayList<NotificationsObject>();
            list.add(newNotif);

            adapter.updateList(list);
            adapter.notifyDataSetChanged();
        }
        else
        {
            ArrayList<NotificationsObject> newList = new ArrayList<NotificationsObject>();
            list.clear();
            list.addAll(adapter.getCurrentList());
            newList.addAll(list);
            newList.add(newNotif);
            list = newList;
            adapter.updateList(newList);
            adapter.notifyDataSetChanged();
            adapter.notifyItemInserted(list.size());
        }


        Gson gson = new Gson();
        String json;

        //MyPref is the place holder for the username (change it with username of current user)
        SharedPreferences saved_values = context.getSharedPreferences(sharedPrefId, MODE_PRIVATE);
        SharedPreferences.Editor editor=saved_values.edit();
        counter = 0;

        if(list != null) {
            for (int i = 0; i < list.size(); i++) {
                json = gson.toJson(list.get(i));
                editor.putString(Integer.toString(counter), json);
                counter++;
            }
        }
        else
        {
            counter = -1;
        }

        editor.putInt("counter", counter);

        editor.commit();
        rv.smoothScrollBy(1,0);

        notifs++;

    }

    static int getIconResource(String devName){
        //TODO change to final trings
        switch(devName){
            case "Heart Rate": return R.drawable.heart;
            case "Body Temperature": return R.drawable.thermometer1;
            case "Body Glucose": return R.drawable.glucose;
            case "Body Insulin": return R.drawable.insulin1;
            default: return  R.mipmap.reva;
        }
    }
    static void systemNotification(Context context, int res, int noteId, String body, int level){
        Intent dismissIntent = new Intent(context, MainActivity.class);
        dismissIntent.setAction(Intent.ACTION_DEFAULT);
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.reva_white)
                        .setContentTitle("ReVA Alert")
                        .setContentText(body)
                        .setDefaults(Notification.DEFAULT_ALL) // must requires VIBRATE permission
                        .setPriority(NotificationCompat.PRIORITY_HIGH)//must give priority to High, Max which will considered as heads-up notification
                        .setAutoCancel(true)
                        .setColor(getColorFromLevel(level));


        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(noteId, builder.build());
    }

    /**
     * The default constructor
     */
    public notifications(){}

    public static int getColorFromLevel(int level){
        switch (level){
            case 1: return GREEN;
            case 2: return YELLOW;
            case 3: return RED;
            default: return GRAY;
        }
    }

    public static void persistAndNotifyNotification(Context context, int noteId, String title, String content, String resource, int level)
    {
        int res = getIconResource(resource);

        int severity = 0;

        if(level == 1)
        {
            severity = GREEN;
        }
        else if(level == 2)
        {
            severity = YELLOW;
        }
        else
        {
            severity = RED;
        }

        String json = "{'noteTitle' : '" + title + "', 'noteContent' : '" + content + "', 'imageSource' : " + res + ", 'severity': " + severity + "}";

        systemNotification(context, res, noteId, content, level);

        SharedPreferences saved_values = context.getSharedPreferences(sharedPrefId, MODE_PRIVATE);
        SharedPreferences.Editor editor=saved_values.edit();
        int counter = saved_values.getInt("counter", 0);
        editor.putString(Integer.toString(counter++), json);
        editor.putInt("counter", counter);

        editor.commit();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Gson gson = new Gson();
        String json;

        //MyPref is the place holder for the username (change it with username of current user)
        SharedPreferences saved_values = context.getSharedPreferences(sharedPrefId, MODE_PRIVATE);
        SharedPreferences.Editor editor=saved_values.edit();
        counter = 0;

        if(list != null) {
            for (int i = 0; i < list.size(); i++) {
                json = gson.toJson(list.get(i));
                editor.putString(Integer.toString(counter), json);
                counter++;
            }
        }
        else
        {
            counter = -1;
        }

        editor.putInt("counter", counter);

        editor.commit();
    }

    /**
     * Overridden on activity create method to create appropriate notification view
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }



    NotificationsService notificationsService = null;
}
