package com.zetta.android.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.zetta.android.R;
import com.zetta.android.browse.MainActivity;
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
    private List<SettingsItem> pendList = new ArrayList<>();



    //TODO: check for when a list is empty, so you can still display the header and a message
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

                SettingsItem item = settingsListAdapter.getSettings().get(position);
                reqList.remove(item);

                updateAdapter();
            }
            @Override
            public void buttonNoOnClick(View v, int position) {
                pubSubBinderEndpoint.pubSubRequestReply(
                        ((RequestItem)settingsListAdapter.getSettings().get(position)).getTitle(),PubSubBindingService.pubSubReqInfo.REPLY.DECLINE
                );

                SettingsItem item = settingsListAdapter.getSettings().get(position);
                reqList.remove(item);

                updateAdapter();
            }
            @Override
            public void deleteOnClick(View v, int position) {
                Log.d("here", "deleteOnClick at position"+position);
                pubSubBinderEndpoint.dropPubSubBindingAsSubscriber(((ExistingItem)settingsListAdapter.getSettings().get(position)).getTitle());
            }

            @Override
            public void settingsButtonOnClick(View v, int position) {
                addAlert();
            }
        });

        settingsList.setAdapter(settingsListAdapter);
        pubSubBinderEndpoint.bind(this);
    }

    private void updateAdapter() {
        List<SettingsItem> tmp = new ArrayList<>();
        tmp.addAll(patList);
        tmp.add(new ButtonItem("ADD PATIENT"));
        tmp.addAll(reqList);
        tmp.add(new TitleItem("Pending Requests"));
        tmp.addAll(pendList);

        settingsListAdapter.updateList(tmp);
    }

    public void addAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        builder.setTitle(Html.fromHtml("<font color='#38ACEC'>Type in the email of the person you wish to add</font>"));

        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        input.setHint("jondoe@email.com");
        input.setHintTextColor(getResources().getColor(R.color.md_blue_grey_500));
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.inputDialogLeft);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.inputDialogRight);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.inputDialogTop);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton(Html.fromHtml("<font color='#38ACEC'>OK</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pubSubBinderEndpoint.pubSubBindingRequest(input.getText().toString());
            }
        });
        builder.setNegativeButton(Html.fromHtml("<font color='#38ACEC'>Cancel</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void alert(String message, final String buttonMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
        builder.setTitle(message);

        // Set up the buttons
        builder.setPositiveButton(buttonMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (buttonMsg.equals("Try Again")) {
                    addAlert();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
                        alert("Succesfully sent request", "OK");
                    } else {
                        alert(msg, "Try Again");
                    }
                    Log.d("------TEST---------", msg);
                }
                @Override public void sendReplyActionCallback(String userUid){
                    Log.d("--sendReplyActionCall--", userUid);
                }
            },
            new PubSubBindingService.PubSubInfoWorker(){
                @Override public void onConnect(Map<String, PubSubBindingService.pubSubReqInfo> infoMap){
                    List<SettingsItem> tmp = new ArrayList<>();
                    reqList.clear();
                    pendList.clear();
                    reqList.add(new TitleItem("Subscriber Requests"));
                    for(Map.Entry<String, PubSubBindingService.pubSubReqInfo> entry : infoMap.entrySet()){
                        PubSubBindingService.pubSubReqInfo info =  entry.getValue();
                        Log.d("----ALL-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());

                        if(info.type.toString().equals("REQUESTER")){
                            pendList.add(new PendingItem(info.userUid));
                        } else {
                            reqList.add(new RequestItem(info.userUid));
                        }

                    }
                    updateAdapter();
                }

                @Override public void newReq(PubSubBindingService.pubSubReqInfo info){
                    Log.d("----NEW-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                }

                @Override public void onPatientList(List<String> patientList){
                    Log.d("--setsub-list---", patientList.toString());

                    patList.clear();
                    patList.add(new TitleItem("Current Patients"));
                    for (int i =0; i < patientList.size(); i++) {
                        patList.add(new ExistingItem(patientList.get(i)));
                    }

                    updateAdapter();
                }

                @Override public void onSubscriberList(List<String> subscriberList) {
                    Log.d("--onSubscriberList---", subscriberList.toString());
                }
            }
    );
}
