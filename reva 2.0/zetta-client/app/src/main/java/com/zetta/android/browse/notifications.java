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

        btnTest = (Button) view.findViewById(R.id.notificationsButton);

        btnTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                Toast.makeText(getActivity(), "Testing notifications button" , Toast.LENGTH_SHORT ).show();
            }
        });

        return view;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
