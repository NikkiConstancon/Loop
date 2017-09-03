package com.zetta.android.browse;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.zetta.android.R;

import java.util.ArrayList;
import java.util.List;

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
        View view = inflater.inflate(R.layout.notifications, container, false);

        Context context = getActivity();

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rvNotif);

        LinearLayoutManager linearLayout = new LinearLayoutManager(context);
        rv.setLayoutManager(linearLayout);

        populateNotifications();

        NotificationsAdapter adapter = new NotificationsAdapter(context, list);
        rv.setAdapter(adapter);

        return view;
    }

    public List<NotificationsObject> list;

    public void populateNotifications()
    {
        list = new ArrayList<NotificationsObject>();
        list.add(new NotificationsObject("Heart Rate", "ReVA has detected strong deviations from the norm. Please contact a medical professional immediately.", R.drawable.ic_heart, RED));
        list.add(new NotificationsObject("Temperature", "ReVA has detected moderate deviations from the norm. Consider contacting a medical professional.", R.drawable.ic_notifications_black_24dp, YELLOW));
        list.add(new NotificationsObject("Glucose", "ReVA has detected slight deviations from the norm. Please check on the patient.", R.drawable.ic_settings_black_24dp, GREEN));
        list.add(new NotificationsObject("Glucose", "ReVA has detected strong deviations from the norm. Please contact a medical professional immediately.", R.drawable.ic_dashboard_black_24dp, RED));
        list.add(new NotificationsObject("Glucose", "ReVA has detected moderate deviations from the norm. Consider contacting a medical professional.", R.drawable.ic_help_black_24dp, YELLOW));
    }

    /**
     * The default constructor
     */
    public notifications(){}

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
