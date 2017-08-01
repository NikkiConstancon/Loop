package com.zetta.android.device;

import android.support.annotation.NonNull;

import com.apigee.zettakit.ZIKDevice;
import com.apigee.zettakit.ZIKDeviceId;
import com.apigee.zettakit.ZIKException;
import com.apigee.zettakit.ZIKServer;
import com.apigee.zettakit.interfaces.ZIKCallback;
import com.novoda.notils.logger.simple.Log;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaSdkApi;
import com.zetta.android.ZettaStyle;
import com.zetta.android.device.actions.ActionListItemParser;

import java.util.Map;

class DeviceDetailsSdkService {

    @NonNull private final ZettaSdkApi zettaSdkApi;
    @NonNull private final DeviceDetails.Parser deviceParser;

    public DeviceDetailsSdkService() {
        zettaSdkApi = ZettaSdkApi.INSTANCE;
        ZettaStyle.Parser zettaStyleParser = new ZettaStyle.Parser();
        ActionListItemParser actionListItemParser = new ActionListItemParser();
        deviceParser = new DeviceDetails.Parser(zettaStyleParser, actionListItemParser);
    }

    @NonNull
    public DeviceDetails getDeviceDetails(@NonNull ZettaDeviceId deviceId) {
        ZIKDeviceId zikDeviceId = new ZIKDeviceId(deviceId.getUuid().toString());
        ZIKServer zikServer = zettaSdkApi.getServerContaining(zikDeviceId);
        ZIKDevice zikDevice = zettaSdkApi.getLiteDevice(zikDeviceId);
        return deviceParser.convertToDevice(zikServer, zikDevice);
    }

    public void startMonitoringDeviceUpdates(@NonNull final ZettaDeviceId deviceId,
                                             @NonNull final DeviceDetailsService.DeviceListener listener) {
        ZIKDeviceId zikDeviceId = new ZIKDeviceId(deviceId.getUuid().toString());
        zettaSdkApi.startMonitoringDevice( // TODO: over here is where it monitors stream data!
            zikDeviceId,
            new ZettaSdkApi.ZikDeviceListener() {
                @Override
                public void updateFor(@NonNull ZIKServer server, @NonNull ZIKDevice device) {
//                    ZIKDevice updatedDevice = device.refreshWithLogEntry(entry);
                    ZIKDevice updatedDevice = device.fetchSync();
                    DeviceDetails deviceDetails = deviceParser.convertToDevice(server, updatedDevice);
                    listener.onUpdated(deviceDetails.getListItems());
                }
            }
        );
    }

    public void stopMonitoringDeviceUpdates() {
        zettaSdkApi.stopMonitoringDevice();
    }

    public void updateDetails(ZettaDeviceId deviceId,
                              String action,
                              final Map<String, Object> labelledInput,
                              @NonNull final DeviceDetailsService.DeviceListener listener) {
        final ZIKDeviceId zikDeviceId = new ZIKDeviceId(deviceId.getUuid().toString());
        zettaSdkApi.update(zikDeviceId, action, labelledInput, new ZIKCallback<ZIKDevice>() {

            @Override
            public void onSuccess(@NonNull ZIKDevice device) {
                ZIKServer server = zettaSdkApi.getServerContaining(zikDeviceId);
                DeviceDetails deviceDetails = deviceParser.convertToDevice(server, device);
                listener.onUpdated(deviceDetails.getListItems());
            }

            @Override
            public void onFailure(@NonNull ZIKException exception) {
                Log.e(exception);
            }
        });
    }

}
