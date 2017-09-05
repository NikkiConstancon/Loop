package com.zetta.android.revawebsocketservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ME on 2017/09/02.
 */
public abstract class RevaWebsocketEndpoint {
    private static final String TAG = "RevaWebsocketEndpoint";
    public abstract String key();


    // ----------------------------- BEGIN OVERRIDEABLE ----------------------------
    //TODO Pass correct values (e.g. code, reason etc)
    public void onClose(String message){}
    public void onOpen(String headerJson){}
    public void onMessage(String message){}
    public void onMessage(LinkedTreeMap obj){}
    public void onServiceConnect(RevaWebSocketService service){}
    public void onServiceDisconnect(){}
    // ----------------------------- END OVERRIDEABLE ----------------------------


    //NOTE! may return null;
    public final RevaWebSocketService getService(){return mService;}
    //NOTE! may return null;
    public final RevaWebSocketService.Publisher getPublisher(){return mPublisher;}
    public final RevaWebSocketService.Publisher bind (Context context) {
        Intent subscriberIntent = new Intent(context, RevaWebSocketService.class);
        subscriberIntent.putExtra(
                RevaWebSocketService.SUBSCRIBER,
                new SubscriberResultReceiver(null));
        subscriberIntent.putExtra(
                RevaWebSocketService.SUBSCRIBER_KEY,
                key()
        );
        context.bindService(subscriberIntent, mConnection, Context.BIND_AUTO_CREATE);
        return (mPublisher = RevaWebSocketService.localStartService(context, subscriberIntent, key()));
    }
    public final void unbind(Context context){
        context.unbindService(mConnection);
    }

    public PushChainer getPushChainer(){return new PushChainer();}
    public PushChainer getPushChainer(String tag){return new PushChainer(tag);}

    public class PushChainer{
        Map<String, Object> map = new TreeMap<>();
        String tag;
        public PushChainer put(String token, String value) {
            map.put(token,value);
            return this;
        }
        public PushChainer send() {
            mPublisher.send(map);
            //NOTE! do not just clear map, as this object will be sent asynchronously
            map = new TreeMap<>();
            map.put(tag,new TreeMap<>());
            return this;
        }
        protected PushChainer(){}
        protected PushChainer(String tag_){
            tag = tag_;
            map.put(tag,new TreeMap<>());
        }
    }

    private static GsonBuilder builder = new GsonBuilder();
    class SubscriberResultReceiver extends ResultReceiver {
        public SubscriberResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String got = resultData.getString(RevaWebSocketService.IPC_SOCKET_MESSAGE_PULLED);
            if(got != null) {
                GsonBuilder builder = new GsonBuilder();
                Object obj = builder.create().fromJson(got, Object.class);
                try{
                    onMessage((LinkedTreeMap)obj);
                }catch (Exception e){
                    onMessage(got);
                }
            }
            got = resultData.getString(RevaWebSocketService.IPC_SOCKET_CLOSED);
            if(got != null) {
                onClose(got);
            }
            got = resultData.getString(RevaWebSocketService.IPC_SOCKET_OPENED);
            if(got != null) {
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
            onServiceConnect(mService);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            onServiceDisconnect();
        }
    };
}
