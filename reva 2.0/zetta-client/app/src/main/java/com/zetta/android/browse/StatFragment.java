package com.zetta.android.browse;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.zetta.android.GraphEntry;
import com.zetta.android.R;
import com.zetta.android.StatItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hristian Vitrychenko on 11/08/2017.
 * Made to do something 22/08/2017
 */

public class StatFragment extends android.support.v4.app.Fragment
{
    private static final int NUM_LIST_ITEMS = 100;
    public static final String Tag = "StatFragment";
    private RecyclerView statList;
    private StatListAdapter statListAdapter;
    private List<StatItem> cards = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stat_hist_fragment, container, false);
        super.onCreate(savedInstanceState);

        // MOCK DATA STARTS HERE
        String timeStamp = new SimpleDateFormat("MM.dd HH:mm").format(new java.util.Date());
        cards.add(new SimpleStatItem("Temperature", "http://i.imgur.com/R9xBixo.png", "average", timeStamp, timeStamp, "C", 37.34 ));
        LinkedList<GraphEntry> entries = new LinkedList<GraphEntry>();
        entries.add(new GraphEntry(1.0f, 56.3f));
        entries.add(new GraphEntry(2.0f, 78.3f));
        entries.add(new GraphEntry(3.0f, 43.16f));
        entries.add(new GraphEntry(4.0f, 88.3f));
        entries.add(new GraphEntry(5.0f, 100.3f));
        entries.add(new GraphEntry(6.0f, 67.3f));
        entries.add(new GraphEntry(7.0f, 43.3f));
        entries.add(new GraphEntry(8.0f, 104.3f));
        entries.add(new GraphEntry(9.0f, 55.3f));
        entries.add(new GraphEntry(10.0f, 67.3f));
        cards.add(new GraphStatItem("Heart-rate", "", "line-graph", timeStamp, timeStamp, "BPM", entries ));
        // MOCK DATA ENDS HERE

        statList = (RecyclerView) view.findViewById(R.id.stats_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        statList.setLayoutManager(layoutManager);

        statList.setHasFixedSize(true);
        statListAdapter = new StatListAdapter(cards);

        statList.setAdapter(statListAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}

