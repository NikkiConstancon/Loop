package com.reva.loop301.reva;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Hristian Vitrychenko on 22/07/2017.
 */

/**
 * CardNotificationFragment is a class that aids in
 * creating the cardview for the notifications section of
 * the application, allowing for dynamic addition of notifications
 */
public class CardNotificationFragment extends Fragment {

    /**
     * Some needed recycler variables, an array list to store
     * notifications and string arrays to populate the new notifications
     */
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<CardNotification> list = new ArrayList<CardNotification>();
    String[] titles, messages, colours;


    /**
     * Overriden onCreateView to cycle through given arrays of
     * notification data, populate notification objects, store them
     * in the notifications list and return the view for user to see.
     *
     * @param inflater works to inflate the layout
     * @param container holds the view group
     * @param savedInstanceState holds the required instance state
     * @return the entire root view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.card_view_notifications, container, false);
        titles = getResources().getStringArray(R.array.titles);
        colours = getResources().getStringArray(R.array.colour);
        messages = getResources().getStringArray(R.array.content);
        int count = 0;

        for(String text : titles)
        {
            CardNotification notification = new CardNotification(titles[count], messages[count], colours[count]);
            count++;
            list.add(notification);
        }

        recyclerView = getView().findViewById(R.id.notificationsRecycler);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new CardNotificationAdapter(list);
        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
