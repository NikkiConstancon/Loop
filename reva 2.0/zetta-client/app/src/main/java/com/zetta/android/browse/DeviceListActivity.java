package com.zetta.android.browse;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.novoda.notils.logger.simple.Log;
import com.zetta.android.BuildConfig;
import com.zetta.android.ImageLoader;
import com.zetta.android.ListItem;
import com.zetta.android.R;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.device.DeviceDetailsActivity;
import com.zetta.android.device.actions.OnActionClickListener;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;
import com.zetta.android.settings.SdkProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class creates the main screen putting all elements together, toolbar, quickactions etc.
 */
public class DeviceListActivity extends Fragment {

    public static final String Tag = "DeviceListActivity";

    static {
        Log.setShowLogs(BuildConfig.DEBUG);
    }

    private DeviceListService deviceListService;
    private DeviceListAdapter adapter;
    private RecyclerView deviceListWidget;
    private EmptyLoadingView emptyLoadingWidget;
    private BottomSheetBehavior<? extends View> bottomSheetBehavior;
    private QuickActionsAdapter quickActionsAdapter;
    private static SdkProperties sdkProperties;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_list_activity, container, false);
        super.onCreate(savedInstanceState);
        sdkProperties = SdkProperties.newInstance(getActivity());

        MainActivity activity = (MainActivity) getActivity();
        String myDataFromActivity = activity.getUser();

        String serverURI = getString(R.string.serverURL) + ":3009";
        sdkProperties.setUrl(serverURI);
        Log.d("data", myDataFromActivity);
        DeviceListSdkService sdkService = new DeviceListSdkService(myDataFromActivity); // this is a hotfix



        //ServerComms server = new ServerComms(DeviceListActivity.this.getActivity());
        //server.execute(serverURI);

        //while (server.getStatus() != AsyncTask.Status.FINISHED){} // Spin while getting server URL
        deviceListService = new DeviceListService(sdkProperties, sdkService);
        emptyLoadingWidget = (EmptyLoadingView) view.findViewById(R.id.device_list_empty_view);
        adapter = new DeviceListAdapter(new ImageLoader(), onDeviceClickListener);
        deviceListWidget = (RecyclerView) view.findViewById(R.id.device_list);
        deviceListWidget.setAdapter(adapter);
        deviceListWidget.setHasFixedSize(true);
        deviceListWidget.setLayoutManager(new LinearLayoutManager(getActivity()));
        quickActionsAdapter = new QuickActionsAdapter(onActionClickListener);
        RecyclerView deviceQuickActionsWidget = (RecyclerView) view.findViewById(R.id.device_list_bottom_sheet_quick_actions);
        deviceQuickActionsWidget.setAdapter(quickActionsAdapter);
        deviceQuickActionsWidget.setHasFixedSize(true);
        deviceQuickActionsWidget.setLayoutManager(new LinearLayoutManager(getActivity()));
        deviceListWidget.setItemAnimator(null);

        bottomSheetBehavior = BottomSheetBehavior.from(deviceQuickActionsWidget);


        // real time data streaming NEW AND SHINY WEBSOCKET BIZ
        realTimeDataEndpoint.bind(this.getContext());
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //pulseEndpoint.unbind(this);
        realTimeDataEndpoint.unbind(this.getContext());
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        updateState();
        if (deviceListService.hasRootUrl()) {
            deviceListService.getDeviceList(onDeviceListLoaded);
        }

        realTimeDataEndpoint.resumeService();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        realTimeDataEndpoint.pauseService();
    }

    RealTimeDataEndpoint realTimeDataEndpoint = new RealTimeDataEndpoint();

    class RealTimeDataEndpoint extends RevaWebsocketEndpoint {
        private final String TAG = this.getClass().getName();
        @Override
        public String key() {
            return "RTDS";
        }

        public void onMessage(String message){
            android.util.Log.i("STUFF", message );
        }
        public void onMessage(LinkedTreeMap obj){
            List<ListItem> list = adapter.getListItems();
            try {
                for (Map.Entry<String, List> entry : ((Map<String, List>) obj).entrySet()){
                    String patientName = entry.getKey();


                    if (list.get(0) instanceof ServerListItem) {
                        ServerListItem temp = (ServerListItem) list.get(0); // first item in list is server name
                        Log.i("NAME", temp.getName());
                    }
                    List<ListItem> toBeAdded = new ArrayList<ListItem>();
                    for(Map row : (List<Map<String, Double>>)entry.getValue()){

                        android.util.Log.i(TAG, patientName + " " + row.toString());
                        //TODO: this is SOOOOOOO inefficient, must fix!!

                        Iterator<ListItem> iter = list.iterator();

                        while (iter.hasNext()) {
                            ListItem item = iter.next();
                            if (item instanceof DeviceListItem) {
                                DeviceListItem temp = (DeviceListItem)item;
                                String title = row.toString();     // full file name

                                String[] parts = title.split("\\=");

                                title = parts[0].substring(1);
                                String state = parts[1].substring(0, parts[1].length()-1);

                                if (temp.getName().equals(title)) {
                                    Log.d("ISEQ", title + " FOUND");
                                    temp.setState(state);
                                    toBeAdded.add(temp);
                                }
                            }

                        }

                    }
                    
                    adapter.update(toBeAdded);

                }
            }catch (Exception e){
                android.util.Log.e(TAG, e.toString());
            }
        }
        @Override
        public void onServiceConnect(RevaWebSocketService service) {
            resumeService();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @NonNull private final DeviceListAdapter.OnDeviceClickListener onDeviceClickListener = new DeviceListAdapter.OnDeviceClickListener() {
        @Override
        public void onDeviceClick(@NonNull ZettaDeviceId deviceId) {
            Intent intent = new Intent(getActivity(), DeviceDetailsActivity.class);
            intent.putExtra(DeviceDetailsActivity.KEY_DEVICE_ID, deviceId);
            startActivity(intent);
            Log.d("Clicked", "Clicked the device");
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

    @NonNull private final DeviceListService.Callback onQuickActionsCallback = new DeviceListService.Callback() {
        @Override
        public void on(@NonNull List<ListItem> listItems) {
            quickActionsAdapter.updateAll(listItems);
        }
    };

    /*
    @NonNull private final DeviceListService.DevicesUpdateListener onStreamedUpdate = new DeviceListService.DevicesUpdateListener() {
        @Override
        public void onUpdated(@NonNull List<ListItem> listItems) {
            if (deviceListWidget.isComputingLayout() || deviceListWidget.isAnimating()) {
                return;
            }
            adapter.update(listItems); //when there is an update the list adapter is updated
        }
    };
    */


    @NonNull private final DeviceListService.Callback onDeviceListLoaded = new DeviceListService.Callback() {
        @Override
        public void on(@NonNull List<ListItem> listItems) {
            adapter.replaceAll(listItems);
            updateState();
            //deviceListService.startMonitoringAllDeviceUpdates(onStreamedUpdate);
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
}
