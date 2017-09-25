package com.zetta.android.browse;

import android.support.annotation.NonNull;

import com.apigee.zettakit.ZIKDevice;
import com.apigee.zettakit.ZIKDeviceId;
import com.apigee.zettakit.ZIKServer;
import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaSdkApi;
import com.zetta.android.ZettaStyle;
import com.zetta.android.browse.DeviceListService.DeviceListItemListener;
import com.zetta.android.device.actions.ActionListItemParser;

import java.util.List;
import java.util.Map;

class DeviceListSdkService {

    @NonNull private final ZettaSdkApi zettaSdkApi;
    @NonNull private final DeviceList.Parser deviceListParser;
    private String serverName; // this is a hotfix

    public DeviceListSdkService(String serverName) {
        this.serverName = serverName; // this is a hotfix
        zettaSdkApi = ZettaSdkApi.INSTANCE;
        deviceListParser = new DeviceList.Parser(new ZettaStyle.Parser(), new ActionListItemParser());
    }

    @NonNull
    public List<ListItem> getListItems(@NonNull String url) {
        zettaSdkApi.registerRoot(url);
        List<ZIKServer> zikServers = zettaSdkApi.getServers();
        return deviceListParser.createListItems(zikServers, serverName); // this is a hotfix
    }

    @NonNull
    public List<ListItem> getQuickActions(@NonNull ZettaDeviceId deviceId) {
        ZIKDeviceId zikDeviceId = new ZIKDeviceId(deviceId.getUuid().toString());
        ZIKServer zikServer = zettaSdkApi.getServerContaining(zikDeviceId);
        ZIKDevice zikDevice = zettaSdkApi.getFullDevice(zikDeviceId);
        return deviceListParser.createQuickActions(zikServer, zikDevice);
    }

    public void startMonitorDeviceUpdates(@NonNull String url, @NonNull final DeviceListItemListener listener) {
        zettaSdkApi.registerRoot(url);
        zettaSdkApi.startMonitoringAllServerAllDevices(new ZettaSdkApi.ZikDeviceListener() {
            @Override
            public void updateFor(@NonNull ZIKServer server, @NonNull ZIKDevice device) {
                DeviceListItem listItem = deviceListParser.createDeviceListItem(server, device);
                listener.onUpdated(listItem);
            }
        });
    }

    public void stopMonitoringStreamedUpdates() {
        zettaSdkApi.stopMonitoringOpenStreams();
    }

    public void updateDetails(@NonNull ZettaDeviceId deviceId, @NonNull String action, @NonNull Map<String, Object> labelledInput) {
        ZIKDeviceId zikDeviceId = new ZIKDeviceId(deviceId.getUuid().toString());
        zettaSdkApi.update(zikDeviceId, action, labelledInput);
    }
}
