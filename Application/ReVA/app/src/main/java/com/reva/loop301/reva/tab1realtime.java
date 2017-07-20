package com.reva.loop301.reva;

/**
 * Created by Hristian Vitrychenko on 04/07/2017.
 */

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;

public class tab1realtime extends Fragment {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<RealTime> list = new ArrayList<RealTime>();
    int[] image_id = {R.drawable.heart_rate, R.drawable.blood_pressure, R.drawable.temperature};
    String[] aboveText, value, extVal;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1realtime, container, false);
        aboveText = getResources().getStringArray(R.array.aboveText);
        value = getResources().getStringArray(R.array.heart_rate);
        int count = 0;

        for(String text : aboveText)
        {
            RealTime realTime = new RealTime(image_id[count], text, value[count], "");
            count++;
            list.add(realTime);
        }

        recyclerView = getView().findViewById(R.id.realTimeRecycler);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new RealTimeAdapter(list);
        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
