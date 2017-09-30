package com.zetta.android.revawebsocketservice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.zetta.android.lib.DelayedUiAction;
import com.zetta.android.lib.Interval;
import com.zetta.android.lib.NotifyCloudAwait;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ME on 2017/09/12.
 */


public abstract class CloudAwaitObject {
    abstract public Object get(Object got, Object localMsg, CloudAwaitObject cao);

    public void dismiss(){
        open = true;
    }

    private static int idCounter = 0;
    protected final String id = Integer.toHexString(idCounter++);


    public static class Chain {
        Context context;
        String key;
        Object obj, localMsg;

        public Chain(Context context, String key, Object obj, Object localMsg) {
            this.context = context;
            this.key = key;
            this.obj = obj;
            this.localMsg = localMsg;
        }
    }


    public CloudAwaitObject(String channelKey_) {
        channelKey = channelKey_;
        publisher = new ChannelPublisher(channelKey);
    }
    public CloudAwaitObject(String channelKey_, boolean silent_) {
        channelKey = channelKey_;
        silent = silent_;
        publisher = new ChannelPublisher(channelKey);
    }

    CloudAwaitObject next = null;

    public CloudAwaitObject then(Object localMsg, CloudAwaitObject cao) {
        endpoint.attachCloudAwaitObject(localMsg, cao);
        cao.publisher.setServicePublisher(endpoint);
        cao.endpoint = endpoint;
        next = cao;
        return cao;
    }

    public CloudAwaitObject then(CloudAwaitObject cao) {
        return then(null, cao);
    }


    public void setLocalMsg(Object obj) {
        localMsg = obj;
    }

    RevaWebsocketEndpoint endpoint;

    protected final void setServicePublisher(RevaWebsocketEndpoint endpoint_) {
        endpoint = endpoint_;
        publisher.setServicePublisher(endpoint);
    }

    public CloudAwaitObject send(final Context context,final String key,final Object obj) {
        if (!awaiting && open) {
            open = false;
            awaiting = true;
            publishInterval = new Interval(RESEND_INTERVAL, MAX_RESEND) {
                NotifyCloudAwait notifyWait;
                @Override public void begin(){
                    if(!silent){
                        notifyWait = new NotifyCloudAwait(context, true,
                                750, " ... connecting to the cloud ... ",
                                Integer.MAX_VALUE//Users should cancel them self
                        );
                    }
                }
                @Override
                public void work() {
                    publisher.publish(id, key, obj);
                }
                @Override
                public void end() {
                    new Interval(1000, 1){
                        @Override
                        public void end() {
                            open = true;
                        }
                    };
                    awaiting = false;
                    if(notifyWait != null) {
                        notifyWait.dismiss();
                    }
                }
            };
        }
        return this;
    }
    boolean open = true;//the endpoint @get must re open the CAO
    Interval publishInterval = null;
    boolean awaiting = false;
    protected final void remoteUpdate(Object remoteMsg) {
        Object ret = get(remoteMsg, localMsg, this);
        if(publishInterval != null) {
            publishInterval.clearInterval();
        }
        if (ret instanceof Chain && next != null) {
            Chain c = (Chain) ret;
            if (c.localMsg != null) {
                next.localMsg = c.localMsg;
            }
            next.send(c.context, c.key, c.obj);
        }
    }

    public String getKey() {
        return channelKey;
    }

    private String channelKey;
    private ChannelPublisher publisher;





    private Object localMsg;


    final static int RESEND_INTERVAL = 3000;
    final static int MAX_RESEND = 4;

    private boolean silent = false;
}