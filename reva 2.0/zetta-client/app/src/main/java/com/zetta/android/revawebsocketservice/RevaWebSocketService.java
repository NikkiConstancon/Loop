package com.zetta.android.revawebsocketservice;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ME on 2017/09/02.
 */

public class RevaWebSocketService extends Service {
    private final String serviceInstanceUuid = UUID.randomUUID().toString();
    private int seviceBoundCount = 0;
    // ------------------------------ BEGIN PUBLICS -------------------------------------------
    public static final String SERVICE_USER_MANAGER_NAME = "UserManager";
    public synchronized boolean setLogin(String authId, String password) {
        if (isConnected()) {
            revaWebSocket.close();
        }
        if (prefs == null) {
            Log.v(TAG, "#setLogin: prefs is null");
            return false;
        }
        String credentials = authId + ":" + password;
        String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        prefs.edit().putString(SHARED_PREF_KEY_AUTHID, authId).apply();
        boolean r = prefs.edit().putString(SHARED_PREF_KEY_AUTH_BASIC_HASH_RAW, basicAuth).commit();
        setForceConnect();
        return r;
    }

    public String getAuthId() {
        return getPref().getString(SHARED_PREF_KEY_AUTHID, null);
    }


    public synchronized static Publisher localStartService(Context context, Intent intent, String key) {
        if (!flagServiceStarted) {
            flagServiceStarted = true;
            context.startService(new Intent(context, RevaWebSocketService.class));
        }
        ResultReceiver subscriber = intent.getParcelableExtra(SUBSCRIBER);
        if (subscriber != null) {
            Bundle bundle = new Bundle();
            bundle.putString("start", "The start msg");
            subscriber.send(0, bundle);


            subscriberReceiverMap.put(key, subscriber);
        } else {
            Log.e(TAG, "public static Publisher localStartService(Context context, Intent intent, String key): subscriber is null");
        }

        context.startService(intent);
        return new Publisher(key);
    }

    public class LocalBinder extends Binder {
        RevaWebSocketService getService() {
            return RevaWebSocketService.this;
        }
    }

    public static class Publisher {
        public synchronized void send(Object obj) {
            addToMessagePool(key, obj);
        }

        public synchronized void send(String key, Object obj) {
            Map tmp = new TreeMap<String, Object>();
            tmp.put(key, obj);
            send(tmp);
        }

        private String key;

        private Publisher(String key_) {
            key = key_;
        }
    }
    // ------------------------------ END PUBLICS -------------------------------------------


    public final static String SUBSCRIBER = "SUBSCRIBER";
    public final static String SUBSCRIBER_KEY = "SUBSCRIBER_KEY";

    private static final String SHARED_PREF_KEYSPACE = "REVAWEBSOCKETSERVICE";
    private static final String SHARED_PREF_KEY_AUTHID = "SHARED_PREF_KEY_AUTHID";
    private static final String SHARED_PREF_KEY_AUTH_BASIC_HASH_RAW = "AUTH_BASIC_HASH_RAW";

    protected static final String IPC_SOCKET_OPENED = "IPC_SOCKET_OPENED";
    protected static final String IPC_SOCKET_CLOSED = "IPC_SOCKET_CLOSED";
    protected static final String IPC_SOCKET_MESSAGE_PULLED = "pull";

    private SharedPreferences prefs = null;

    private static boolean flagServiceStarted = false;


    private static Map<String, List<Object>> publisherMessagePoolMap = new HashMap<>();


    private final static Map<String, ResultReceiver> subscriberReceiverMap = new HashMap<>();


    private static class PublisherResultReceiver extends ResultReceiver {
        String key;

        private PublisherResultReceiver(String key_) {
            super(null);
            this.key = key_;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            //do websocket stuff
        }
    }

    private SharedPreferences getPref() {
        return this.getSharedPreferences(SHARED_PREF_KEYSPACE, Context.MODE_PRIVATE);
    }
    private static void addToMessagePool(String key, Object obj){
        if (!publisherMessagePoolMap.containsKey(key)) {
            publisherMessagePoolMap.put(key, new ArrayList<Object>());
        }
        publisherMessagePoolMap.get(key).add(obj);
        if (scoutThread != null) {
            scoutThread.interrupt();
        }
    }

    private final IBinder mBinder = new LocalBinder();
    //-------------------------------BEGIN OVERRIDES -------------------------
    static final String TAG = "RevaWebSocketService";

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "WebSocketService Started ", Toast.LENGTH_LONG).show();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.test_revawebsocketservice_notification_icon)
                        .setContentTitle("RevaWebSocketService")
                        .setContentText("Created");
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());

        initScout();
    }

    @Override
    public synchronized IBinder onBind(Intent intent) {
        prefs = this.getSharedPreferences(SHARED_PREF_KEYSPACE, Context.MODE_PRIVATE);
        seviceBoundCount++;
        addToMessagePool(SERVICE_USER_MANAGER_NAME, "IPC_SEVICE_BOUND_COUNT!=0");
        return mBinder;
    }
    @Override
    public synchronized boolean onUnbind(Intent intent){
        seviceBoundCount--;
        if(seviceBoundCount == 0) {
            addToMessagePool(SERVICE_USER_MANAGER_NAME, "IPC_SEVICE_BOUND_COUNT=0");
        }
        return false;//Return true if you would like to have the service's onRebind(Intent) method later called when new clients bind to it.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if(revaWebSocket != null){
            revaWebSocket.close();
            revaWebSocket = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int startId, int flags) {
        if (intent != null) {
            ResultReceiver subscriberResultReceiver = intent.getParcelableExtra(RevaWebSocketService.SUBSCRIBER);
            if (subscriberResultReceiver != null) {
                String key = intent.getStringExtra(RevaWebSocketService.SUBSCRIBER_KEY);
                if (key != null) {
                    subscriberReceiverMap.put(key, subscriberResultReceiver);
                }
            }
        }
        return START_STICKY;
    }


    //------------------------------- END OVERRIDES -------------------------


    // ----------------------------------- Begin WebSocket -------------------------------------
    private class RevaWebSocket extends WebSocketClient {
        private RevaWebSocket(URI serverUri, Draft draft, Map<String, String> httpHeaders, int timeout) {
            super(serverUri, draft, httpHeaders, timeout);
            Log.d(TAG, "--NEW-- RevaWebSocket");
        }

        private RevaWebSocket(URI serverURI) {
            super(serverURI);
            Log.d(TAG, "--NEW-- RevaWebSocket");
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d(TAG, "--OPENED--");
            if (flagSocketConnected) {
                revaWebSocket.close();
            }
            flagSocketConnected = true;

            connectionSemaphore.release();
            flagForceReconnect = false;

            Bundle bundle = new Bundle();
            bundle.putString(IPC_SOCKET_OPENED, gson.toJson(handshakedata));
            for (Map.Entry<String, ResultReceiver> entry : subscriberReceiverMap.entrySet()) {
                entry.getValue().send(0, bundle);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            connectionSemaphore.release();
            flagSocketConnected = false;
            Log.d(TAG, "--CLOSED--  code: " + code + "  info: " + reason);

            Bundle bundle = new Bundle();
            bundle.putString(IPC_SOCKET_CLOSED, "TODO@RevaWebService$RevaWebSocket#onClose");
            for (Map.Entry<String, ResultReceiver> entry : subscriberReceiverMap.entrySet()) {
                entry.getValue().send(0, bundle);
            }
        }

        @Override
        public void onMessage(String message) {
            Log.d(TAG, "--msg--" + message);

            GsonBuilder builder = new GsonBuilder();
            LinkedTreeMap msgObj = (LinkedTreeMap) builder.create().fromJson(message, Object.class);

            try {
                if (msgObj != null) {
                    for (Object entry : msgObj.entrySet()) {
                        Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) entry;


                        ResultReceiver subscriber = subscriberReceiverMap.get(pair.getKey());
                        if (subscriber != null) {
                            List messageList = (List)pair.getValue();
                            for(Object o : messageList) {
                                Bundle bundle = new Bundle();
                                bundle.putString(IPC_SOCKET_MESSAGE_PULLED, o.toString());
                                subscriber.send(0, bundle);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onError(Exception ex) {
            connectionSemaphore.release();
            Log.d(TAG, "--ERROR--" + ex);
        }
    }
    // ----------------------------------- END WebSocket -------------------------------------

    // ----------------------------------- BEGIN socket Management ------------------------------
    //Got from
    // https://stackoverflow.com/questions/44572162/java-websocket-cannot-resolve-setwebsocketfactory
    //https://github.com/TooTallNate/Java-WebSocket/issues/263
    public void trustAllHosts(WebSocketClient client) {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                return myTrustedAnchors;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {}
        } };
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            client.setSocket(factory.createSocket());
            client.connectBlocking();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private synchronized void initScout() {
        //NOTE !! scout now also sends messages in message pool!
        if (!scoutRunning) {
            scoutRunning = true;
            (scoutThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (shouldReconnect()) {
                            buildAndConnect();
                        }
                        if (flagForceReconnect) {
                            sleepMs = START_SLEEP_MS;
                        }
                        runBroker();
                        scoutSleep();
                    }
                }
            })).start();
        } /*else {
            revaWebSocket.send("{pulse:0}");
        }*/
    }

    private static Gson gson = new Gson();

    private void runBroker() {
        if (isConnected()) {
            //TODO syncronyze pushhing (i.e. send) to the pool (currenty there is a chance that map gets cleared while other thread is pusshing too pool)
            if (publisherMessagePoolMap.size() > 0) {
                String send = gson.toJson(publisherMessagePoolMap);
                Log.d(TAG, "#runBroker: sending! " + send);
                if (getAuthForHeader() != "" && isConnected()) {
                    revaWebSocket.send(send);
                    publisherMessagePoolMap.clear();
                }
            }
        }
    }

    private void buildAndConnect() {
        try {
            connectionSemaphore.acquire();
            if (isConnected()) {
                revaWebSocket.close();
            }
            boolean wasNull = revaWebSocket == null;
            revaWebSocket = buildRevaWebSocket();
            if(wasNull){
                //trustAllHosts(revaWebSocket);//for now care for MAN IN THE MIDDLE
            }
            revaWebSocket.connect();

        } catch (InterruptedException e) {
        }
    }

    private void scoutSleep() {
        try {
            Thread.sleep(sleepMs);
            sleepMs = (sleepMs > SCOUT_MAX_SLEEP ? sleepMs : (int) (sleepMs * 1.5));
        } catch (InterruptedException e) {
        }
    }

    private boolean shouldReconnect() {
        if (revaWebSocket == null) {
            return true;
        }
        return flagForceReconnect || !isConnected();
    }

    private RevaWebSocket buildRevaWebSocket() {
        try {
            return new RevaWebSocket(
                    new URI("ws://" + getString(R.string.serverURL) + ":8080"),
                    new Draft_6455(),
                    genHeader(),
                    tryConnectTimeout
            );
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> genHeader() {
        Map<String, String> httpHeaders = new TreeMap<>();
        httpHeaders.put("Authorization", getAuthForHeader());
        httpHeaders.put("serviceInstanceUuid",  serviceInstanceUuid);
        return httpHeaders;
    }

    public String getAuthForHeader() {
        return getPref().getString(SHARED_PREF_KEY_AUTH_BASIC_HASH_RAW, null);
    }

    private void setForceConnect() {
        flagForceReconnect = true;
        scoutThread.interrupt();
    }

    public synchronized static boolean isConnected() {
        if (revaWebSocket == null) {
            return false;
        }
        return revaWebSocket.isOpen();
    }

    private boolean flagSocketConnected = false;//use this to ensure only one open socket
    private boolean flagForceReconnect = false;
    private final int START_SLEEP_MS = 1000;
    private final int SCOUT_MAX_SLEEP = 10000;
    private final int tryConnectTimeout = 4000;
    private int sleepMs = START_SLEEP_MS;
    private boolean scoutRunning = false;
    private static Semaphore connectionSemaphore = new Semaphore(4);
    private static Thread scoutThread = null;
    private static RevaWebSocket revaWebSocket = null;
    // ----------------------------------- END socket Management ------------------------------
}
