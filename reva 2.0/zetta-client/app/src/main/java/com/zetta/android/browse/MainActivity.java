package com.zetta.android.browse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.zetta.android.R;
import com.zetta.android.lib.Interval;
import com.zetta.android.lib.RevaNotificationManager;
import com.zetta.android.revaServices.PubSubBindingService;
import com.zetta.android.revaServices.UserManager;
import com.zetta.android.revawebsocketservice.CloudAwaitObject;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;
import com.zetta.android.settings.settingsPage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onNewIntent(Intent gotIntent){
        Log.d("-----onNewIntent-------", "test)");
        //RevaNotificationManager.getInstance().doOnNewIntent(this, intent);
    }



    private static final String TAG = "MainActivity";
    private ProgressDialog dialog;
    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;
    private Drawer result;

    private String m_Text = "";

    private DeviceListActivity dList = new DeviceListActivity();

    private String zettaUser;

    private Interval validateUserUidInterval = null;
    public Context cont = this;

    private Set<String> subbedTo = new TreeSet<>();


    /**
     * Main activity that allows for tab functionality and switching of views
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading your patients and stats..");
        dialog.show();
        Log.d(TAG, "onCreate: Starting.");
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        userManagerEndpoint.bind(this);
        pubSubBinderEndpoint.bind(this);

        userManagerEndpoint.hardGuardActivityByVerifiedUser(workOnUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userManagerEndpoint.unbind(this);
        pubSubBinderEndpoint.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        userManagerEndpoint.resumeGuardActivityByVerifiedUser(workOnUser);
    }

    UserManager.MainActivityEndpoint.WorkOnUser workOnUser
            = new UserManager.MainActivityEndpoint.WorkOnUser(){
        @Override public void work(String userUid){
            bootstrap(userUid);
        }
    };

    static class PatientTag{
        PatientTag(String name_) {
            name = name_;
        }
        public final String name;
    }

    void bootstrap(final String userUid) {
        zettaUser = userUid;//getIntent().getStringExtra("Username");
        UserManager.setViewedUser(userUid);//view self at start

        Log.d("--for stats--", UserManager.getViewedUser());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPageAdapter);

        PrimaryDrawerItem adder = null;
        PrimaryDrawerItem patient = new PrimaryDrawerItem().withIdentifier(1).withName("TMP");
        if(userManagerEndpoint.getUserType() == RevaWebSocketService.USER_TYPE.PATIENT){
            patient.withName(R.string.drawerNameAddPatient).withTag(R.string.drawerNameAddPatient);
            adder = new PrimaryDrawerItem().withName("My vitals").withTag(new PatientTag(userUid)).withIcon(R.drawable.ic_profile);
            dList.setUser(getUser());

        }

        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        SecondaryDrawerItem signOutItem = new SecondaryDrawerItem().withIdentifier(1).withName(R.string.drawerNameSignOut).withIcon(R.drawable.ic_sign_out);
        signOutItem.withTag(R.string.drawerNameSignOut);

        SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(1).withName("My Connections").withIcon(R.drawable.ic_user_manage);
        settings.withTag(5);


        SectionDrawerItem header = new SectionDrawerItem().withName("Patients");
        PrimaryDrawerItem tmpItemForNikki = new PrimaryDrawerItem().withIdentifier(1).withName("For Nikki");
        tmpItemForNikki.withTag(1234);

        final int tmpItemForAcceptId = 521;
        PrimaryDrawerItem tmpItemForAccept = new PrimaryDrawerItem().withIdentifier(tmpItemForAcceptId)
                .withName("tmpItemForAccept");
        final int tmpItemForDeclineId = 522;
        PrimaryDrawerItem tmpItemForDecline = new PrimaryDrawerItem().withIdentifier(tmpItemForDeclineId)
                .withName("tmpItemForDecline");




        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(userUid).withEmail(R.string.serverURL).withIcon(getResources().getDrawable(R.drawable.ic_person_black_24dp))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                }).withSelectionListEnabledForSingleProfile(false)
                .build();


        //create the drawer and remember the `Drawer` result object
        result = new DrawerBuilder().withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(toolbar)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        Object tag = drawerItem.getTag();

                        String name = drawerItem.toString();
                        Log.d("--Name of name", name);
                        if(tag instanceof PatientTag){
                            dList.setUser(((PatientTag)tag).name);
                            setupViewPager(mViewPager);
                            UserManager.setViewedUser(((PatientTag)tag).name);
                            result.closeDrawer();
                        }

                        if(tag != null && tag instanceof Integer){
                            Integer value = (Integer)tag;
                            switch(value){
                                case 5: {
                                    Intent intent = new Intent(MainActivity.this, settingsPage.class);
                                    startActivity(intent);
                                }break;
                                case R.string.drawerNameSignOut:{

                                    AlertDialog.Builder builder = new AlertDialog.Builder(cont, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
                                    builder.setTitle(Html.fromHtml("<font color='#38ACEC'>Are you sure you want to sign out?</font>"));

                                    // Set up the buttons
                                    builder.setPositiveButton(Html.fromHtml("<font color='#38ACEC'>Yes</font>"), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            userManagerEndpoint.triggerLoginIntent();
                                        }
                                    });
                                    builder.setNegativeButton(Html.fromHtml("<font color='#38ACEC'>No</font>"), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                                    builder.show();

                                }break;
                                case R.string.drawerNameSettings: {
                                    Toast.makeText(cont, drawerItem.getTag().toString(), Toast.LENGTH_SHORT).show();
                                }break;
                            }
                        }

                        return true;
                    }
                }).build();

        if (adder != null) {
            result.addItem(adder);
        }

        result.addItem(
                header
        );



        for(String name : subbedTo)
        {
            result.addItem(new PrimaryDrawerItem().withName(name).withTag(new PatientTag(name)).withIcon(R.drawable.ic_profile));
        }

        result.addItems(
                new DividerDrawerItem(),
                settings,
                signOutItem
        );


        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "To be implemented", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }
    // Set up the input


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("RESU", "RESULT IS HAPPEN");
        Log.d("RESCODE", resultCode + "");
        Log.d("REQCODE", requestCode + ""); //TODO: get this to actually work
        mViewPager.setCurrentItem(1);
        mViewPager.getAdapter().notifyDataSetChanged();

        /*if (resultCode == 0) {

            //mViewPager.;
        }*/
        // else... other cases
    }

    /**
     * Function to get user from server
     *
     * @return a user from the server
     */
    public String getUser() {
        return zettaUser;
    }

    /**
     * Method that sets up tab titles
     *
     * @param viewPager the pager of the views to be tabbed
    getTab
     */

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());


        adapter.addFragment(dList, "Vitals");
        adapter.addFragment(new StatFragment(), "Stats");
        adapter.addFragment(new notifications(), "Alerts");

        viewPager.setAdapter(adapter);

    }

    UserManager.MainActivityEndpoint userManagerEndpoint = new UserManager.MainActivityEndpoint(this);


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
                @Override public void sendReplyActionCallback(String username){

                }
            },
            new PubSubBindingService.PubSubInfoWorker(){
                @Override public void onConnect(Map<String,PubSubBindingService.PubSubReqInfo> infoMap){
                    for(Map.Entry<String,PubSubBindingService.PubSubReqInfo> entry : infoMap.entrySet()){
                        PubSubBindingService.PubSubReqInfo info =  entry.getValue();
                        Log.d("----ALL-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                    }
                }
                @Override public void newReq(PubSubBindingService.PubSubReqInfo info){
                    Log.d("----NEW-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                }
                @Override public void onPatientList(List<String> patientList){
                    Log.d("----sub-list---", patientList.toString());

                    for(String names : patientList)
                    {
                        subbedTo.add(names);
                    }
                }
                @Override public void doneCallback(){
                    Log.d("----doneCallback---", "--done--");
                    if(userManagerEndpoint.getUserType() != RevaWebSocketService.USER_TYPE.PATIENT){
                        if (subbedTo.isEmpty()) {
                            dList.setUser("new");
                        }
                        else {
                            dList.setUser(subbedTo.iterator().next());
                        }
                    }

                    userManagerEndpoint.resumeGuardActivityByVerifiedUser(workOnUser);

                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
    );


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
