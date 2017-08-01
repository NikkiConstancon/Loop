package com.zetta.android.browse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.novoda.notils.logger.simple.Log;
import com.zetta.android.BuildConfig;
import com.zetta.android.ImageLoader;
import com.zetta.android.ListItem;
import com.zetta.android.R;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.device.DeviceDetailsActivity;
import com.zetta.android.device.actions.OnActionClickListener;
import com.zetta.android.settings.SdkProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class creates the main screen putting all elements together, toolbar, quickactions etc.
 */
public class DeviceListActivity extends AppCompatActivity {

    static {
        Log.setShowLogs(BuildConfig.DEBUG);
    }

    private DeviceListService deviceListService;
    private DeviceListAdapter adapter;
    private RecyclerView deviceListWidget;
    private EmptyLoadingView emptyLoadingWidget;
    private BottomSheetBehavior<? extends View> bottomSheetBehavior;
    private QuickActionsAdapter quickActionsAdapter;
    private SwipeRefreshLayout pullRefreshWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SdkProperties sdkProperties = SdkProperties.newInstance(this);
        DeviceListSdkService sdkService = new DeviceListSdkService();
        deviceListService = new DeviceListService(sdkProperties, sdkService);

        setContentView(R.layout.device_list_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        emptyLoadingWidget = (EmptyLoadingView) findViewById(R.id.device_list_empty_view);
        adapter = new DeviceListAdapter(new ImageLoader(), onDeviceClickListener);
        deviceListWidget = (RecyclerView) findViewById(R.id.device_list);
        deviceListWidget.setAdapter(adapter);
        deviceListWidget.setHasFixedSize(true);
        deviceListWidget.setLayoutManager(new LinearLayoutManager(this));
        quickActionsAdapter = new QuickActionsAdapter(onActionClickListener);
        RecyclerView deviceQuickActionsWidget = (RecyclerView) findViewById(R.id.device_list_bottom_sheet_quick_actions);
        deviceQuickActionsWidget.setAdapter(quickActionsAdapter);
        deviceQuickActionsWidget.setHasFixedSize(true);
        deviceQuickActionsWidget.setLayoutManager(new LinearLayoutManager(this));
        deviceListWidget.setItemAnimator(null);

        bottomSheetBehavior = BottomSheetBehavior.from(deviceQuickActionsWidget);
        pullRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.pull_refresh);
        pullRefreshWidget.setOnRefreshListener(onPullRefreshListener);
    }

    @NonNull private final DeviceListAdapter.OnDeviceClickListener onDeviceClickListener = new DeviceListAdapter.OnDeviceClickListener() {
        @Override
        public void onDeviceClick(@NonNull ZettaDeviceId deviceId) {
            Intent intent = new Intent(DeviceListActivity.this, DeviceDetailsActivity.class);
            intent.putExtra(DeviceDetailsActivity.KEY_DEVICE_ID, deviceId);
            startActivity(intent);
        }

        @Override
        public void onDeviceLongClick(@NonNull ZettaDeviceId deviceId) {
            List<ListItem> items = new ArrayList<>();
            items.add(new ListItem.LoadingListItem());
            quickActionsAdapter.updateAll(items);

            // This postDelayed is a hack that fixes an issue with bottom sheet not showing recycler view data when opened
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }, 1);

            deviceListService.getQuickActions(deviceId, onQuickActionsCallback);
        }
    };

    @NonNull private final OnActionClickListener onActionClickListener = new OnActionClickListener() {

        @Override
        public void onActionClick(@NonNull ZettaDeviceId deviceId, @NonNull String action, @NonNull Map<String, Object> inputs) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            deviceListService.updateDetails(deviceId, action, inputs);
        }
    };

    @NonNull private final SwipeRefreshLayout.OnRefreshListener onPullRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            Toast.makeText(DeviceListActivity.this, "Refreshing...", Toast.LENGTH_SHORT).show();
            deviceListService.getDeviceList(onDeviceListLoaded);
        }
    };

    @NonNull private final DeviceListService.Callback onQuickActionsCallback = new DeviceListService.Callback() {
        @Override
        public void on(@NonNull List<ListItem> listItems) {
            quickActionsAdapter.updateAll(listItems);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        updateState();
        if (deviceListService.hasRootUrl()) {
            deviceListService.getDeviceList(onDeviceListLoaded);
            deviceListService.startMonitoringAllDeviceUpdates(onStreamedUpdate);
        }
    }

    @NonNull private final DeviceListService.DevicesUpdateListener onStreamedUpdate = new DeviceListService.DevicesUpdateListener() {
        @Override
        public void onUpdated(@NonNull List<ListItem> listItems) {
            if (deviceListWidget.isComputingLayout() || deviceListWidget.isAnimating()) {
                return;
            }
            adapter.update(listItems);
        }
    };

    @NonNull private final DeviceListService.Callback onDeviceListLoaded = new DeviceListService.Callback() {
        @Override
        public void on(@NonNull List<ListItem> listItems) {
            adapter.replaceAll(listItems);
            pullRefreshWidget.setRefreshing(false);
            updateState();
        }
    };

    private void updateState() {
        if (!adapter.isEmpty()) {
            emptyLoadingWidget.setVisibility(View.GONE);
            deviceListWidget.setVisibility(View.VISIBLE);
        } else if (adapter.isEmpty() && deviceListService.hasRootUrl()) {
            emptyLoadingWidget.setStateLoading(deviceListService.getRootUrl());
            emptyLoadingWidget.setVisibility(View.VISIBLE);
            deviceListWidget.setVisibility(View.GONE);
        } else {
            emptyLoadingWidget.setStateEmpty();
            emptyLoadingWidget.setVisibility(View.VISIBLE);
            deviceListWidget.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        deviceListService.stopMonitoringStreamedUpdates();
        super.onPause();
    }
}
