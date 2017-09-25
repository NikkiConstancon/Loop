package com.zetta.android.device;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zetta.android.BuildConfig;
import com.zetta.android.ImageLoader;
import com.zetta.android.ListItem;
import com.zetta.android.R;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.device.actions.OnActionClickListener;
import com.zetta.android.device.events.EventsActivity;
import com.zetta.android.settings.SdkProperties;

import java.util.List;
import java.util.Map;

public class DeviceDetailsActivity extends AppCompatActivity {

    public static final String KEY_DEVICE_ID = BuildConfig.APPLICATION_ID + "/DEVICE_ID";

    private DeviceDetailsService deviceDetailsService;
    private EmptyLoadingView emptyLoadingWidget;
    private DeviceDetailsListAdapter adapter;
    private RecyclerView detailsListWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SdkProperties sdkProperties = SdkProperties.newInstance(this);
        DeviceDetailsSdkService sdkService = new DeviceDetailsSdkService();
        DeviceDetailsMockService mockService = new DeviceDetailsMockService();
        deviceDetailsService = new DeviceDetailsService(sdkProperties, sdkService, mockService);

        setContentView(R.layout.device_details_activity);

        emptyLoadingWidget = (EmptyLoadingView) findViewById(R.id.device_details_empty_view);
        adapter = new DeviceDetailsListAdapter(new ImageLoader(), onActionClickListener, onEventsClickListener);
        detailsListWidget = (RecyclerView) findViewById(R.id.device_details_list);
        detailsListWidget.setAdapter(adapter);
        detailsListWidget.setHasFixedSize(true);
        detailsListWidget.setLayoutManager(new LinearLayoutManager(this));
        detailsListWidget.setItemAnimator(null);
    }

    private final OnActionClickListener onActionClickListener = new OnActionClickListener() {

        @Override
        public void onActionClick(@NonNull ZettaDeviceId deviceId, @NonNull String action, @NonNull Map<String, Object> inputs) {
            deviceDetailsService.updateDetails(deviceId, action, inputs, onDeviceUpdate);
        }
    };

    private final DeviceDetailsListAdapter.OnEventsClickListener onEventsClickListener = new DeviceDetailsListAdapter.OnEventsClickListener() {
        @Override
        public void onEventsClick(@NonNull ZettaDeviceId deviceId) {
            Intent intent = new Intent(DeviceDetailsActivity.this, EventsActivity.class);
            intent.putExtra(EventsActivity.KEY_DEVICE_ID, deviceId);
            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_details, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateState();
        ZettaDeviceId deviceId = (ZettaDeviceId) getIntent().getSerializableExtra(KEY_DEVICE_ID);
        deviceDetailsService.getDetails(deviceId, onDeviceDetailsLoaded);
        deviceDetailsService.startMonitoringDevice(deviceId, onDeviceUpdate);
    }

    private final DeviceDetailsService.Callback onDeviceDetailsLoaded = new DeviceDetailsService.Callback() {
        @Override
        public void on(@NonNull DeviceDetails deviceDetails) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(deviceDetails.getName());
            actionBar.setSubtitle(deviceDetails.getSeverName());
            actionBar.setBackgroundDrawable(deviceDetails.createBackground());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(deviceDetails.getBackgroundColor());
            }

            Drawable upArrow = ContextCompat.getDrawable(DeviceDetailsActivity.this, R.drawable.ic_back_arrow);
            upArrow.setColorFilter(deviceDetails.getTintColor(), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);

            adapter.replaceAll(deviceDetails.getListItems());
            updateState();
        }

        @Override
        public void onDeviceLoadError() {
            Toast.makeText(DeviceDetailsActivity.this, "Device couldn't load", Toast.LENGTH_SHORT).show();
            deviceDetailsService.stopMonitoringDevice();
            finish();
        }
    };

    private final DeviceDetailsService.DeviceListener onDeviceUpdate = new DeviceDetailsService.DeviceListener() {
        @Override
        public void onUpdated(@NonNull List<ListItem> listItems) {
            if (detailsListWidget.isAnimating() || detailsListWidget.isComputingLayout()) {
                return;
            }
            adapter.replaceAll(listItems);
        }
    };

    private void updateState() {
        if (adapter.isEmpty()) {
            emptyLoadingWidget.setStateLoading();
            emptyLoadingWidget.setVisibility(View.VISIBLE);
            detailsListWidget.setVisibility(View.GONE);
        } else {
            emptyLoadingWidget.setVisibility(View.GONE);
            detailsListWidget.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        deviceDetailsService.stopMonitoringDevice();
        super.onPause();
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
}
