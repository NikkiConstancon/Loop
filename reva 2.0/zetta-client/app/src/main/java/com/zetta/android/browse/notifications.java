package com.zetta.android.browse;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zetta.android.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
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

        Button butn = (Button) view.findViewById(R.id.btn_test_notif);


        butn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addNotification("Heart Rate", "Heart rate is dropping too fast", "Heart", RED);
                addNotification("Temperature", "Temperature is too high. Please check on the patient", "Temperature", GREEN);
                addNotification("Glucose", "Body glucose levels are not normal. Please consider contacting a medical professional", "Glucose", YELLOW);
            }
        });

        rv = (RecyclerView) view.findViewById(R.id.rvNotif);

        LinearLayoutManager linearLayout = new LinearLayoutManager(context);
        rv.setLayoutManager(linearLayout);

        list = new ArrayList<NotificationsObject>();

        //MyPref is the place holder for the username (change it with username of current user)
        SharedPreferences saved_values = context.getSharedPreferences("MyPref", MODE_PRIVATE);

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

        adapter = new NotificationsAdapter(context, list);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();


        return view;
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

    public void addNotification(String title, String content, String resource, int severity)
    {
        int res = R.drawable.ic_dashboard_black_24dp;

        if(resource.equalsIgnoreCase("Heart"))
        {
            res = R.drawable.ic_heart;
        }
        else if(resource.equalsIgnoreCase("Temperature"))
        {
            res = R.drawable.thermometer;
        }
        else if(resource.equalsIgnoreCase("Glucose"))
        {
            res = R.drawable.glucose;
        }
        else if(resource.equalsIgnoreCase("Insulin"))
        {
            res = R.drawable.insulin;
        }
        else if(resource.equalsIgnoreCase("Blood Pressure"))
        {
            res = R.drawable.ic_help_black_24dp;
        }

        NotificationsObject newNotif = new NotificationsObject(title, content, res, severity);
        if(list == null)
        {
            list = new ArrayList<NotificationsObject>();
            list.add(newNotif);

            adapter = new NotificationsAdapter(context, list);
            rv.setAdapter(adapter);
        }
        else
        {
            list.add(newNotif);

            adapter.notifyItemInserted(list.size()-1);
        }

        adapter.notifyDataSetChanged();

        Intent dismissIntent = new Intent(context, MainActivity.class);
        dismissIntent.setAction(Intent.ACTION_DEFAULT);
        dismissIntent.putExtra("NotificationMessager", "STUFF");//
        dismissIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_heart)
                        .setContentTitle("reva Alert")
                        .setContentText("Deviations from the norm")
                        .setDefaults(Notification.DEFAULT_ALL) // must requires VIBRATE permission
                        .setPriority(NotificationCompat.PRIORITY_HIGH)//must give priority to High, Max which will considered as heads-up notification
                        .setAutoCancel(true);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(contentIntent);

//set intents and pending intents to call service on click of "dismiss" action button of notification
// Gets an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//to post your notification to the notification bar with a id. If a notification with same id already exists, it will get replaced with updated information.
        notificationManager.notify(0, builder.build());
    }

    /**
     * The default constructor
     */
    public notifications(){}


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Gson gson = new Gson();
        String json;

        //MyPref is the place holder for the username (change it with username of current user)
        SharedPreferences saved_values = context.getSharedPreferences("MyPref", MODE_PRIVATE);
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
}
