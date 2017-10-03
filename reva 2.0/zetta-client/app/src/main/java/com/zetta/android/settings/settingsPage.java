package com.zetta.android.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zetta.android.R;
import com.zetta.android.browse.StatListAdapter;

import java.util.ArrayList;
import java.util.List;

public class settingsPage extends AppCompatActivity {
    RecyclerView settingsList;
    SettingsListAdapter settingsListAdapter;
    private List<SettingsItem> settings = new ArrayList<>();;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settings.add(new TitleItem("Subscriber Requests"));
        settings.add(new RequestItem("Its me"));
        settings.add(new RequestItem("Your Brother"));

        settingsList = (RecyclerView) findViewById(R.id.settings_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        settingsList.setLayoutManager(layoutManager);

        settingsList.setHasFixedSize(true);

        settingsListAdapter = new SettingsListAdapter(settings, new SettingsListAdapter.MyAdapterListener() {
            @Override
            public void buttonYesOnClick(View v, int position) {
                Log.d("here", "buttonYesOnClick at position "+position);
            }
            @Override
            public void buttonNoOnClick(View v, int position) {
                Log.d("here", "buttonNoOnClick at position "+position);
            }
        });

        settingsList.setAdapter(settingsListAdapter);
    }

}
