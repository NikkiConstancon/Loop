package com.zetta.android.revawebsocketservice;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by ME on 2017/09/02.
 */
public abstract class RevaWebsocketEndpoint {
    public static final String CHANNEL_KEY = "|^|";
    public static final String META_KEY = "|#|";
    private static final String TAG = "RevaWebsocketEndpoint";

    public abstract String key();


    // ----------------------------- BEGIN OVERRIDEABLE ----------------------------
    //TODO Pass correct values (e.g. code, reason etc)
    public void onClose(String message) {
    }

    public void onOpen(String headerJson) {
    }

    public void onMessage(String message) {
    }

    public void onMessage(LinkedTreeMap obj) {
    }

    public void onServiceConnect(RevaWebSocketService service) {
    }

    public void onServiceDisconnect() {
    }

    public void onMeta(Object msg) {
    }
    // ----------------------------- END OVERRIDEABLE ----------------------------


    //NOTE! may return null;
    public final RevaWebSocketService getService() {
        return mService;
    }

    //NOTE! may return null;
    public final RevaWebSocketService.Publisher getPublisher() {
        return mPublisher;
    }

    public final RevaWebSocketService.Publisher bind(Context context_) {
        context = context_;
        if (mPublisher != null) {
            return mPublisher;
        }
        Intent subscriberIntent = new Intent(context, RevaWebSocketService.class);
        String tmpTest = key();
        subscriberIntent.putExtra(
                RevaWebSocketService.SUBSCRIBER,
                subscriberResultReceiver);
        subscriberIntent.putExtra(
                RevaWebSocketService.SUBSCRIBER_KEY,
                key()
        );

        context.bindService(subscriberIntent, mConnection, Context.BIND_AUTO_CREATE);
        RevaWebSocketService.notifyServiceBind(key(), subscriberResultReceiver);
        return (mPublisher = RevaWebSocketService.localStartService(context, subscriberIntent, key()));
    }

    public final void unbind(Context context) {
        if (mPublisher != null) {
            mPublisher = null;
            context.unbindService(mConnection);
            RevaWebSocketService.notifyServiceUnbind(key(), subscriberResultReceiver);
        }
    }

    private Boolean flagResumeService = null;

    public final void autoService() {
        flagResumeService = null;
        resumeOrPause();
    }

    public final void pauseService() {
        flagResumeService = false;
        resumeOrPause();
    }

    public final void resumeService() {
        flagResumeService = true;
        resumeOrPause();
    }

    public PushChainer getPushChainer() {
        return new PushChainer();
    }

    public PushChainer getPushChainer(String tag) {
        return new PushChainer(tag);
    }


    public class PushChainer {
        Map<String, Object> map = new TreeMap<>();
        Map<String, Object> sendMap;
        String tag;

        public PushChainer put(String token, Object value) {
            map.put(token, value);
            return this;
        }

        public PushChainer send() {
            if (mPublisher == null) {
                Log.e(TAG, "using the endpoint without binding first");
                return null;
            }
            mPublisher.send(sendMap);
            //NOTE! do not just clear map, as this object will be sent asynchronously
            map = new TreeMap<>();
            map.put(tag, new TreeMap<>());
            return this;
        }

        protected PushChainer() {
            sendMap = map;
        }

        protected PushChainer(String tag_) {
            tag = tag_;
            sendMap = new TreeMap<>();
            sendMap.put(tag, map);
        }
    }

    public Context getContext() {
        return context;
    }

    private Context context;
    private static GsonBuilder builder = new GsonBuilder();
    SubscriberResultReceiver subscriberResultReceiver = new SubscriberResultReceiver(null);

    class SubscriberResultReceiver extends ResultReceiver {
        public SubscriberResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String got = resultData.getString(RevaWebSocketService.IPC_SOCKET_MESSAGE_PULLED);
            if (got != null) {
                GsonBuilder builder = new GsonBuilder();
                try {
                    LinkedTreeMap obj = (LinkedTreeMap) builder.create().fromJson(got, Object.class);
                    if (obj.containsKey(META_KEY)) {
                        onMeta(obj.get(META_KEY));
                    } else if (obj.containsKey(CHANNEL_KEY)) {
                        Log.d(TAG, "Got channel: " + obj.toString());
                        for (Object entry : ((LinkedTreeMap) obj.get(CHANNEL_KEY)).entrySet()) {
                            try {
                                Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) entry;
                                CloudAwaitObject cao = cloudAwaitObjectMap.get(pair.getKey());
                                if (cao != null) {
                                    cao.gotFrom(pair.getValue());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "--GOT--" + got);
                            }
                        }
                    } else {
                        onMessage(obj);
                    }
                } catch (Exception e) {
                    onMessage(got);
                }
            }
            got = resultData.getString(RevaWebSocketService.IPC_SOCKET_CLOSED);
            if (got != null) {
                onClose(got);
            }
            got = resultData.getString(RevaWebSocketService.IPC_SOCKET_OPENED);
            if (got != null) {
                resumeOrPause();
                onOpen(got);
            }
        }
    }


    private RevaWebSocketService.Publisher mPublisher;
    private RevaWebSocketService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = ((RevaWebSocketService.LocalBinder) service).getService();
            resumeOrPause();
            onServiceConnect(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            onServiceDisconnect();
        }
    };

    private void resumeOrPause() {
        if (flagResumeService != null && mService != null) {
            if (flagResumeService) {
                mService.rccResumeService(key());
            } else {
                mService.rccPauseService(key());
            }
        }
    }


    protected Map<String, CloudAwaitObject> cloudAwaitObjectMap = new TreeMap<>();

    public CloudAwaitObject attachCloudAwaitObject(CloudAwaitObject cao) {
        cloudAwaitObjectMap.put(cao.getKey(), cao);
        cao.setServicePublisher(this);
        return cao;
    }

    public CloudAwaitObject attachCloudAwaitObject(boolean once, CloudAwaitObject cao) {
        if (once) {
            if (cloudAwaitObjectMap.containsKey(cao.getKey())) {
                return cloudAwaitObjectMap.get(cao.getKey());
            }
        }
        cloudAwaitObjectMap.put(cao.getKey(), cao);
        cao.setServicePublisher(this);
        return cao;
    }


}
