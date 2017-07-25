package com.zetta.android.browse;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.novoda.notils.logger.simple.Log;
import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.settings.SdkProperties;

import java.util.ArrayList;
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

class DeviceListService {

    @NonNull private final CompositeSubscription subscriptions = new CompositeSubscription();

    @NonNull private final SdkProperties sdkProperties;
    @NonNull private final DeviceListSdkService sdkService;
    @NonNull private final DeviceListMockService mockService;

    DeviceListService(@NonNull SdkProperties sdkProperties,
                      @NonNull DeviceListSdkService sdkService,
                      @NonNull DeviceListMockService mockService) {
        this.sdkProperties = sdkProperties;
        this.sdkService = sdkService;
        this.mockService = mockService;
    }

    public boolean hasRootUrl() {
        return sdkProperties.hasUrl() || sdkProperties.useMockResponses();
    }

    @NonNull
    public String getRootUrl() {
        return sdkProperties.getUrl();
    }

    public void getDeviceList(@NonNull final Callback callback) {
        Subscription subscription = getDeviceListObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(new Subscriber<List<ListItem>>() {
                @Override
                public void onCompleted() {
                    // not used
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(e, "Something went wrong retrieving list of devices.");
                }

                @Override
                public void onNext(List<ListItem> listItems) {
                    callback.on(listItems);
                }
            });
        subscriptions.add(subscription);
    }

    @NonNull
    private Observable<List<ListItem>> getDeviceListObservable() {
        return Observable.create(new Observable.OnSubscribe<List<ListItem>>() {
            @Override
            public void call(Subscriber<? super List<ListItem>> subscriber) {
                subscriber.onNext(getDeviceListItems());
                subscriber.onCompleted();
            }
        });
    }

    @NonNull
    private List<ListItem> getDeviceListItems() {
        String url = getRootUrl();
        List<ListItem> items = new ArrayList<>();
        if (sdkProperties.useMockResponses()) {
            SystemClock.sleep(TimeUnit.SECONDS.toMillis(3));
            items.addAll(mockService.getListItems(url));
        } else {
            items.addAll(sdkService.getListItems(url));
        }
        return items;
    }

    public void getQuickActions(@NonNull final ZettaDeviceId deviceId, @NonNull final Callback callback) {
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<ListItem>>() {
            @Override
            public void call(Subscriber<? super List<ListItem>> subscriber) {
                List<ListItem> items = getQuickActions(deviceId);
                subscriber.onNext(items);
                subscriber.onCompleted();
            }
        })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(new Subscriber<List<ListItem>>() {
                @Override
                public void onCompleted() {
                    // not used
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(e, "Something went wrong retrieving quick actions.");
                }

                @Override
                public void onNext(List<ListItem> listItems) {
                    callback.on(listItems);
                }
            });
        subscriptions.add(subscription);
    }

    @NonNull
    private List<ListItem> getQuickActions(@NonNull ZettaDeviceId deviceId) {
        if (sdkProperties.useMockResponses()) {
            SystemClock.sleep(TimeUnit.SECONDS.toMillis(1));
            return mockService.getQuickActions(deviceId);
        } else {
            return sdkService.getQuickActions(deviceId);
        }
    }

    interface Callback {
        void on(@NonNull List<ListItem> listItems);
    }

    public void startMonitoringAllDeviceUpdates(@NonNull final DevicesUpdateListener listener) {
        Subscription subscription = getDeviceUpdatesObservable()
            .buffer(700, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<List<ListItem>>() {
                @Override
                public void onNext(List<ListItem> listItems) {
                    listener.onUpdated(listItems);
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(e, "Something went wrong monitoring device list streams.");
                }

                @Override
                public void onCompleted() {
                    // not used
                }
            });
        subscriptions.add(subscription);
    }

    @NonNull
    private Observable<ListItem> getDeviceUpdatesObservable() {
        return Observable.create(new Observable.OnSubscribe<ListItem>() {
            @Override
            public void call(final Subscriber<? super ListItem> subscriber) {
                monitorDeviceUpdates(new DeviceListItemListener() {
                    @Override
                    public void onUpdated(@NonNull ListItem listItem) {
                        subscriber.onNext(listItem);
                    }
                });
            }
        });
    }

    private void monitorDeviceUpdates(@NonNull DeviceListItemListener listener) {
        String url = sdkProperties.getUrl();
        if (sdkProperties.useMockResponses()) {
            mockService.startMonitorDeviceUpdates(url, listener);
        } else {
            sdkService.startMonitorDeviceUpdates(url, listener);
        }
    }

    public void stopMonitoringStreamedUpdates() {
        subscriptions.clear();
        if (sdkProperties.useMockResponses()) {
            mockService.stopMonitoringStreamedUpdates();
        } else {
            sdkService.stopMonitoringStreamedUpdates();
        }
    }

    public void updateDetails(@NonNull ZettaDeviceId deviceId,
                              @NonNull String action,
                              @NonNull Map<String, Object> labelledInput) {
        if (sdkProperties.useMockResponses()) {
            mockService.updateDetails(deviceId, action, labelledInput);
        } else {
            sdkService.updateDetails(deviceId, action, labelledInput);
        }
    }

    interface DevicesUpdateListener {

        void onUpdated(@NonNull List<ListItem> listItems);

    }

    interface DeviceListItemListener {

        void onUpdated(@NonNull ListItem listItem);

    }
}
