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

import com.zetta.android.R;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stat_hist_fragment, container, false);
        super.onCreate(savedInstanceState);

        statList = (RecyclerView) view.findViewById(R.id.stats_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        statList.setLayoutManager(layoutManager);
        // this is for performance
        statList.setHasFixedSize(true);
        statListAdapter = new StatListAdapter(NUM_LIST_ITEMS);

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

