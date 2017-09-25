package com.zetta.android.revawebsocketservice;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ME on 2017/09/12.
 */


public class ChannelPublisher{
    private RevaWebsocketEndpoint endpoint;
    protected void setServicePublisher(RevaWebsocketEndpoint endpoint_){
        endpoint = endpoint_;
    }
    private String channelKey;
    ChannelPublisher(String channelKey_){
        channelKey = channelKey_;
    }
    public ChannelPublisher publish(String caoId, String key, Object obj){
        Map<String, Map<String, Object>> map = new TreeMap<>();
        map.put(caoId, new TreeMap<String, Object>());
        map.get(caoId).put(key,obj);
        endpoint.getPushChainer(RevaWebsocketEndpoint.CHANNEL_KEY).put(channelKey, map)
                .send();
        return this;
    }
}