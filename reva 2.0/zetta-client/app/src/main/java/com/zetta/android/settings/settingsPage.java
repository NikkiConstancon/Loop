package com.zetta.android.settings;

import android.app.ProgressDialog;
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

import com.zetta.android.R;
import com.zetta.android.revaServices.PubSubBindingService;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class settingsPage extends AppCompatActivity {
    RecyclerView settingsList;
    SettingsListAdapter settingsListAdapter;
    private List<SettingsItem> settings = new ArrayList<>();
    private List<SettingsItem> patList = new ArrayList<>();
    private List<SettingsItem> reqList = new ArrayList<>();
    private List<SettingsItem> pendList = new ArrayList<>();
    private ProgressDialog dialog;
    private List<SettingsItem> subList = new ArrayList<>();



    //TODO: check for when a list is empty, so you can still display the header and a message
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new ProgressDialog(this);

        settingsList = (RecyclerView) findViewById(R.id.settings_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        settingsList.setLayoutManager(layoutManager);
        settingsList.setHasFixedSize(true);

        settingsListAdapter = new SettingsListAdapter(settings, new SettingsListAdapter.MyAdapterListener() {
            @Override
            public void buttonYesOnClick(View v, int position) {
                pubSubBinderEndpoint.pubSubRequestReply(
                        ((RequestItem) settingsListAdapter.getSettings().get(position)).getTitle(), PubSubBindingService.PubSubReqInfo.REPLY.ACCEPT
                );
                /*
                SettingsItem item = settingsListAdapter.getSettings().get(position);
                reqList.remove(item);

                updateAdapter();
                */
            }

            @Override
            public void buttonNoOnClick(View v, int position) {
                SettingsItem item = settingsListAdapter.getSettings().get(position);
                Log.d("del", "" + reqList.remove(item));
                dialog.setMessage("Rejecting the request...");
                dialog.show();
                pubSubBinderEndpoint.pubSubRequestReply(
                        ((RequestItem) settingsListAdapter.getSettings().get(position)).getTitle(), PubSubBindingService.PubSubReqInfo.REPLY.DECLINE
                );
                /*
                SettingsItem item = settingsListAdapter.getSettings().get(position);
                reqList.remove(item);
                updateAdapter();
                */
            }

            @Override
            public void deleteOnClick(View v, int position) {
                Log.d("here", "deleteOnClick at position" + position);
                pubSubBinderEndpoint.dropPubSubBindingAsSubscriber(((ExistingItem) settingsListAdapter.getSettings().get(position)).getTitle());
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
        boolean isPatient = pubSubBinderEndpoint.getService().getUserType() == RevaWebSocketService.USER_TYPE.PATIENT;
        List<SettingsItem> tmp = new ArrayList<>();
        /*
        if(isPatient) {
            tmp.add(new TitleItem("Sharing My Info With"));
            tmp.addAll(subList);
        }
        */
        tmp.add(new TitleItem(isPatient ? "Sharing My Info With" : "Subscribed Patients"));
        tmp.addAll(patList);
        tmp.add(new ButtonItem(isPatient ? "Share With New Contact" : "Add Patient"));
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
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
            new PubSubBindingService.PubSubWorker() {
                @Override
                public void sendRequestCallback(final String msg) {
                    //You no longer need to do the ugly runOnUiThread
                    Log.d("MEAS", msg);
                    if (msg.equals("")) {
                        alert("Succesfully sent request", "OK");
                    } else {
                        alert(msg, "Try Again");
                    }
                    Log.d("------TEST---------", msg);
                }

                @Override
                public void sendReplyActionCallback(String userUid) {
                    Log.d("--sendReplyActionCall--", userUid);
                    dialog.dismiss();
//                    if (userUid.equals("")) {
//                        //Failed
//                        alert("Failed.", "OK");
//                    } else {
//                        //Success
//                        alert("Success!", "OK");
//                    }
                    updateAdapter();
                }
            },
            new PubSubBindingService.PubSubInfoWorker() {
                @Override
                public void onConnect(Map<String, PubSubBindingService.PubSubReqInfo> infoMap) {
                }

                @Override
                public void newReq(PubSubBindingService.PubSubReqInfo info) {
                    Log.d("----NEW-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                    reqList.add(new RequestItem(info.userUid));
                }

                @Override
                public void onPatientList(List<String> patientList) {
                }

                @Override
                public void onSubscriberList(List<String> subscriberList) {
                    Log.d("--onSubscriberList---", subscriberList.toString());
                }

                @Override
                public void doneCallback(
                        List<String> patientList,
                        List<String> subscriberList,
                        List<PubSubBindingService.PubSubReqInfo> reqInfoList
                ) {
                    Set<String> added = new TreeSet<>();
                    List<SettingsItem> tmp = new ArrayList<>();

                    patList.clear();
                    subList.clear();
                    pendList.clear();
                    reqList.clear();
                    for (String userUid : patientList) {
                        added.add(userUid);
                        patList.add(new ExistingItem(userUid));
                    }
                    for (String userUid : subscriberList) {
                        if (!added.contains(userUid)) subList.add(new ExistingItem(userUid));
                    }
                    for (PubSubBindingService.PubSubReqInfo info : reqInfoList) {
                        if (info.type == PubSubBindingService.PubSubReqInfo.TYPE.REQUESTER) {
                            pendList.add(new PendingItem(info.userUid));
                        } else {
                            reqList.add(new RequestItem(info.userUid));
                        }
                    }


                    boolean isPatient = pubSubBinderEndpoint.getService().getUserType() == RevaWebSocketService.USER_TYPE.PATIENT;

                    if (reqList.size() > 0) {
                        tmp.add(new TitleItem("New Connection Requests"));
                        tmp.addAll(reqList);
                    }
                    boolean flagGotConnections = patList.size() > 0 || subList.size() > 0;
                    tmp.add(new TitleItem(
                            flagGotConnections
                                    ? (isPatient ? "Sharing My Info With" : "Subscribed Patients")
                                    : ("You are not connected ... add someone")
                    ));
                    tmp.addAll(patList);
                    tmp.addAll(subList);
                    tmp.add(new ButtonItem(isPatient ? "Share With New Contact" : "Add Patient"));
                    tmp.add(new TitleItem(pendList.size() > 0 ? "Requests waiting to be accepted" : "You have no pending requests"));
                    tmp.addAll(pendList);

                    settingsListAdapter.updateList(tmp);
                }
            }
    );
}
