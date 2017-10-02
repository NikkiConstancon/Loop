package com.zetta.android.browse;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.AndroidResources;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.zetta.android.R;
import com.zetta.android.lib.Interval;
import com.zetta.android.revaServices.UserManager;
import com.zetta.android.revawebsocketservice.CloudAwaitObject;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    private String m_Text = "";


    private String zettaUser;

    private Interval validateUserUidInterval = null;
    public Context cont = this;


    /**
     * Main activity that allows for tab functionality and switching of views
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Starting.");
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        userManagerEndpoint.bind(this);
        statTmpForNikkiEndpoint.bind(this);

        userManagerEndpoint.hardGuardActivityByVerifiedUser(workOnUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userManagerEndpoint.unbind(this);
        statTmpForNikkiEndpoint.unbind(this);
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

    void bootstrap(String zettaUser_) {
        zettaUser = zettaUser_;//getIntent().getStringExtra("Username");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPageAdapter);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        SecondaryDrawerItem signOutItem = new SecondaryDrawerItem().withIdentifier(1).withName(R.string.drawerNameSignOut);
        signOutItem.withTag(R.string.drawerNameSignOut);


        PrimaryDrawerItem adder;

        PrimaryDrawerItem patient = new PrimaryDrawerItem().withIdentifier(1).withName("TMP");
        if(userManagerEndpoint.getUserType() == RevaWebSocketService.USER_TYPE.PATIENT){
            patient.withName(R.string.drawerNameAddPatient).withTag(R.string.drawerNameAddPatient);
             adder = new PrimaryDrawerItem().withName(R.string.drawerNameAddSub).withTag(R.string.drawerNameAddSub);
        }else{
            adder = new PrimaryDrawerItem().withName(R.string.drawerNameAddPatient).withTag(R.string.drawerNameAddPatient);
        }


        PrimaryDrawerItem tmpItemForNikki = new PrimaryDrawerItem().withIdentifier(1).withName("For Nikki");
        tmpItemForNikki.withTag(1234);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("Nikki").withEmail("nikki@gmail.com").withIcon(getResources().getDrawable(R.drawable.ic_person_black_24dp))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();


        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder().withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        adder,
                        new DividerDrawerItem(),
                        signOutItem,
                        tmpItemForNikki
                )

                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        // do something with the clicked item :D
                        Object tag = drawerItem.getTag();

                        if(tag != null && tag instanceof Integer){
                            Integer value = (Integer)tag;
                            switch(value){
                                case R.string.drawerNameSignOut:{
                                    userManagerEndpoint.triggerLoginIntent();
                                }break;
                                case R.string.drawerNameAddPatient:{
                                    addAlert();
                                }break;
                                case R.string.drawerNameAddSub:{
                                    addAlert();
                                }break;
                                case R.string.drawerNameSettings: {
                                    Toast.makeText(cont, drawerItem.getTag().toString(), Toast.LENGTH_SHORT).show();
                                }break;
                                case 123:{
                                    userManagerEndpoint.pubSubBindingRequest("what@sub.com");
                                }break;
                                case 1234:{
                                    statTmpForNikkiEndpoint.attachCloudAwaitObject(
                                            null,
                                            statTmpForNikki
                                    ).send(MainActivity.this, "RAW", "THE MSG");
                                }break;
                            }
                        }

                        return true;
                    }
                })
                .build();


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

    public void addAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MaterialBaseTheme_Light_AlertDialog);
        builder.setTitle("Type in email of person to add");

        final EditText input = new EditText(MainActivity.this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("jondoe@email.com");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(Html.fromHtml("<font color='black'>OK</font>"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                userManagerEndpoint.pubSubBindingRequest(m_Text);
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

    public void alert(String message, final String buttonMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MaterialBaseTheme_Light_AlertDialog);
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
     */
    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new DeviceListActivity(), "Vitals");
        adapter.addFragment(new StatFragment(), "Stats");
        adapter.addFragment(new notifications(), "Alerts");
        viewPager.setAdapter(adapter);
    }
    UserManager.MainActivityEndpoint userManagerEndpoint = new UserManager.MainActivityEndpoint(
            this,
            new UserManager.MainActivityEndpoint.PubSubWorker(){
                @Override public void work(final String msg){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("MEAS", msg);
                            if (msg.equals("")) {
                                alert("Succesfully sent request", "OK");
                            } else {
                                alert(msg, "Try Again");
                            }
                        }
                    });
                    Log.d("------TEST---------", msg);
                }
            },
            new UserManager.MainActivityEndpoint.PubSubInfoWorker(){
                @Override public void onConnect(Map<String, UserManager.MainActivityEndpoint.pubSubReqInfo> infoMap){
                    for(Map.Entry<String, UserManager.MainActivityEndpoint.pubSubReqInfo> entry : infoMap.entrySet()){
                        UserManager.MainActivityEndpoint.pubSubReqInfo info =  entry.getValue();
                        Log.d("----ALL-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                    }
                }
                @Override public void newReq(UserManager.MainActivityEndpoint.pubSubReqInfo info){
                    Log.d("----NEW-PUB-SUB-REQ---", info.userUid + " " + info.state.toString() + " " + info.type.toString());
                }
                @Override public void onPatientList(List<String> patientList){
                    Log.d("----sub-list---", patientList.toString());
                }
            }
    );





    StatTmpForNikkiEndpoint statTmpForNikkiEndpoint = new StatTmpForNikkiEndpoint();
    public static class StatTmpForNikkiEndpoint extends RevaWebsocketEndpoint {
        @Override
        public String key() {
            return "Stats";
        }
    }
    public CloudAwaitObject statTmpForNikki = new CloudAwaitObject("GRAPH_POINTS") {
        @Override
        public Object get(Object obj, Object localMsg, CloudAwaitObject cao) {
            return null;
        }
    };
}
