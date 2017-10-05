package com.zetta.android.revaServices;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.R;
import com.zetta.android.revawebsocketservice.CloudAwaitObject;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;
import com.zetta.android.settings.settingsPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ME on 2017/10/03.
 */

public class PubSubBindingService extends RevaWebsocketEndpoint {
    public RevaWebSocketService.USER_TYPE getUserType() {
        return webService.getUserType();
    }

    public static abstract class WorkOnUser {
        abstract public void work(String userUid);
    }

    @Override
    public String key() {
        return "PubSubBindingService";
    }

    public PubSubBindingService(
            Context context_,
            PubSubWorker pubSubWorker_,
            PubSubInfoWorker pubSubInfoWorker_
    ) {
        context = context_;
        pubSubWorker = pubSubWorker_;
        pubSubInfoWorker = pubSubInfoWorker_;
    }


    public void pubSubBindingRequest(String target) {
        attachCloudAwaitObject(null, pubSubReqCAO).send(context, "REQ_BIND", target);
    }
    public void dropPubSubBindingAsSubscriber(String target){
        attachCloudAwaitObject(null, pubSubReqDropBindingCAO).send(context, "DROP_PUB_SUB_BINDING_AS_SUB", target);
    }
    public void dropPubSubBindingAsPatient(String target){
        attachCloudAwaitObject(null, pubSubReqDropBindingCAO).send(context, "DROP_PUB_SUB_BINDING_AS_PAT", target);
    }
    final CloudAwaitObject pubSubReqDropBindingCAO = new CloudAwaitObject("BIND_PATIENT_AND_SUBSCRIBER") {
        @Override
        public Object get(Object obj, Object localMsg, CloudAwaitObject cao) {
            return null;
        }
    };

    static public class PubSubReqInfo {
        public enum TYPE {REQUESTER, TARGET}

        ;

        public enum STATE {PENDING, DELIVERED, ACCEPTED}

        ;

        public enum REPLY {ACCEPT, DECLINE}

        ;
        public final String userUid;
        public final TYPE type;
        public final STATE state;

        public PubSubReqInfo(
                String userUid_,
                final String typeStr,
                final String stateStr
        ) {
            userUid = userUid_;

            if (typeStr.compareTo("request") == 0) {
                type = TYPE.REQUESTER;
            } else {
                type = TYPE.TARGET;
            }
            switch (stateStr) {
                case "pending": {
                    state = STATE.PENDING;
                }
                break;
                case "delivered": {
                    state = STATE.DELIVERED;
                }
                break;
                default: {
                    state = STATE.ACCEPTED;
                }
            }
        }
    }

    List<String> patientList = new ArrayList<>();
    List<String> subscriberList = new ArrayList<>();
    Map<String, PubSubReqInfo> pubSubInfoMap = new TreeMap<>();

    public ArrayList<String> getPubSubList() {
        return new ArrayList<>(pubSubInfoMap.keySet());
    }

    PubSubReqInfo getPubSubInfo(int i) {
        ArrayList names = getPubSubList();
        return pubSubInfoMap.get(names.get(i));
    }

    final Context context;

    void doOnMessage(final LinkedTreeMap obj) {
        Map<String, Object> gotMapKeys = obj;
        for (Map.Entry<String, Object> entry : gotMapKeys.entrySet()) {
            switch (entry.getKey()) {
                case "BINDING_CONFIRMATION_REQ": {
                    Map<String, Map<String, String>> gotMap = (Map<String, Map<String, String>>) entry.getValue();
                    for (Map.Entry<String, Map<String, String>> entryInfo : gotMap.entrySet()) {
                        Map<String, String> info = entryInfo.getValue();
                        pubSubInfoMap.put(entryInfo.getKey(), new PubSubReqInfo(entryInfo.getKey(), info.get("type"), info.get("state")));
                    }
                    pubSubInfoWorker.onConnect(pubSubInfoMap);
                }
                break;
                case "NEW_BINDING_CONFIRMATION_REQ": {
                    Map<String, String> entryInfo = (Map<String, String>) entry.getValue();
                    PubSubReqInfo info =
                            new PubSubReqInfo(
                                    entryInfo.get("userUid"),
                                    entryInfo.get("type"),
                                    entryInfo.get("state")
                            );

                    pubSubInfoMap.put(entryInfo.get("userUid"), info);
                    pubSubInfoWorker.newReq(info);

                }
                break;
                case "PATIENT_LIST": {
                    patientList = (ArrayList<String>) entry.getValue();
                    pubSubInfoWorker.onPatientList(patientList);
                }
                case "SUBSCRIBER_LIST": {
                    subscriberList = (ArrayList<String>) entry.getValue();
                    pubSubInfoWorker.onSubscriberList(subscriberList);
                }
                break;
                case "DONE": {
                    pubSubInfoWorker.doneCallback();
                    pubSubInfoWorker.doneCallback(
                            patientList,
                            subscriberList,
                            new ArrayList<>(pubSubInfoMap.values())
                    );
                    patientList.clear();
                    subscriberList.clear();
                    pubSubInfoMap.clear();
                }
                break;
            }
        }
    }

    @Override
    public void onMessage(final LinkedTreeMap obj) {
        if(context instanceof Activity) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doOnMessage(obj);
                }
            });
        }else{
            doOnMessage(obj);
        }
    }

    @Override
    public void onServiceConnect(RevaWebSocketService service) {
        webService = service;
    }

    RevaWebSocketService webService = null;
    CloudAwaitObject pubSubReqCAO = new CloudAwaitObject("BIND_PATIENT_AND_SUBSCRIBER") {
        @Override
        public Object get(final Object obj, Object localMsg, CloudAwaitObject cao) {
            if(context instanceof Activity) {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (obj instanceof String) {
                            pubSubWorker.sendRequestCallback((String) obj);
                        } else {
                            pubSubWorker.sendRequestCallback("");
                        }
                    }
                });
            }else{
                if (obj instanceof String) {
                    pubSubWorker.sendRequestCallback((String) obj);
                } else {
                    pubSubWorker.sendRequestCallback("");
                }
            }
            return null;
        }
    };




    public void pubSubRequestReply(String userUid, PubSubReqInfo.REPLY reply) {
        attachCloudAwaitObject(null, pubSubReqReplyCAO).send(context, reply.toString(), userUid);
    }

    final CloudAwaitObject pubSubReqReplyCAO = new CloudAwaitObject("BIND_PATIENT_AND_SUBSCRIBER") {
        @Override
        public Object get(Object obj, Object localMsg, CloudAwaitObject cao) {
            pubSubWorker.sendReplyActionCallback((String) obj);
            return null;
        }
    };


    public static abstract class PubSubWorker {
        abstract public void sendRequestCallback(String msg);

        abstract public void sendReplyActionCallback(String userUid);
    }

    final PubSubWorker pubSubWorker;

    public static abstract class PubSubInfoWorker {
        abstract public void onConnect(Map<String, PubSubReqInfo> infoMap);

        abstract public void newReq(PubSubReqInfo info);

        abstract public void onPatientList(List<String> patientList);
        public void onSubscriberList(List<String> subscriberList){}
        public void doneCallback(List<String> patientList, List<String> subscriberList,List<PubSubReqInfo> reqInfoList){}
        public void doneCallback(){}
    }

    final PubSubInfoWorker pubSubInfoWorker;
}
