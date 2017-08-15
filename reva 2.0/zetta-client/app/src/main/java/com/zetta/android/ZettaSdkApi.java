package com.zetta.android;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.apigee.zettakit.ZIKDevice;
import com.apigee.zettakit.ZIKDeviceId;
import com.apigee.zettakit.ZIKException;
import com.apigee.zettakit.ZIKLogStreamEntry;
import com.apigee.zettakit.ZIKRoot;
import com.apigee.zettakit.ZIKServer;
import com.apigee.zettakit.ZIKSession;
import com.apigee.zettakit.ZIKStream;
import com.apigee.zettakit.ZIKStreamEntry;
import com.apigee.zettakit.interfaces.ZIKCallback;
import com.apigee.zettakit.interfaces.ZIKStreamListener;
import com.novoda.notils.exception.DeveloperError;
import com.novoda.notils.logger.simple.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * This singleton is not _fully_ tested as thread safe
 */
public enum ZettaSdkApi {
    INSTANCE;

    @NonNull private final List<ZIKStream> openStreams = Collections.synchronizedList(new ArrayList<ZIKStream>());
    @NonNull private final CompositeSubscription subscriptions = new CompositeSubscription();

    @Nullable private String rootUrl;
    @Nullable private ZIKRoot zikRoot;
    @Nullable private List<ZIKServer> zikServers;
    @Nullable private List<ZIKDevice> zikDevices;
    @Nullable private ZIKStream logStream;

    public void registerRoot(@NonNull String url) {
        if (url.equals(this.rootUrl)) {
            // No need to re-register
            return;
        }
        this.rootUrl = url;
        getRoot();
    }

    @NonNull
    public ZIKRoot getRoot() {
        if (rootUrl == null) {
            throw new DeveloperError("Call registerRoot(url) before getRoot!");
        }
        final ZIKSession session = ZIKSession.getSharedSession();
        this.zikRoot = session.getRootSync(rootUrl);
        return zikRoot;
    }

    @NonNull
    public List<ZIKServer> getServers() {
        if (zikRoot == null || !zikRoot.getSelfLink().getHref().equals(rootUrl)) {
            getRoot();
        }
        ZIKSession session = ZIKSession.getSharedSession();
        List<ZIKServer> zikServers = session.getServersSync(zikRoot);
        filterHiddenDevices(zikServers);
        this.zikServers = zikServers;
        return this.zikServers;
    }

    private void filterHiddenDevices(List<ZIKServer> zikServers) {
        for (ZIKServer zikServer : zikServers) {
            filterHiddenDevices(zikServer, zikServer.getDevices());
        }
    }

    private void filterHiddenDevices(ZIKServer server, List<ZIKDevice> zikDevices) {
        if (server.getProperties().containsKey("style")) {
            Map entities = (Map) ((Map) server.getProperties().get("style")).get("entities");
            for (Object entityType : entities.keySet()) {
                Map deviceType = (Map) entities.get(entityType);
                if (deviceType.containsKey("display") && deviceType.get("display").equals("none")) {
                    for (Iterator<ZIKDevice> iterator = zikDevices.iterator(); iterator.hasNext(); ) {
                        ZIKDevice zikDevice = iterator.next();
                        if (zikDevice.getType().equals(entityType)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    @NonNull
    public List<ZIKDevice> getDevices(@NonNull ZIKServer zikServer) {
        if (zikServers == null) {
            throw new DeveloperError("Ensure getServers is called and completes successfully first!");
        }
        ZIKSession session = ZIKSession.getSharedSession();
        this.zikDevices = session.getDevicesSync(zikServer);
        return zikDevices;
    }

    @NonNull
    public ZIKDevice getLiteDevice(@NonNull ZIKDeviceId zikDeviceId) {
        if (zikDevices == null) {
            getDevices(getServerContaining(zikDeviceId));
        }
        for (ZIKDevice device : zikDevices) {
            if (zikDeviceId.equals(device.getDeviceId())) {
                return device;
            }
        }
        getDevices(getServerContaining(zikDeviceId));
        for (ZIKDevice device : zikDevices) {
            if (zikDeviceId.equals(device.getDeviceId())) {
                return device;
            }
        }
        throw new DeveloperError("A device should always be found, what did you do?");
    }

    @NonNull
    public ZIKDevice getFullDevice(@NonNull ZIKDeviceId zikDeviceId) {
        return getLiteDevice(zikDeviceId).fetchSync();
    }

    @NonNull
    public ZIKServer getServerContaining(@NonNull ZIKDeviceId zikDeviceId) {
        if (zikServers == null) {
            throw new DeveloperError("Ensure getServers is called and completes successfully first!");
        }
        for (ZIKServer zikServer : zikServers) {
            for (ZIKDevice device : zikServer.getDevices()) {
                if (zikDeviceId.equals(device.getDeviceId())) {
                    return zikServer;
                }
            }
        }
        throw new DeveloperError("A server should always be found, what did you do?");
    }

    public void startMonitoringDevice(@NonNull ZIKDeviceId deviceId, @NonNull ZikDeviceListener listener) {
        stopMonitoringDevice();
        ZIKDevice device = getLiteDevice(deviceId);
        monitorDeviceStreams(getServerContaining(deviceId), device, listener);
    }

    public void stopMonitoringDevice() {
        stopMonitoringOpenStreams();
    }

    public void startMonitoringAllServerAllDevices(@NonNull final ZikDeviceListener listener) {
        if (zikServers == null) {
            getServers();
        }
        stopMonitoringOpenStreams();
        for (ZIKServer zikServer : zikServers) {
            List<ZIKDevice> zikDevices = zikServer.getDevices();
            for (ZIKDevice liteZikDevice : zikDevices) {
                final ZIKDevice zikDevice = liteZikDevice.fetchSync();
                monitorDeviceStreams(zikServer, zikDevice, listener);
            }
        }
    }

    private void monitorDeviceStreams(@NonNull ZIKServer zikServer,
                                      @NonNull ZIKDevice zikDevice,
                                      @NonNull ZikDeviceListener listener) {
        for (final ZIKStream stream : zikDevice.getAllStreams()) {
            if (stream.getTitle().equals("logs")) {
                continue;
            }
            Subscription subscription = monitorDeviceStream(stream, zikServer, zikDevice, listener);
            subscriptions.add(subscription);
            openStreams.add(stream);
        }
    }

    private static Subscription monitorDeviceStream(@NonNull final ZIKStream stream,
                                                    @NonNull final ZIKServer zikServer, @NonNull final ZIKDevice zikDevice,
                                                    @NonNull final ZikDeviceListener listener) {
        return Observable.create(new Observable.OnSubscribe<ZIKStreamEntry>() {
            @Override
            public void call(final Subscriber<? super ZIKStreamEntry> subscriber) {
                stream.setStreamListener(new ZIKStreamListener() {
                    @Override
                    public void onUpdate(Object object) {
                        ZIKStreamEntry entry = (ZIKStreamEntry) object;
                        subscriber.onNext(entry);
                    }

                    @Override
                    public void onError(ZIKException exception, Response response) {
                        subscriber.onError(exception);
                    }

                    @Override
                    public void onClose() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onOpen() {
                        // not used
                    }

                    @Override
                    public void onPong() {
                        // not used
                    }
                });
                stream.resume();
            }
        })
            .throttleLast(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(new Action1<ZIKStreamEntry>() {
                @Override
                public void call(ZIKStreamEntry entry) {
//                    ZIKDevice updatedDevice = zikDevice.refreshWithLogEntry(entry);
                    ZIKDevice updatedDevice = zikDevice.fetchSync();
                    listener.updateFor(zikServer, updatedDevice);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Log.e(throwable, "Error whilst monitoring.");
                }
            });
    }

    public void stopMonitoringOpenStreams() {
        synchronized (openStreams) {
            for (ZIKStream stream : openStreams) {
                stream.close();
            }
            openStreams.clear();
            subscriptions.clear();
        }
    }

    public void startMonitoringLogStreamFor(@NonNull ZIKDeviceId deviceId, @NonNull ZikStreamListener listener) {
        ZIKDevice device = getLiteDevice(deviceId);
        monitorLogStream(device, listener);
    }

    private void monitorLogStream(@NonNull ZIKDevice device, @NonNull ZikStreamListener listener) {
        logStream = device.stream("logs");
        if (logStream == null) {
            Log.e("Stream not found, monitoring nothing.");
            return;
        }
        monitorLog(logStream, device, listener);
    }

    private void monitorLog(@NonNull ZIKStream stream,
                            @NonNull final ZIKDevice device,
                            @NonNull final ZikStreamListener listener) {
        ZIKDeviceId deviceId = device.getDeviceId();
        final ZIKServer server = getServerContaining(deviceId);
        stream.setStreamListener(new ZIKStreamListener() {
            @Override
            public void onOpen() {
                // not used
            }

            @Override
            public void onUpdate(Object object) {
                ZIKLogStreamEntry streamEntry = (ZIKLogStreamEntry) object;
                listener.updateFor(server, device.refreshWithLogEntry(streamEntry), streamEntry);
            }

            @Override
            public void onPong() {
                // not used
            }

            @Override
            public void onClose() {
                // not used
            }

            @Override
            public void onError(ZIKException exception, Response response) {
                Log.e(exception, "Error streaming " + response);
            }
        });
        stream.resume();
    }

    public void stopMonitoringLogStream() {
        if (logStream == null) {
            return;
        }
        logStream.close();
    }

    public void update(@NonNull final ZIKDeviceId deviceId,
                       @NonNull final String action,
                       @NonNull final Map<String, Object> commands) {
        update(deviceId, action, commands, null);
    }

    public void update(@NonNull final ZIKDeviceId deviceId,
                       @NonNull final String action,
                       @NonNull final Map<String, Object> commands,
                       @Nullable final ZIKCallback<ZIKDevice> callback) {
        Observable.create(new Observable.OnSubscribe<ZIKDevice>() {
            @Override
            public void call(final Subscriber<? super ZIKDevice> subscriber) {
                getFullDevice(deviceId).transition(action, commands, new ZIKCallback<ZIKDevice>() {
                    @Override
                    public void onSuccess(@NonNull ZIKDevice result) {
                        subscriber.onNext(result);
                    }

                    @Override
                    public void onFailure(@NonNull ZIKException exception) {
                        subscriber.onError(exception);
                    }
                });
            }
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<ZIKDevice>() {
                @Override
                public void call(ZIKDevice device) {
                    if (callback == null) {
                        return;
                    }
                    callback.onSuccess(device);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    if (callback == null) {
                        return;
                    }
                    callback.onFailure((ZIKException) throwable);
                }
            });
    }

    public interface ZikStreamListener {
        void updateFor(@NonNull ZIKServer server, @NonNull ZIKDevice device, @NonNull ZIKLogStreamEntry entry);
    }

    public interface ZikDeviceListener {
        void updateFor(@NonNull ZIKServer server, @NonNull ZIKDevice device);
    }
}
