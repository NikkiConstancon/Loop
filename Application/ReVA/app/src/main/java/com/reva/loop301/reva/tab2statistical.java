package com.reva.loop301.reva;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
/**
 * Created by Hristian Vitrychenko on 04/07/2017.
 */

/**
 * Tab class for statistical data
 */
public class tab2statistical  extends Fragment{

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Statistical> list = new ArrayList<Statistical>();
    int[] image_id = {R.drawable.heart_rate, R.drawable.blood_pressure, R.drawable.temperature};
    String[] aboveText, min, max, avg;


    /**
     * Overridden onCreateView, adapted for statistical data
     * @param inflater holds the layout inflater for statistical data
     * @param container holds the view group for statistical data
     * @param savedInstanceState holds the Bundle for statistical data
     * @return the view for statistical data
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2statistical, container, false);

        aboveText = getResources().getStringArray(R.array.aboveText);
        min = getResources().getStringArray(R.array.minimums);
        max = getResources().getStringArray(R.array.maximums);
        avg = getResources().getStringArray(R.array.averages);
        int count = 0;

        for(String text : aboveText)
        {
            Statistical stat = new Statistical(image_id[count], text, min[count], max[count], avg[count]);
            count++;
            list.add(stat);
        }

        recyclerView = getView().findViewById(R.id.statisticalRecycler);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new StatisticalAdapter(list);
        recyclerView.setAdapter(adapter);

        return rootView;
    }
}
