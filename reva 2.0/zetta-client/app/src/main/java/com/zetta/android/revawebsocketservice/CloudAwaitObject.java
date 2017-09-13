package com.zetta.android.revawebsocketservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ME on 2017/09/12.
 */


public abstract class CloudAwaitObject{
    abstract public Object get(Object got, ChannelPublisher pub);

    public CloudAwaitObject(String channelKey_){
        channelKey = channelKey_;
        publisher = new ChannelPublisher(channelKey);
    }
    CloudAwaitObject next = null;
    CloudAwaitObject then(CloudAwaitObject cao, ChannelPublisher pub){
        boolean isFirst = false;
        if(next == null){next = cao; isFirst = true;}
        CloudAwaitObject end = next;
        while(end.next != null){end = end.next;}
        if(isFirst){end.next = cao;}
        return this;
    }
    public void send(Context context, Object obj){
        startDialog(context);
        publisher.publish(obj);
    }
    public void send(Context context, String key, Object obj){
        startDialog(context);
        publisher.publish(key, obj);
    }

    protected final void setServicePublisher(RevaWebsocketEndpoint endpoint){
        publisher.setServicePublisher(endpoint);
    }

    protected final void gotFrom(Object got){
        tying = false;
        if(!canceled) {
            if(connectToCloudWaitDialog != null){
                connectToCloudWaitDialog.dismiss();
            }
            Object ret = get(got, publisher);
            if (ret != null) {
                next.gotFrom(ret);
            }
        }
    }

    public String getKey(){return channelKey;}

    private String channelKey;
    private ChannelPublisher publisher;



    private void startDialog(final Context ct){
        String waitMsg = "...connecting to the cloud...";
        if(!tying) {
            tying = true;
            connectToCloudWaitDialog = new ProgressDialog(ct) {
                @Override
                public void onBackPressed() {
                    canceled = true;
                    connectToCloudWaitDialog.dismiss();
                    tying = false;
                    super.onBackPressed();
                }
            };
        }
        canceled = false;
        connectToCloudWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        connectToCloudWaitDialog.setMessage(waitMsg);
        connectToCloudWaitDialog.setIndeterminate(true);
        connectToCloudWaitDialog.setCanceledOnTouchOutside(false);

        new DelayedAction(ct, 750){
            @Override public void work(){
                connectToCloudWaitDialog.show();
            }
        };
    }

    static abstract class DelayedAction{
        DelayedAction(final Context ct, final int sleep){
            new Thread(){
                @Override public void run() {
                    try {
                        ((Activity)ct).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(sleep);
                                } catch (Exception e) {
                                } finally {
                                    work();
                                }
                            }
                        });
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        public abstract void work();
    }


    ProgressDialog connectToCloudWaitDialog;
    private boolean canceled = false;
    private boolean tying = false;
}