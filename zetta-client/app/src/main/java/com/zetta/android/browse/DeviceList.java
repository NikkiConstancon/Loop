package com.zetta.android.browse;

import android.support.annotation.NonNull;

import com.apigee.zettakit.ZIKDevice;
import com.apigee.zettakit.ZIKServer;
import com.apigee.zettakit.ZIKTransition;
import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;
import com.zetta.android.device.actions.ActionListItemParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

class DeviceList {

    static class Parser {

        private static final Map<UUID, ZettaDeviceId> zettaDeviceIdCache = new HashMap<>();
        private static final Pattern IS_DOUBLE = Pattern.compile("[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");

        private final ZettaStyle.Parser zettaStyleParser;
        private final ActionListItemParser actionParser;

        public Parser(ZettaStyle.Parser zettaStyleParser, ActionListItemParser actionParser) {
            this.zettaStyleParser = zettaStyleParser;
            this.actionParser = actionParser;
        }

        @NonNull
        public List<ListItem> createListItems(@NonNull List<ZIKServer> servers) {
            List<ListItem> items = new ArrayList<>();
            for (ZIKServer server : servers) {
                ZettaStyle serverStyle = zettaStyleParser.parseStyle(server);
                items.add(createServerListItem(serverStyle, server));

                List<ZIKDevice> zikDevices = server.getDevices();

                if (zikDevices.isEmpty()) {
                    items.add(createEmptyServerListItem(serverStyle));
                } else {
                    for (ZIKDevice device : zikDevices) {
                        items.add(createDeviceListItem(server, device));
                    }
                }
            }

            return items;
        }

        @NonNull
        private ServerListItem createServerListItem(@NonNull ZettaStyle style, @NonNull ZIKServer zikServer) {
            String serverName = zikServer.getName();
            return new ServerListItem(serverName, style);
        }

        @NonNull
        private ListItem.EmptyListItem createEmptyServerListItem(@NonNull ZettaStyle style) {
            return new ListItem.EmptyListItem("No devices online for this server", style);
        }

        @NonNull
        public DeviceListItem createDeviceListItem(@NonNull ZIKServer server, @NonNull ZIKDevice device) {
            ZettaStyle style = zettaStyleParser.parseStyle(server, device);
            String state = getState(server, device);
            return createDeviceListItem(style, device, state);
        }

        private String getState(@NonNull ZIKServer server, @NonNull ZIKDevice device) {
            String state = device.getState();
            Map serverPropsStyle = (Map) server.getProperties().get("style");
            if (serverPropsStyle == null) {
                return state;
            }
            Map entities = (Map) serverPropsStyle.get("entities");
            String deviceType = device.getType();
            if (entities.containsKey(deviceType)) {
                Map deviceProperties = (Map) ((Map) entities.get(deviceType)).get("properties");
                if (deviceProperties.containsKey("state")) {
                    if (((Map) deviceProperties.get("state")).get("display").equals("none")) {
                        Iterator iterator = deviceProperties.keySet().iterator();
                        iterator.next();
                        String promotedPropertyKey = (String) iterator.next();
                        String promotedPropertyValue = String.valueOf(device.getProperties().get(promotedPropertyKey));
                        Map promotedProperties = (Map) deviceProperties.get(promotedPropertyKey);
                        String symbol = (String) promotedProperties.get("symbol");
                        Double significantDigits = (double) promotedProperties.get("significantDigits");
                        boolean isDouble = IS_DOUBLE.matcher(promotedPropertyValue).matches();
                        if (isDouble) {
                            BigDecimal bigValue = new BigDecimal(promotedPropertyValue).setScale(significantDigits.intValue(), BigDecimal.ROUND_FLOOR);
                            String roundedValue = bigValue.toString();
                            state = roundedValue + symbol;
                        } else {
                            state = promotedPropertyValue;
                        }
                    }
                }
            }
            return state;
        }

        @NonNull
        private DeviceListItem createDeviceListItem(@NonNull ZettaStyle style,
                                                    @NonNull ZIKDevice device,
                                                    @NonNull String state) {
            return new DeviceListItem(
                getDeviceId(device),
                device.getName(),
                state,
                style
            );
        }

        @NonNull
        private ZettaDeviceId getDeviceId(@NonNull ZIKDevice device) {
            UUID uuid = device.getDeviceId().getUuid();
            if (zettaDeviceIdCache.containsKey(uuid)) {
                return zettaDeviceIdCache.get(uuid);
            } else {
                ZettaDeviceId zettaDeviceId = new ZettaDeviceId(uuid);
                zettaDeviceIdCache.put(uuid, zettaDeviceId);
                return zettaDeviceId;
            }
        }

        public List<ListItem> createQuickActions(@NonNull ZIKServer zikServer, @NonNull ZIKDevice zikDevice) {
            List<ListItem> listItems = new ArrayList<>();
            ZettaStyle style = zettaStyleParser.parseStyle(zikServer, zikDevice);

            listItems.add(createDeviceHeaderListItem(zikDevice));

            List<ZIKTransition> transitions = zikDevice.getTransitions();
            if (transitions.isEmpty()) {
                listItems.add(createEmptyQuickActionsListItem(style));
            }
            ZettaDeviceId deviceId = getDeviceId(zikDevice);
            for (ZIKTransition transition : transitions) {
                if (transition.getName().startsWith("_")) {
                    continue;
                }
                listItems.add(actionParser.parseActionListItem(deviceId, style, transition));
            }
            return listItems;
        }

        @NonNull
        private ListItem.HeaderListItem createDeviceHeaderListItem(@NonNull ZIKDevice zikDevice) {
            return new ListItem.HeaderListItem(zikDevice.getName());
        }

        @NonNull
        private ListItem.EmptyListItem createEmptyQuickActionsListItem(@NonNull ZettaStyle style) {
            return new ListItem.EmptyListItem("No actions for this device.", style);
        }
    }

}
