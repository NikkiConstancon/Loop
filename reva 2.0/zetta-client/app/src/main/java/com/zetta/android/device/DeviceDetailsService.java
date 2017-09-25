package com.zetta.android.device;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.novoda.notils.logger.simple.Log;
import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.settings.SdkProperties;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

class DeviceDetailsService {

    @NonNull private final CompositeSubscription subscriptions = new CompositeSubscription();

    @NonNull private final SdkProperties sdkProperties;
    @NonNull private final DeviceDetailsSdkService sdkService;
    @NonNull private final DeviceDetailsMockService mockService;

    DeviceDetailsService(@NonNull SdkProperties sdkProperties,
                         @NonNull DeviceDetailsSdkService sdkService,
                         @NonNull DeviceDetailsMockService mockService) {
        this.sdkProperties = sdkProperties;
        this.sdkService = sdkService;
        this.mockService = mockService;
    }

    public void getDetails(@NonNull ZettaDeviceId deviceId, @NonNull final Callback callback) {
        Subscription subscription = getDetailsObservable(deviceId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(new Subscriber<DeviceDetails>() {
                @Override
                public void onCompleted() {
                    // not used
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(e, "Something went wrong retrieving device specifics.");
                    callback.onDeviceLoadError();
                }

                @Override
                public void onNext(DeviceDetails deviceDetails) {
                    callback.on(deviceDetails);
                }
            });
        subscriptions.add(subscription);
    }

    @NonNull
    private Observable<DeviceDetails> getDetailsObservable(@NonNull final ZettaDeviceId deviceId) {
        return Observable.create(new Observable.OnSubscribe<DeviceDetails>() {
            @Override
            public void call(Subscriber<? super DeviceDetails> subscriber) {
                subscriber.onNext(getDetails(deviceId));
                subscriber.onCompleted();
            }
        });
    }

    @NonNull
    private DeviceDetails getDetails(@NonNull ZettaDeviceId deviceId) {
        if (sdkProperties.useMockResponses()) {
            SystemClock.sleep(TimeUnit.SECONDS.toMillis(1));
            return mockService.getDetails();
        } else {
            return sdkService.getDeviceDetails(deviceId);
        }
    }

    public void startMonitoringDevice(@NonNull ZettaDeviceId deviceId, @NonNull final DeviceListener listener) {
        Subscription subscription = getDeviceUpdatesObservable(deviceId)
            .throttleLast(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<List<ListItem>>() {
                @Override
                public void onNext(List<ListItem> listItem) {
                    listener.onUpdated(listItem);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(e, "Something went wrong monitoring device specific streams.");
                }

                @Override
                public void onCompleted() {
                    // never completes - hot observable
                }
            });
        subscriptions.add(subscription);
    }

    @NonNull
    private Observable<List<ListItem>> getDeviceUpdatesObservable(@NonNull final ZettaDeviceId deviceId) {
        return Observable.create(new Observable.OnSubscribe<List<ListItem>>() {
            @Override
            public void call(final Subscriber<? super List<ListItem>> subscriber) {
                monitorDevice(deviceId, new DeviceListener() {
                    @Override
                    public void onUpdated(@NonNull List<ListItem> listItems) {
                        subscriber.onNext(listItems);
                    }
                });
            }
        });
    }

    private void monitorDevice(@NonNull ZettaDeviceId deviceId, @NonNull DeviceListener listener) {
        if (sdkProperties.useMockResponses()) {
            mockService.startMonitoringDeviceUpdates(listener);
        } else {
            sdkService.startMonitoringDeviceUpdates(deviceId, listener);
        }
    }

    public void stopMonitoringDevice() {
        subscriptions.clear();
        if (sdkProperties.useMockResponses()) {
            mockService.stopMonitoringDeviceUpdates();
        } else {
            sdkService.stopMonitoringDeviceUpdates();
        }
    }

    public void updateDetails(@NonNull ZettaDeviceId deviceId,
                              @NonNull String action,
                              @NonNull Map<String, Object> labelledInput,
                              @NonNull final DeviceListener listener) {
        if (sdkProperties.useMockResponses()) {
            mockService.updateDetails(deviceId, action, labelledInput, listener);
        } else {
            sdkService.updateDetails(deviceId, action, labelledInput, listener);
        }
    }

    interface Callback {
        void on(@NonNull DeviceDetails deviceDetails);

        void onDeviceLoadError();
    }

    interface DeviceListener {

        void onUpdated(@NonNull List<ListItem> listItems);

    }

}
