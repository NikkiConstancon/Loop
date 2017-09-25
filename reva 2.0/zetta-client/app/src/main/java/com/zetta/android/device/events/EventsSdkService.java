package com.zetta.android.device.events;

import android.support.annotation.NonNull;

import com.apigee.zettakit.ZIKDevice;
import com.apigee.zettakit.ZIKDeviceId;
import com.apigee.zettakit.ZIKLogStreamEntry;
import com.apigee.zettakit.ZIKServer;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaSdkApi;
import com.zetta.android.ZettaStyle;

import java.util.Date;

class EventsSdkService {

    @NonNull private final ZettaSdkApi zettaSdkApi;
    @NonNull private final ZettaStyle.Parser zettaStyleParser;

    public EventsSdkService() {
        zettaSdkApi = ZettaSdkApi.INSTANCE;
        zettaStyleParser = new ZettaStyle.Parser();
    }

    public void startMonitorLogUpdatesFor(@NonNull final ZettaDeviceId deviceId,
                                          @NonNull final EventsService.StreamListener listener) {
        ZIKDeviceId zikDeviceId = new ZIKDeviceId(deviceId.getUuid().toString());
        ZIKServer zikServer = zettaSdkApi.getServerContaining(zikDeviceId);
        ZIKDevice zikDevice = zettaSdkApi.getLiteDevice(zikDeviceId);
        final ZettaStyle style = zettaStyleParser.parseStyle(zikServer, zikDevice);
        zettaSdkApi.startMonitoringLogStreamFor(zikDeviceId, new ZettaSdkApi.ZikStreamListener() {
            @Override
            public void updateFor(@NonNull ZIKServer server, @NonNull ZIKDevice device, @NonNull ZIKLogStreamEntry entry) {
                EventListItem listItem = createEventListItem(style, entry);
                listener.onUpdated(listItem);
            }
        });
    }

    @NonNull
    private EventListItem createEventListItem(@NonNull ZettaStyle style, @NonNull ZIKLogStreamEntry entry) {
        String transition = entry.getTransition();
        String timestamp = new Date(entry.getTimeStamp()).toString();
        return new EventListItem(
            transition,
            timestamp,
            style.getForegroundColor(),
            style.getBackgroundColor()
        );
    }

    public void stopMonitoringStreamedUpdates() {
        zettaSdkApi.stopMonitoringLogStream();
    }
}
