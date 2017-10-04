package com.zetta.android.revaServices;

import android.app.Activity;

import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.revawebsocketservice.CloudAwaitObject;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

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
            Activity activity_,
            PubSubWorker pubSubWorker_,
            PubSubInfoWorker pubSubInfoWorker_
    ) {
        activity = activity_;
        pubSubWorker = pubSubWorker_;
        pubSubInfoWorker = pubSubInfoWorker_;
    }


    public void pubSubBindingRequest(String target) {
        attachCloudAwaitObject(null, pubSubReqCAO).send(activity, "REQ_BIND", target);
    }

    static public class pubSubReqInfo {
        public enum TYPE {REQUESTER, TARGET}

        ;

        public enum STATE {PENDING, DELIVERED, ACCEPTED}

        ;

        public enum REPLY {ACCEPT, DECLINE}

        ;
        public final String userUid;
        public final TYPE type;
        public final STATE state;

        public pubSubReqInfo(
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
    Map<String, pubSubReqInfo> pubSubInfoMap = new TreeMap<>();

    public ArrayList<String> getPubSubList() {
        return new ArrayList<>(pubSubInfoMap.keySet());
    }

    pubSubReqInfo getPubSubInfo(int i) {
        ArrayList names = getPubSubList();
        return pubSubInfoMap.get(names.get(i));
    }

    final Activity activity;

    @Override
    public void onMessage(final LinkedTreeMap obj) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Map<String, Object> gotMapKeys = obj;
                for (Map.Entry<String, Object> entry : gotMapKeys.entrySet()) {
                    switch (entry.getKey()) {
                        case "BINDING_CONFIRMATION_REQ": {
                            Map<String, Map<String, String>> gotMap = (Map<String, Map<String, String>>) entry.getValue();
                            for (Map.Entry<String, Map<String, String>> entryInfo : gotMap.entrySet()) {
                                Map<String, String> info = entryInfo.getValue();
                                pubSubInfoMap.put(entryInfo.getKey(), new pubSubReqInfo(entryInfo.getKey(), info.get("type"), info.get("state")));
                            }
                            pubSubInfoWorker.onConnect(pubSubInfoMap);
                        }
                        break;
                        case "NEW_BINDING_CONFIRMATION_REQ": {
                            Map<String, String> entryInfo = (Map<String, String>) entry.getValue();
                            pubSubReqInfo info =
                                    new pubSubReqInfo(
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
                        break;
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
                    pubSubWorker.sendRequestCallback((String) obj);
                }
            });
            return null;
        }
    };


    public void pubSubRequestReply(String userUid, pubSubReqInfo.REPLY reply) {
        attachCloudAwaitObject(null, pubSubReqReplyCAO).send(activity, reply.toString(), userUid);
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
        abstract public void onConnect(Map<String, pubSubReqInfo> infoMap);

        abstract public void newReq(pubSubReqInfo info);

        abstract public void onPatientList(List<String> patientList);
    }

    final PubSubInfoWorker pubSubInfoWorker;
}
