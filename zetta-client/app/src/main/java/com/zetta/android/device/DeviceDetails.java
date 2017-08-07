package com.zetta.android.device;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import com.apigee.zettakit.ZIKDevice;
import com.apigee.zettakit.ZIKServer;
import com.apigee.zettakit.ZIKStream;
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

class DeviceDetails {

    private final Spannable deviceName;
    private final Spannable serverName;
    private final ZettaStyle style;
    private final List<ListItem> listItems;

    public DeviceDetails(Spannable deviceName, Spannable serverName, ZettaStyle style, List<ListItem> listItems) {
        this.deviceName = deviceName;
        this.serverName = serverName;
        this.style = style;
        this.listItems = listItems;
    }

    @NonNull
    public Spannable getName() {
        return deviceName;
    }

    @NonNull
    public Spannable getSeverName() {
        return serverName;
    }

    @NonNull
    public Drawable createBackground() {
        return style.createBackgroundDrawable();
    }

    @NonNull
    public List<ListItem> getListItems() {
        return listItems;
    }

    public int getTintColor() {
        return style.getTintColor();
    }

    public int getBackgroundColor() {
        return style.getBackgroundColor();
    }

    public static class Parser {

        private static final Map<UUID, ZettaDeviceId> zettaDeviceIdCache = new HashMap<>();

        private final ZettaStyle.Parser zettaStyleParser;
        private final ActionListItemParser actionParser;

        public Parser(ZettaStyle.Parser zettaStyleParser, ActionListItemParser actionParser) {
            this.zettaStyleParser = zettaStyleParser;
            this.actionParser = actionParser;
        }

        @NonNull
        public DeviceDetails convertToDevice(@NonNull final ZIKServer zikServer,
                                             @NonNull final ZIKDevice zikDevice) {
            ZettaStyle zettaStyle = zettaStyleParser.parseStyle(zikServer, zikDevice);
            Spannable serverName = getServerName(zikServer, zettaStyle);
            Spannable deviceName = getDeviceName(zikDevice, zettaStyle);
            List<ListItem> listItems = convertToDeviceListItems(zettaStyle, zikServer, zikDevice);
            return new DeviceDetails(serverName, deviceName, zettaStyle, listItems);
        }

        @NonNull
        private Spannable getDeviceName(@NonNull ZIKDevice zikDevice, @NonNull ZettaStyle zettaStyle) {
            Spannable name = new SpannableString(zikDevice.getName());
            name.setSpan(zettaStyle.createBackgroundColorSpan(), 0, name.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            name.setSpan(zettaStyle.createForegroundColorSpan(), 0, name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return name;
        }

        @NonNull
        private Spannable getServerName(@NonNull ZIKServer zikServer, @NonNull ZettaStyle zettaStyle) {
            Spannable name = new SpannableString(zikServer.getName());
            BackgroundColorSpan backgroundColorSpan = zettaStyle.createBackgroundColorSpan();
            name.setSpan(backgroundColorSpan, 0, name.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ForegroundColorSpan foregroundColorSpan = zettaStyle.createForegroundColorSpan();
            name.setSpan(foregroundColorSpan, 0, name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            return name;
        }

        @NonNull
        private List<ListItem> convertToDeviceListItems(@NonNull ZettaStyle style, @NonNull ZIKServer zikServer, @NonNull ZIKDevice zikDevice) {
            List<ListItem> listItems = new ArrayList<>();

            ZettaDeviceId deviceId = getDeviceId(zikDevice);
            listItems.add(createPromotedListItem(deviceId, style, zikServer, zikDevice));

            listItems.add(new ListItem.HeaderListItem("Actions"));

            List<ZIKTransition> transitions = zikDevice.getTransitions();
            if (transitions.isEmpty()) {
                listItems.add(createEmptyActionsListItem(style));
            }
            for (ZIKTransition transition : transitions) {
                if (transition.getName().startsWith("_")) {
                    continue;
                }
                listItems.add(actionParser.parseActionListItem(deviceId, style, transition));
            }

            listItems.add(new ListItem.HeaderListItem("Streams"));

            List<ZIKStream> allStreams = zikDevice.getAllStreams();
            for (ZIKStream stream : allStreams) {
                if (stream.getTitle().equals("logs")) {
                    continue;
                }
                listItems.add(createStreamListItem(style, deviceId, zikDevice, stream));
            }

            listItems.add(new ListItem.HeaderListItem("Properties"));

            Map<String, Object> deviceProperties = zikDevice.getProperties();
            for (String propertyName : deviceProperties.keySet()) {
                if (propertyName.equals("style")) {
                    continue;
                }
                listItems.add(createPropertyListItem(deviceId, style, deviceProperties, propertyName));
            }
            if (deviceProperties.isEmpty()) {
                listItems.add(createEmptyPropertiesListItem(style));
            }

            listItems.add(new ListItem.HeaderListItem("Events"));

            listItems.add(createEventsListItem(style, deviceId));

            return listItems;
        }

        @NonNull
        private ListItem createPromotedListItem(@NonNull ZettaDeviceId deviceId,
                                                @NonNull ZettaStyle style,
                                                @NonNull ZIKServer zikServer,
                                                @NonNull ZIKDevice zikDevice) {
            Map serverPropsStyle = (Map) zikServer.getProperties().get("style");
            if(serverPropsStyle == null) {
                String state = zikDevice.getState();
                return new StateListItem(deviceId, state, style);
            }
            Map entities = (Map) serverPropsStyle.get("entities");
            String deviceType = zikDevice.getType();
            if (entities.containsKey(deviceType)) {
                Map deviceProperties = (Map) ((Map) entities.get(deviceType)).get("properties");
                if (deviceProperties.containsKey("state")) {
                    if (((Map) deviceProperties.get("state")).get("display").equals("none")) {
                        Iterator iterator = deviceProperties.keySet().iterator();
                        iterator.next();
                        String promotedPropertyKey = (String) iterator.next();
                        String value = String.valueOf(zikDevice.getProperties().get(promotedPropertyKey));
                        Map promotedProperties = (Map) deviceProperties.get(promotedPropertyKey);
                        String symbol = (String) promotedProperties.get("symbol");
                        Double significantDigits = (double) promotedProperties.get("significantDigits");
                        BigDecimal bigValue = new BigDecimal(value).setScale(significantDigits.intValue(), BigDecimal.ROUND_FLOOR);
                        String roundedValue = bigValue.toString();
                        return new PromotedListItem(deviceId, promotedPropertyKey, roundedValue, symbol, style);
                    }
                }

            }
            String state = zikDevice.getState();
            return new StateListItem(deviceId, state, style);
        }

        @NonNull
        private ListItem.EmptyListItem createEmptyActionsListItem(@NonNull ZettaStyle style) {
            return new ListItem.EmptyListItem("No actions for this device.", style);
        }

        @NonNull
        private StreamListItem createStreamListItem(@NonNull ZettaStyle style,
                                                    @NonNull ZettaDeviceId deviceId,
                                                    @NonNull ZIKDevice device,
                                                    @NonNull ZIKStream zikStream) {
            String stream = zikStream.getTitle();
            String value = "";
            Map<String, Object> properties = device.getProperties();
            if (properties.containsKey(stream)) {
                value = String.valueOf(properties.get(stream));
            }
            return new StreamListItem(
                deviceId,
                stream,
                value,
                style
            );
        }

        @NonNull
        private ListItem.EmptyListItem createEmptyPropertiesListItem(@NonNull ZettaStyle style) {
            return new ListItem.EmptyListItem("No properties for this device.", style);
        }

        @NonNull
        private PropertyListItem createPropertyListItem(@NonNull ZettaDeviceId deviceId,
                                                        @NonNull ZettaStyle style,
                                                        @NonNull Map<String, Object> deviceProperties,
                                                        @NonNull String propertyName) {
            String propertyValue = String.valueOf(deviceProperties.get(propertyName));
            return new PropertyListItem(deviceId, propertyName, propertyValue, style);
        }

        @NonNull
        private EventsListItem createEventsListItem(@NonNull ZettaStyle style, @NonNull ZettaDeviceId deviceId) {
            return new EventsListItem(deviceId, "View Events (...)", style);
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
    }
}
