package com.zetta.android.browse;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.zetta.android.R;

/**
 * Created by Hristian Vitrychenko on 11/08/2017.
 */

public class statHistFragment extends android.support.v4.app.Fragment
{
    public static final String Tag = "statHistFragment";
    private Button btnTest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stat_hist_fragment, container, false);

        btnTest = (Button) view.findViewById(R.id.statHistButton);

        btnTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                Toast.makeText(getActivity(), "Testing stat hist button" , Toast.LENGTH_SHORT ).show();
            }
        });

        return view;
    }

    public statHistFragment(){

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}

