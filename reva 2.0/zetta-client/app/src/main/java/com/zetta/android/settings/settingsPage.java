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
import com.zetta.android.revaServices.PubSubBindingService;
import com.zetta.android.revaServices.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class settingsPage extends AppCompatActivity {
    RecyclerView settingsList;
    SettingsListAdapter settingsListAdapter;
    private List<SettingsItem> settings = new ArrayList<>();
    private List<SettingsItem> patList = new ArrayList<>();
    private List<SettingsItem> reqList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settings.add(new TitleItem("Subscriber Requests"));
        settings.add(new TitleItem("Current Subscribers"));

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
            @Override
            public void deleteOnClick(View v, int position) {
                Log.d("here", "deleteOnClick at position"+position);
            }
        });

        settingsList.setAdapter(settingsListAdapter);
        pubSubBinderEndpoint.bind(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        pubSubBinderEndpoint.unbind(this);
    }

    PubSubBindingService pubSubBinderEndpoint = new PubSubBindingService(this,
            new PubSubBindingService.PubSubWorker(){
                @Override public void sendRequestCallback(final String msg){
                    //You no longer need to do the ugly runOnUiThread
                    Log.d("MEAS", msg);
                    if (msg.equals("")) {
                        //alert("Succesfully sent request", "OK");
                    } else {
                        //alert(msg, "Try Again");
                    }
                    Log.d("------TEST---------", msg);
                }
                @Override public void sendReplyActionCallback(boolean sucsess){

                }
            },
            new PubSubBindingService.PubSubInfoWorker(){
                @Override public void onConnect(Map<String, PubSubBindingService.pubSubReqInfo> infoMap){
                    for(Map.Entry<String, PubSubBindingService.pubSubReqInfo> entry : infoMap.entrySet()){
                        PubSubBindingService.pubSubReqInfo info =  entry.getValue();
                        Log.d("----ALL-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                    }
                }
                @Override public void newReq(PubSubBindingService.pubSubReqInfo info){
                    Log.d("----NEW-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());

                    List<SettingsItem> tmp = new ArrayList<>();
                    tmp.add(settings.get(0));
                    

                }
                @Override public void onPatientList(List<String> patientList){
                    Log.d("----sub-list---", patientList.toString());

                    List<SettingsItem> tmp = new ArrayList<>();
                    for (int i =0; i < patientList.size(); i++) {
                        patList.add(new ExistingItem(patientList.get(i)));
                    }

                    tmp.add(settings.get(0));
                    for (int count = 1; count < settings.size(); count++) {
                        tmp.add(settings.get(count));
                        if (settings.get(count) instanceof TitleItem) {
                            tmp.add(settings.get(count));
                            break;
                        }
                    }

                    tmp.addAll(patList);
                    for (int i = 1; i < settings.size(); i++) {
                        tmp.add(settings.get(i));
                    }
                    settingsListAdapter.updateList(patList);
                }
            }
    );
}
