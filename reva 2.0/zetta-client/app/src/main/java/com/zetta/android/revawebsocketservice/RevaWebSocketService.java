package com.zetta.android.revawebsocketservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.R;
import com.zetta.android.browse.MainActivity;
import com.zetta.android.browse.login_activity;
import com.zetta.android.browse.notifications;
import com.zetta.android.lib.Interval;
import com.zetta.android.revaServices.NotificationsService;
import com.zetta.android.revaServices.PubSubBindingService;
import com.zetta.android.revaServices.UserManager;
import com.zetta.android.settings.settingsPage;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ME on 2017/09/02.
 */

public class RevaWebSocketService extends Service {
    // ------------------------------ BEGIN PUBLICS -------------------------------------------
    public USER_TYPE getUserType() {
        return userType;
    }

    public synchronized boolean setLogin(String authId, String password) {
        if (isConnected()) {
            revaWebSocket.close();
        }
        connectInterval.interrupt();
        scoutThread.interrupt();
        if (prefs == null) {
            Log.v(TAG, "#setLogin: prefs is null");
            return false;
        }
        boolean r = commitCredentials(authId, password);
        setForceConnect();
        return r;
    }

    public void signOut() {
        setLogin("", "");
    }

    public void atLoginValidation(final Runnable pass, final Runnable fail, Integer nextTryTimeout, Integer maxTrys) {
        new Interval(nextTryTimeout, maxTrys) {
            boolean passed = false;
            @Override
            public void work() {
                if (isLoggedIn()) {
                    passed = true;
                    clearInterval();
                }
            }

            @Override
            public void end() {
                if (passed) {
                    pass.run();
                } else {
                    fail.run();
                }
            }
        };
    }

    public boolean isLoggedIn() {
        String validity = getPref().getString(SHARED_PREF_KEY_AUTH_VALID, null);
        return validity != null && validity.compareTo("true") == 0;
    }

    public String getAuthId() {
        return getPref().getString(SHARED_PREF_KEY_AUTHID, null);
    }


    public synchronized static Publisher localStartService(Context context, Intent intent, String key) {

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

        private String key;

        private Publisher(String key_) {
            key = key_;
        }
    }

    public void rccPauseService(String key) {
        rccPublishPauseResume(key, false);
    }

    public void rccResumeService(String key) {
        rccPublishPauseResume(key, true);
    }
    // ------------------------------ END PUBLICS -------------------------------------------


    public static final String SPECIAL_USER_ANONYMOUS = "--ANONYMOUS--";
    public static final String SUBSCRIBER = "SUBSCRIBER";
    public static final String SUBSCRIBER_KEY = "SUBSCRIBER_KEY";

    private static final String SHARED_PREF_KEYSPACE = "REVAWEBSOCKETSERVICE";
    private static final String SHARED_PREF_KEY_AUTHID = "SHARED_PREF_KEY_AUTHID";
    private static final String SHARED_PREF_KEY_AUTH_VALID = "SHARED_PREF_KEY_AUTH_VALID";
    private static final String SHARED_PREF_KEY_AUTH_BASIC_HASH_RAW = "AUTH_BASIC_HASH_RAW";

    protected static final String IPC_SOCKET_OPENED = "IPC_SOCKET_OPENED";
    protected static final String IPC_SOCKET_CLOSED = "IPC_SOCKET_CLOSED";
    protected static final String IPC_SOCKET_MESSAGE_PULLED = "pull";

    private static final String RCC_SERVICE_NAME = "RCC";
    private static final String RCC_KEY_PAUSE_RESUME = "PAUSE_RESUME";
    private static final String RCC_KEY_PAUSE_RESUME_KEY_ENABLEMENT = "ENABLEMENT";
    private static final String RCC_KEY_PAUSE_RESUME_KEY_SERVICE_NAME = "SERVICE_KEY";
    private static final String RCC_KEY_SERVICE_BINDING_KEY_STATE = "ENABLEMENT";
    private static final String RCC_KEY_SERVICE_BINDING_KEY_NAME = "SERVICE_KEY";
    private static final String RCC_KEY_SERVICE_BINDING = "SERVICE_BINDING";
    private static final String RCC_KEY_ERROR_DUP_DEVICE_UID = "DUP_DEVICE_UID";

    private final String serviceInstanceUuid = UUID.randomUUID().toString();


    private SharedPreferences prefs = null;


    private static Map<String, List<Object>> publisherMessagePoolMap = new HashMap<>();


    private final static List<String> serviceBoundList = new LinkedList<>();
    private final static Map<String, List<ResultReceiver>> subscriberReceiverMap = new HashMap<>();


    private SharedPreferences getPref() {
        return this.getSharedPreferences(SHARED_PREF_KEYSPACE, Context.MODE_PRIVATE);
    }

    private static void addToMessagePool(String key, Object obj) {
        synchronized (RevaWebSocketService.class) {
            if (!publisherMessagePoolMap.containsKey(key)) {
                publisherMessagePoolMap.put(key, new ArrayList<Object>());
            }
            publisherMessagePoolMap.get(key).add(obj);
            if (scoutThread != null) {
                scoutThread.interrupt();
            }
        }
    }

    private void rccPublishPauseResume(String key, boolean enabled) {
        JsonObject keys = new JsonObject();
        keys.addProperty(RCC_KEY_PAUSE_RESUME_KEY_ENABLEMENT, enabled);
        keys.addProperty(RCC_KEY_PAUSE_RESUME_KEY_SERVICE_NAME, key);
        JsonObject json = new JsonObject();
        json.add(RCC_KEY_PAUSE_RESUME, keys);
        addToMessagePool(RCC_SERVICE_NAME, json);
    }

    private boolean clearAuthBasicToNull() {
        prefs.edit().putString(SHARED_PREF_KEY_AUTHID, null).apply();
        prefs.edit().putString(SHARED_PREF_KEY_AUTH_VALID, "false").commit();
        return prefs.edit().putString(SHARED_PREF_KEY_AUTH_BASIC_HASH_RAW, null).commit();
    }

    private boolean commitCredentials(String authId, String password) {
        String credentials = authId + ":" + password;
        String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        if (getPref().getString(SHARED_PREF_KEY_AUTHID, "").compareTo(authId) != 0) {
            prefs.edit().putString(SHARED_PREF_KEY_AUTH_VALID, "false").commit();
        }
        prefs.edit().putString(SHARED_PREF_KEY_AUTHID, authId).apply();
        return prefs.edit().putString(SHARED_PREF_KEY_AUTH_BASIC_HASH_RAW, basicAuth).commit();
    }

    static Publisher rccPublisher = null;
    RCCEndpoint rccEndpoint = new RCCEndpoint();

    public enum USER_TYPE {PATIENT, SUBSCRIBER}

    ;
    USER_TYPE userType;


    NotificationsService notificationsService = new NotificationsService(
            this,
            new NotificationsService.Worker(){
                @Override  public void onNotification(NotificationsService.Notification note){
                    Log.d("---Notifications---SOCK", note.message);
                }
            }
    );


    PubSubBindingService pubSubBindingService = new PubSubBindingService(this,
            new PubSubBindingService.PubSubWorker() {
                @Override
                public void sendRequestCallback(final String msg) {
                }

                @Override
                public void sendReplyActionCallback(String userUid) {
                }
            },
            new PubSubBindingService.PubSubInfoWorker() {
                @Override
                public void onConnect(Map<String, PubSubBindingService.PubSubReqInfo> infoMap) {
                }

                @Override
                public void newReq(PubSubBindingService.PubSubReqInfo info) {
                    /*
                    Intent dismissIntent = new Intent(RevaWebSocketService.this, settingsPage.class);
                    dismissIntent.setAction(Intent.ACTION_DEFAULT);
                    dismissIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(RevaWebSocketService.this)
                                    .setSmallIcon(R.mipmap.reva_white)
                                    .setContentTitle("ReVA New Request")
                                    .setContentText(info.userUid + " has sent you and invite")
                                    .setDefaults(Notification.DEFAULT_ALL) // must requires VIBRATE permission
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)//must give priority to High, Max which will considered as heads-up notification
                                    .setAutoCancel(true);

                    PendingIntent contentIntent = PendingIntent.getActivity(RevaWebSocketService.this, 0,
                            dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);


                    builder.setContentIntent(contentIntent);
                    NotificationManager notificationManager = (NotificationManager) RevaWebSocketService.this.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0, builder.build());
                    */
                }

                @Override
                public void onPatientList(List<String> patientList) {
                }

                @Override
                public void onSubscriberList(List<String> subscriberList) {
                    Log.d("--onSubscriberList---", subscriberList.toString());
                }
            }
    );
    class RCCEndpoint extends RevaWebsocketEndpoint {
        private final String TAG = this.getClass().getName();

        public void onMessage(LinkedTreeMap obj) {
            try {
                JsonObject jsonObject = gson.toJsonTree(obj).getAsJsonObject();
                JsonElement e = jsonObject.get("ERROR");
                if (e != null) {
                    JsonObject jsonError = e.getAsJsonObject();
                    if (jsonError.has("AUTH")) {
                        //TODO send to UserManager ??
                        clearAuthBasicToNull();
                    }
                    if (jsonError.has(RCC_KEY_ERROR_DUP_DEVICE_UID)) {
                        if (revaWebSocket != null) {
                            revaWebSocket.close();
                        }
                    }
                }
                JsonElement connectedJson = jsonObject.get("CONNECTED");
                if (connectedJson != null) {
                    String userUid = connectedJson.getAsJsonObject().get("USER_UID").getAsString();
                    if (userUid != null) {
                        if (userUid.compareTo(SPECIAL_USER_ANONYMOUS) == 0) {
                            prefs.edit().putString(SHARED_PREF_KEY_AUTH_VALID, "false").commit();
                            commitCredentials(userUid, "");
                        } else {
                            prefs.edit().putString(SHARED_PREF_KEY_AUTH_VALID, "true").commit();
                            String test = connectedJson.getAsJsonObject().get("USER_TYPE").getAsString();
                            if (connectedJson.getAsJsonObject().get("USER_TYPE").getAsString().compareTo("patient") == 0) {
                                userType = USER_TYPE.PATIENT;
                            } else {
                                userType = USER_TYPE.SUBSCRIBER;
                            }
                        }
                    }
                }
                JsonElement redirectEl = jsonObject.get("RCC_REDIRECT");
                if (redirectEl != null) {
                    JsonArray array = redirectEl.getAsJsonArray();
                    List<String> result = new ArrayList<>(array.size());

                    for (int i = 0; i < array.size(); i++) {
                        dispatchMessagePulled(array.get(i).getAsString(), gson.toJson(obj));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error at onMessage with: " + obj.toString());
            }
        }

        @Override
        public String key() {
            return RCC_SERVICE_NAME;
        }
    }

    UserManager userManager = UserManager.instance();

    protected synchronized static void notifyServiceBind(String key, ResultReceiver subscriberResultReceiver) {
        rccPushServiceBinding(key, true);
        if (!serviceBoundList.contains(key)) {
            serviceBoundList.add(key);
        }
        if (subscriberResultReceiver != null) {
            if (key != null) {
                if (!subscriberReceiverMap.containsKey(key)) {
                    subscriberReceiverMap.put(key, new LinkedList<ResultReceiver>());
                }
                if (!subscriberReceiverMap.get(key).contains(subscriberResultReceiver)) {
                    subscriberReceiverMap.get(key).add(subscriberResultReceiver);
                }
            }
        }
    }

    protected synchronized static void notifyServiceUnbind(String key, ResultReceiver subscriberResultReceiver) {
        rccPushServiceBinding(key, false);
        serviceBoundList.remove(key);
        if (subscriberReceiverMap.containsKey(key)) {
            subscriberReceiverMap.get(key).remove(subscriberResultReceiver);
        }
    }

    //call this at socket connect
    private static void rccPublishServiceBindings() {
        synchronized (RevaWebSocketService.class) {
            for (String key : serviceBoundList) {
                rccPushServiceBinding(key, true);
            }
        }
    }

    private static void rccPushServiceBinding(String key, boolean enabled) {
        JsonObject keys = new JsonObject();
        keys.addProperty(RCC_KEY_SERVICE_BINDING_KEY_STATE, enabled);
        keys.addProperty(RCC_KEY_SERVICE_BINDING_KEY_NAME, key);
        JsonObject json = new JsonObject();
        json.add(RCC_KEY_SERVICE_BINDING, keys);
        addToMessagePool(RCC_SERVICE_NAME, json);

    }

    private final IBinder mBinder = new LocalBinder();
    //-------------------------------BEGIN OVERRIDES -------------------------
    static final String TAG = "RevaWebSocketService";

    @Override
    public void onCreate() {
        super.onCreate();
        runSouct = true;

        /*
        Toast.makeText(this, "WebSocketService Started ", Toast.LENGTH_LONG).show();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.test_revawebsocketservice_notification_icon)
                        .setContentTitle("RevaWebSocketService")
                        .setContentText("Created");
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
        */

        initScout();
        rccPublisher = rccEndpoint.bind(RevaWebSocketService.this);
        notificationsService.bind(this);
        pubSubBindingService.bind(this);
    }

    @Override
    public synchronized IBinder onBind(Intent intent) {
        prefs = this.getSharedPreferences(SHARED_PREF_KEYSPACE, Context.MODE_PRIVATE);
        return mBinder;
    }

    @Override
    public synchronized boolean onUnbind(Intent intent) {
        return false;//Return true if you would like to have the service's onRebind(Intent) method later called when new clients bind to it.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        runSouct = false;

        if (revaWebSocket != null) {
            revaWebSocket.close();
            revaWebSocket = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int startId, int flags) {
        /*
        NOTE! this was moved to notifyServiceBind()
        if (intent != null) {
            ResultReceiver subscriberResultReceiver = intent.getParcelableExtra(RevaWebSocketService.SUBSCRIBER);
            if (subscriberResultReceiver != null) {
                String key = intent.getStringExtra(RevaWebSocketService.SUBSCRIBER_KEY);
                if (key != null) {
                    if(!subscriberReceiverMap.containsKey(key)){
                        subscriberReceiverMap.put(key, new LinkedList<ResultReceiver>());
                    }
                    if(!subscriberReceiverMap.get(key).contains(subscriberResultReceiver)){
                        subscriberReceiverMap.get(key).add(subscriberResultReceiver);
                    }
                }
            }
        }
        */
        return START_STICKY;
    }


    //------------------------------- END OVERRIDES -------------------------


    // ----------------------------------- Begin WebSocket -------------------------------------
    private class RevaWebSocket extends WebSocketClient {
        private RevaWebSocket(URI serverUri, Draft draft, Map<String, String> httpHeaders, int timeout) {
            super(serverUri, draft, httpHeaders, timeout);
            Log.d(TAG, "--NEW-- RevaWebSocket");
        }

        @Override
        public void onOpen(final ServerHandshake handshakedata) {
            connectThrashingGuardLastOpen = (new Date()).getTime();
            Log.d(TAG, "--OPENED--");
            if (revaWebSocket != null) {
                revaWebSocket.close();
            }
            revaWebSocket = RevaWebSocket.this;
            flagForceReconnect = false;

            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(256);
                    } catch (Exception e) {
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(IPC_SOCKET_OPENED, gson.toJson(handshakedata));
                    for (Map.Entry<String, List<ResultReceiver>> entry : subscriberReceiverMap.entrySet()) {
                        for (ResultReceiver rr : entry.getValue()) {
                            rr.send(0, bundle);
                        }
                    }
                    rccPublishServiceBindings();
                    scoutThread.interrupt();
                }
            }.start();
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d(TAG, "--CLOSED--  code: " + code + "  info: " + reason);

            Bundle bundle = new Bundle();
            bundle.putString(IPC_SOCKET_CLOSED, "TODO@RevaWebService$RevaWebSocket#onClose");
            for (Map.Entry<String, List<ResultReceiver>> entry : subscriberReceiverMap.entrySet()) {
                for (ResultReceiver rr : entry.getValue()) {
                    rr.send(0, bundle);
                }
            }
            connectSemaphore.release();
        }

        @Override
        public void onMessage(String message) {
            //Log.d(TAG, "--msg--" + message);

            GsonBuilder builder = new GsonBuilder();
            LinkedTreeMap msgObj = (LinkedTreeMap) builder.create().fromJson(message, Object.class);

            try {
                if (msgObj != null) {
                    for (Object entry : msgObj.entrySet()) {
                        Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) entry;


                        List<ResultReceiver> subscriberList = subscriberReceiverMap.get(pair.getKey());
                        if (subscriberList != null) {
                            for (ResultReceiver subscriber : subscriberList)
                                try {
                                    List messageList = (List) pair.getValue();
                                    for (Object o : messageList) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString(IPC_SOCKET_MESSAGE_PULLED, gson.toJson(o));
                                        subscriber.send(0, bundle);
                                    }
                                } catch (ClassCastException e) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString(IPC_SOCKET_MESSAGE_PULLED, gson.toJson(pair.getValue()));
                                    subscriber.send(0, bundle);
                                }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                Log.e(TAG, "--ON--" + message);
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.d(TAG, "--ERROR--" + ex);
        }
    }
    // ----------------------------------- END WebSocket -------------------------------------

    void dispatchMessagePulled(String serviceKey, String msg) {
        Bundle bundle = new Bundle();
        bundle.putString(IPC_SOCKET_MESSAGE_PULLED, msg);
        for (ResultReceiver rr : subscriberReceiverMap.get(serviceKey)) {
            rr.send(0, bundle);
        }
    }

    // ----------------------------------- BEGIN socket Management ------------------------------
    //Got from
    // https://stackoverflow.com/questions/44572162/java-websocket-cannot-resolve-setwebsocketfactory
    //https://github.com/TooTallNate/Java-WebSocket/issues/263
    public WebSocketClient trustAllHosts(WebSocketClient client) {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                return myTrustedAnchors;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
        }};
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory factory = sslContext.getSocketFactory();
            client.setSocket(factory.createSocket());
            client.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }
    Interval connectInterval;
    private synchronized void initScout() {
        //NOTE !! scout now also sends messages in message pool!
        if (!scoutRunning) {
            connectInterval = new Interval(256, Integer.MAX_VALUE) {
                @Override
                public void work() {
                    if (shouldReconnect()) {
                        buildAndConnect();
                    }
                    scoutSleep();
                }
            };
            scoutRunning = true;
            (scoutThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Integer cout = 0;
                    while (runSouct) {
                        if (shouldReconnect()) {
                            try {
                                Thread.sleep(sleepMs);
                            } catch (InterruptedException e) {
                            }
                        }
                        if (flagForceReconnect) {
                            sleepMs = START_SLEEP_MS;
                        }
                        runBroker();
                        scoutSleep();
                    }
                }
            })).start();
        }
    }

    private static Gson gson = new Gson();

    private synchronized void runBroker() {
        synchronized (RevaWebSocketService.class) {
            if (isConnected()) {
                if (publisherMessagePoolMap.size() > 0) {
                    String send = gson.toJson(publisherMessagePoolMap);
                    Log.d(TAG, "#runBroker: sending! " + send);
                    if (getAuthForHeader() != "" && isConnected()) {
                        try {
                            revaWebSocket.send(send);
                            publisherMessagePoolMap.clear();
                        } catch (Exception e) {
                            revaWebSocket = null;
                        }
                    }
                }
            }
        }
    }

    private void buildAndConnect() {
        try {
            try {
                connectSemaphore.acquire();//release on open
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (shouldReconnect()) {
                long delta = Math.abs(connectThrashingGuardLastOpen - (new Date()).getTime());
                delta = (delta / 100) + 1;
                double threshhold = 60000;
                double expRatio = 0.98 * Math.abs((threshhold) / (threshhold + delta * 0.2 * threshhold));
                connectThrashingGuardLastDelay = (long) (expRatio * connectThrashingGuardLastDelay)
                        + (long) ((1 - expRatio) * (threshhold / delta));
                try {
                    if (connectThrashingGuardLastDelay > 1) {
                        Log.d("-----SLEEP----1", ((Long)connectThrashingGuardLastDelay).toString());
                        Thread.sleep(connectThrashingGuardLastDelay);
                    }
                } catch (InterruptedException e) {
                }
                trustAllHosts(buildRevaWebSocket()).connect();
            }
        } catch (Exception e) {
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
            String url = "wss://" + getString(R.string.serverURL) + ":" + getString(R.string.webSocketServerPort);
            Log.i(TAG, "--CONNECTING-TO--" + url);
            return new RevaWebSocket(
                    new URI(url),
                    //new URI("ws://" + getString(R.string.serverURL) + ":8080"),
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
        httpHeaders.put("serviceInstanceUuid", serviceInstanceUuid);
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

    private boolean flagForceReconnect = false;
    private final int START_SLEEP_MS = 1000;
    private final int SCOUT_MAX_SLEEP = 10000;
    private final int tryConnectTimeout = 5000;
    private int sleepMs = START_SLEEP_MS;
    private boolean scoutRunning = false;
    private static Thread scoutThread = null;
    private static RevaWebSocket revaWebSocket = null;
    private boolean runSouct = true;
    private Semaphore connectSemaphore = new Semaphore(1);
    private static long connectThrashingGuardLastOpen = Integer.MAX_VALUE;
    private static long connectThrashingGuardLastDelay = 0;
    // ----------------------------------- END socket Management ------------------------------
}
