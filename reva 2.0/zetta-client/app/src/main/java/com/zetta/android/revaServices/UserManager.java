package com.zetta.android.revaServices;

import android.app.Activity;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.browse.MainActivity;
import com.zetta.android.browse.Registration_Cont;
import com.zetta.android.browse.Registration_Patient;
import com.zetta.android.browse.login_activity;
import com.zetta.android.lib.Interval;
import com.zetta.android.lib.NotifyCloudAwait;
import com.zetta.android.revawebsocketservice.CloudAwaitObject;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ME on 2017/09/30.
 */

public class UserManager extends RevaService {
    public static final String SERVICE_KEY = "UserManager";

    static String viewedUser = "";

    static public void setViewedUser(String viewedUser_){
        viewedUser = viewedUser_;
    }
    static public String getViewedUser(){
        return viewedUser;
    }
    private UserManager() {
    }

    static UserManager self = null;

    public static UserManager instance() {
        if (self == null) {
            self = new UserManager();
        }
        return self;
    }


    public static class MainActivityEndpoint extends RevaWebsocketEndpoint {
        public RevaWebSocketService.USER_TYPE getUserType(){
            return webService.getUserType();
        }
        public static abstract class WorkOnUser{
            abstract public void work(String userUid);
        }
        @Override
        public String key() {
            return "UserManager";
        }
        public MainActivityEndpoint(
                Activity activity_
        ){
            activity = activity_;
        }

        Interval validateUserUidInterval;
        public void hardGuardActivityByVerifiedUser(final WorkOnUser then){

            if (validateUserUidInterval != null) {
                validateUserUidInterval.clearInterval();
            }
            validateUserUidInterval = new Interval(100, Integer.MAX_VALUE) {
                @Override
                public void work() {
                    if (webService != null) {
                        self.clearInterval();
                    }
                }
                @Override
                public void end() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (webService.isLoggedIn()) {
                                then.work(webService.getAuthId());
                            } else {
                                triggerLoginIntent();
                            }
                        }
                    });
                }
                Interval self = this;
            };
        }
        public void resumeGuardActivityByVerifiedUser(WorkOnUser then){
            if (webService != null) {
                //Note: do nothing and wait for onCreate to build the webService
                if (webService.isLoggedIn()) {
                    then.work(webService.getAuthId());
                } else {
                    triggerLoginIntent();
                }
            }
        }

        public void triggerLoginIntent(){
            webService.signOut();
            Intent intent = new Intent(activity, login_activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }

        final Activity activity;
        @Override
        public void onServiceConnect(RevaWebSocketService service) {
            webService = service;
        }
        RevaWebSocketService webService = null;
    }






    public static class RegisterEndpoint extends RevaWebsocketEndpoint {
        @Override
        public String key() {
            return SERVICE_KEY;
        }

        public RegisterEndpoint(Activity activity_) {
            activity = activity_;
        }

        public void sendRequest(boolean isPatient_,
                                String regEmail_,
                                String regPass_,
                                EditText emailText_,
                                AlertDialog.Builder builder

        ) {
            patient = isPatient_;
            regEmail = regEmail_;
            regPass = regPass_;
            emailText = emailText_;


            attachCloudAwaitObject(builder, validateEmailCAO)
                    .send(activity, "VALIDATE_EMAIL", regEmail)
                    .then(registerNonPatientCAO);
        }

        boolean patient;
        String regEmail, regPass;
        EditText emailText;


        private final Activity activity;

        CloudAwaitObject validateEmailCAO = new CloudAwaitObject("REGISTER") {
            @Override
            public Object get(Object obj, Object localMsg, CloudAwaitObject cao) {
                Object ret = null;
                try {
                    final AlertDialog.Builder builder1 = (AlertDialog.Builder) localMsg;
                    final LinkedTreeMap<String, Object> got = (LinkedTreeMap<String, Object>) obj;
                    if ((boolean) got.get("PASS")) {
                        if (patient) {
                            /////////////////////////////////////////change intent to Registration_cont
                            Intent toReg = new Intent(activity, Registration_Patient.class);
                            toReg.putExtra("regEmail", regEmail);
                            toReg.putExtra("regPass", regPass);
                            activity.startActivityForResult(toReg, 0);
                            return true;
                        } else {
                            //pass the builder
                            Map<String, Object> sendMap = new TreeMap<>();
                            sendMap.put("Email", regEmail);
                            sendMap.put("Password", regPass);
                            return new CloudAwaitObject.Chain(activity, "REGISTER_NON_PATIENT", sendMap, builder1);
                        }
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                emailText.setError((String) got.get("ERROR"));
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.toString());
                }
                return ret;
            }
        };
        CloudAwaitObject registerNonPatientCAO = new CloudAwaitObject("REGISTER") {
            @Override
            public Object get(Object obj, Object localMsg, CloudAwaitObject cao) {
                Object ret = null;
                final AlertDialog.Builder builder1 = (AlertDialog.Builder) localMsg;
                try {
                    final LinkedTreeMap<String, Object> got = (LinkedTreeMap<String, Object>) obj;
                    if ((boolean) got.get("NON_PATIENT_PASS")) {
                        ret = true;
                        getService().setLogin(regEmail, regPass);
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, "Welcome " + regEmail, Toast.LENGTH_LONG).show();
                                final Intent intent = new Intent(activity, MainActivity.class);
                                intent.putExtra("Username", regEmail.toString());
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                                getService().atLoginValidation(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                activity.startActivity(intent);
                                            }
                                        },
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                activity.startActivity(intent);
                                            }
                                        },
                                        100,
                                        19
                                );
                            }
                        });
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    builder1.setMessage((String) got.get("NON_PATIENT_ERROR"));
                                    AlertDialog alertWarning = builder1.create();
                                    alertWarning.show();
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(this.getClass().getName(), e.toString());
                }
                return ret;
            }
        };
    }


    public static class RegisterPatientEndpoint extends RevaWebsocketEndpoint {
        @Override
        public String key() {
            return SERVICE_KEY;
        }

        public RegisterPatientEndpoint(Activity activity_) {
            activity = activity_;
        }

        final Activity activity;

        public void sendRequest(
                final AlertDialog.Builder builder,
                final String regEmail,
                final String regPass,
                final String address,
                final String username,
                final String reason
        ) {
            Map<String, String> sendMap = new TreeMap<>();
            sendMap.put("Email", regEmail);
            sendMap.put("Password", regPass);
            sendMap.put("Address", address);
            sendMap.put("Username", username);
            sendMap.put("Reason", reason);

            attachCloudAwaitObject(true, new CloudAwaitObject("REGISTER") {
                @Override
                public Object get(Object obj, Object localMsg, CloudAwaitObject cao) {
                    Object ret = null;
                    try {
                        final LinkedTreeMap<String, Object> got = (LinkedTreeMap<String, Object>) obj;
                        if ((boolean) got.get("PATIENT_PASS")) {
                            ret = true;
                            getService().setLogin(username, reason);
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activity, "Welcome " + username, Toast.LENGTH_LONG).show();
                                    final Intent intent = new Intent(activity, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("Username", username.toString());
                                    getService().atLoginValidation(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    activity.startActivity(intent);
                                                }
                                            },
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    activity.startActivity(intent);
                                                }
                                            },
                                            100,
                                            19
                                    );
                                }
                            });
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        builder.setMessage((String) got.get("PATIENT_ERROR"));
                                        AlertDialog alertWarning = builder.create();
                                        alertWarning.show();
                                    } catch (Exception e) {
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), e.toString());
                    }
                    return ret;
                }
            }).send(activity, "REGISTER_PATIENT", sendMap);
        }

    }

    public static class LoginEndpoint extends RevaWebsocketEndpoint {
        public void setEditText(EditText userUid_, EditText password_){
            userUidEditText = userUid_;
            passwordEditText = password_;
        }
        public LoginEndpoint(Activity activity_) {
            activity = activity_;
        }

        NotifyCloudAwait notifyWait = null;

        public void tryLogin(String userUid, String password){
            buildLoginAwaitObject(userUid, password);
        }
        public void buildLoginAwaitObject(final String userUid, final String password) {
            getService().setLogin(userUid, password);
            if (notifyWait == null) {
                notifyWait = new NotifyCloudAwait(activity, false,
                        750, " ... validating your input ... ", 5000) {
                    @Override
                    public void end(NotifyCloudAwait.DISMISS_TYPE type) {
                        if (type == DISMISS_TYPE.TIMEOUT) {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                            builder1.setTitle("Could not connect to the cloud");
                            builder1.setMessage("try again ?");
                            builder1.setCancelable(true);
                            builder1.setPositiveButton("YES",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            notifyWait = null;
                                            buildLoginAwaitObject(userUid, password);
                                            dialog.dismiss();
                                        }
                                    });
                            builder1.setNegativeButton("NO",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            notifyWait = null;
                                            dialog.dismiss();
                                        }
                                    });
                            builder1.show();
                        }
                    }
                };
                userUidEditText.setError(null);
                passwordEditText.setError(null);
            }
        }

        @Override
        public String key() {
            return SERVICE_KEY;
        }

        Activity activity;

        public final void onMessage(final LinkedTreeMap obj) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String USER_MANAGER_KEY_CONNECTED = "CONNECTED";
                    if (notifyWait != null && obj.containsKey(USER_MANAGER_KEY_CONNECTED)) {
                        notifyWait.dismiss(true);
                        notifyWait = null;
                        if (tryLoginInterval == null) {
                            tryLoginInterval = new Interval(1, Integer.MAX_VALUE) {
                                @Override
                                public void work() {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (tryLoginInterval != null && getService().isLoggedIn()) {
                                                tryLoginInterval.clearInterval();
                                                tryLoginInterval = null;
                                            }
                                        }
                                    });
                                }
                                @Override
                                public void end() {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(activity, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("Username", getService().getAuthId());
                                            activity.startActivity(intent);
                                        }
                                    });
                                }
                            };
                        }
                        Map<String, Object> info = (Map<String, Object>) obj.get(USER_MANAGER_KEY_CONNECTED);
                        String userUid = (String) info.get("USER_UID");
                        if (userUid.compareTo(RevaWebSocketService.SPECIAL_USER_ANONYMOUS) != 0) {
                        } else {
                            final Map<String, String> errorMap = (Map<String, String>) info.get("ERROR");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (errorMap != null) {
                                        String error = errorMap.get("text");
                                        if (errorMap.get("field").compareTo("password") == 0) {
                                            passwordEditText.setError(error);
                                        } else {
                                            userUidEditText.setError(error);
                                        }
                                    } else {
                                        userUidEditText.setError("You must specify a username");
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

        EditText userUidEditText, passwordEditText;
        Interval tryLoginInterval = null;
    }
}
