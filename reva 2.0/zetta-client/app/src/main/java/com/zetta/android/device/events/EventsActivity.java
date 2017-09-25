package com.zetta.android.device.events;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.zetta.android.BuildConfig;
import com.zetta.android.ListItem;
import com.zetta.android.R;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.settings.SdkProperties;

public class EventsActivity extends AppCompatActivity {

    public static final String KEY_DEVICE_ID = BuildConfig.APPLICATION_ID + "/DEVICE_ID";

    private EventsListAdapter adapter;
    private EventsService eventsService;
    private RecyclerView deviceListWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.Events);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        adapter = new EventsListAdapter();
        deviceListWidget = (RecyclerView) findViewById(R.id.events_list);
        deviceListWidget.setAdapter(adapter);
        deviceListWidget.setHasFixedSize(true);
        deviceListWidget.setLayoutManager(new LinearLayoutManager(this));

        SdkProperties sdkProperties = SdkProperties.newInstance(this);
        EventsSdkService sdkService = new EventsSdkService();
        EventsMockService mockService = new EventsMockService();
        eventsService = new EventsService(sdkProperties, sdkService, mockService);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZettaDeviceId deviceId = (ZettaDeviceId) getIntent().getSerializableExtra(KEY_DEVICE_ID);
        eventsService.startMonitoringLogs(deviceId, onEventsLoaded);
    }

    @NonNull private final EventsService.StreamListener onEventsLoaded = new EventsService.StreamListener() {
        @Override
        public void onUpdated(@NonNull ListItem listItem) {
            adapter.update(listItem);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        eventsService.stopMonitoringLogs();
        super.onPause();
    }
}
