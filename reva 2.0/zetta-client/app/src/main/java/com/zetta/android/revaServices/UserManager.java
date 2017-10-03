package com.zetta.android.revaServices;

import android.app.Activity;
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
                Activity activity_,
                PubSubWorker pubSubWorker_,
                PubSubInfoWorker pubSubInfoWorker_
        ){
            activity = activity_;
            pubSubWorker = pubSubWorker_;
            pubSubInfoWorker = pubSubInfoWorker_;
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

        public void pubSubBindingRequest(String target){
            attachCloudAwaitObject(null, pubSubReqCAO).send(activity, "REQ_BIND", target);
        }

        static public class pubSubReqInfo{
            public enum TYPE {REQUESTER, TARGET};
            public enum STATE {PENDING, DELIVERED, ACCEPTED};
            public enum REPLY {ACCEPT, DECLINE};
            public final String userUid;
            public final TYPE type;
            public final STATE state;

            public pubSubReqInfo(
                    String userUid_,
                    final String typeStr ,
                    final String stateStr
            ){
                userUid = userUid_;

                if(typeStr.compareTo("request") == 0){
                    type = TYPE.REQUESTER;
                }else{
                    type = TYPE.TARGET;
                }
                switch (stateStr){
                    case "pending":{
                        state = STATE.PENDING;
                    }break;
                    case "delivered":{
                        state = STATE.DELIVERED;
                    }break;
                    default:{
                        state = STATE.ACCEPTED;
                    }
                }
            }
        }

        List<String> patientList = new ArrayList<>();
        Map<String, pubSubReqInfo> pubSubInfoMap = new TreeMap<>();

        public ArrayList<String> getPubSubList(){
            return new ArrayList<>(pubSubInfoMap.keySet());
        }

        pubSubReqInfo getPubSubInfo(int i){
            ArrayList names = getPubSubList();
            return pubSubInfoMap.get(names.get(i));
        }

        final Activity activity;
        @Override
        public void onMessage(final LinkedTreeMap obj){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Map<String, Object> gotMapKeys = obj;
                    for(Map.Entry<String, Object> entry : gotMapKeys.entrySet()){
                        switch (entry.getKey()){
                            case "BINDING_CONFIRMATION_REQ":{
                                Map<String, Map<String, String>> gotMap = (Map<String, Map<String, String>>)entry.getValue();
                                for(Map.Entry<String, Map<String, String>> entryInfo : gotMap.entrySet()){
                                    Map<String, String> info = entryInfo.getValue();
                                    pubSubInfoMap.put(entryInfo.getKey(), new pubSubReqInfo(entryInfo.getKey(), info.get("type"), info.get("state")));
                                }
                                pubSubInfoWorker.onConnect(pubSubInfoMap);
                            }break;
                            case "NEW_BINDING_CONFIRMATION_REQ":{
                                Map<String, String> entryInfo = (Map<String, String>)entry.getValue();
                                pubSubReqInfo info =
                                        new pubSubReqInfo(
                                                entryInfo.get("userUid"),
                                                entryInfo.get("type"),
                                                entryInfo.get("state")
                                        );

                                pubSubInfoMap.put(entryInfo.get("userUid"),info);
                                pubSubInfoWorker.newReq(info);
                            }break;
                            case "PATIENT_LIST":{
                                patientList = (ArrayList<String>)entry.getValue();
                                pubSubInfoWorker.onPatientList(patientList);
                            }break;
                        }
                    }
                }
            });
        }
        @Override
        public void onServiceConnect(RevaWebSocketService service) {
            webService = service;
        }
        RevaWebSocketService webService = null;
        CloudAwaitObject pubSubReqCAO = new CloudAwaitObject("BIND_PATIENT_AND_SUBSCRIBER") {
            @Override
            public Object get(final Object obj, Object localMsg, CloudAwaitObject cao) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pubSubWorker.sendRequestCallback((String)obj);
                    }
                });
                return null;
            }
        };


        public void pubSubRequestReply(String userUid, pubSubReqInfo.REPLY reply){
            attachCloudAwaitObject(null, pubSubReqReplyCAO).send(activity, reply.toString(), userUid);
        }
        final CloudAwaitObject pubSubReqReplyCAO = new CloudAwaitObject("BIND_PATIENT_AND_SUBSCRIBER") {
            @Override
            public Object get(Object obj, Object localMsg, CloudAwaitObject cao) {
                pubSubWorker.sendReplyActionCallback((boolean)obj);
                return null;
            }
        };


        public static abstract class PubSubWorker{
            abstract public void sendRequestCallback(String msg);
            abstract public void sendReplyActionCallback(boolean sucsess);
        }
        final PubSubWorker pubSubWorker;
        public static abstract class PubSubInfoWorker{
            abstract public void onConnect(Map<String, pubSubReqInfo> infoMap);
            abstract public void newReq(pubSubReqInfo info);
            abstract public void onPatientList(List<String> patientList);
        }
        final PubSubInfoWorker pubSubInfoWorker;
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
                            Intent toReg = new Intent(activity, Registration_Cont.class);
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
            getService().setLogin(userUid, password);
            buildLoginAwaitObject();
        }
        public void buildLoginAwaitObject() {
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
                                            buildLoginAwaitObject();
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

        public final void onMessage(LinkedTreeMap obj) {
            //TODO move this to
            final String USER_MANAGER_KEY_CONNECTED = "CONNECTED";
            if (obj.containsKey(USER_MANAGER_KEY_CONNECTED)) {
                notifyWait.dismiss(true);
                notifyWait = null;
                Map<String, Object> info = (Map<String, Object>) obj.get(USER_MANAGER_KEY_CONNECTED);
                String userUid = (String) info.get("USER_UID");
                if (userUid.compareTo(RevaWebSocketService.SPECIAL_USER_ANONYMOUS) != 0) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Username", userUid.toString());
                    activity.startActivity(intent);
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

        EditText userUidEditText, passwordEditText;
    }
}
