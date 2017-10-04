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



        settingsList = (RecyclerView) findViewById(R.id.settings_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        settingsList.setLayoutManager(layoutManager);
        settingsList.setHasFixedSize(true);

        settingsListAdapter = new SettingsListAdapter(settings, new SettingsListAdapter.MyAdapterListener() {
            @Override
            public void buttonYesOnClick(View v, int position) {
                pubSubBinderEndpoint.pubSubRequestReply(
                        ((RequestItem)settingsListAdapter.getSettings().get(position)).getTitle(),PubSubBindingService.pubSubReqInfo.REPLY.ACCEPT
                );
            }
            @Override
            public void buttonNoOnClick(View v, int position) {
                pubSubBinderEndpoint.pubSubRequestReply(
                        ((RequestItem)settingsListAdapter.getSettings().get(position)).getTitle(),PubSubBindingService.pubSubReqInfo.REPLY.DECLINE
                );
            }
            @Override
            public void deleteOnClick(View v, int position) {
                Log.d("here", "deleteOnClick at position"+position);
            }
        });

        settingsList.setAdapter(settingsListAdapter);
        pubSubBinderEndpoint.bind(this);
    }

    private void updateAdapter() {
        List<SettingsItem> tmp = new ArrayList<>();
        tmp.addAll(reqList);
        tmp.addAll(patList);

        settingsListAdapter.updateList(tmp);
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
                    List<SettingsItem> tmp = new ArrayList<>();
                    reqList.clear();
                    reqList.add(new TitleItem("Subscriber Requests"));
                    for(Map.Entry<String, PubSubBindingService.pubSubReqInfo> entry : infoMap.entrySet()){
                        PubSubBindingService.pubSubReqInfo info =  entry.getValue();
                        Log.d("----ALL-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                        reqList.add(new RequestItem(info.userUid));
                    }

                    updateAdapter();
                }

                @Override public void newReq(PubSubBindingService.pubSubReqInfo info){
                    Log.d("----NEW-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());



                }
                @Override public void onPatientList(List<String> patientList){
                    Log.d("--setsub-list---", patientList.toString());

                    patList.clear();
                    patList.add(new TitleItem("Current Subscribers"));
                    for (int i =0; i < patientList.size(); i++) {
                        patList.add(new ExistingItem(patientList.get(i)));
                    }

                    updateAdapter();
                }
            }
    );
}
