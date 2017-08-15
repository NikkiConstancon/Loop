package com.zetta.android.browse;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.apigee.zettakit.ZIKDevice;
import com.apigee.zettakit.ZIKServer;
import com.apigee.zettakit.ZIKStream;
import com.apigee.zettakit.ZIKTransition;
import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;
import com.zetta.android.device.StreamListItem;
import com.zetta.android.device.actions.ActionListItemParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The DeviceList class contains a parser that uses the ZettaStyle parser and ActionListItem parser
 */
class DeviceList {
    /**
     *The parser that is used by the DeviceList class
     */
    static class Parser {

        private static final Map<UUID, ZettaDeviceId> zettaDeviceIdCache = new HashMap<>();
        private static final Pattern IS_DOUBLE = Pattern.compile("[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");

        private final ZettaStyle.Parser zettaStyleParser;
        private final ActionListItemParser actionParser;

        /**
         * The main constructor that sets the style and action parsers
         * @param zettaStyleParser to parse the style
         * @param actionParser to parse actions
         */
        public Parser(ZettaStyle.Parser zettaStyleParser, ActionListItemParser actionParser) {
            this.zettaStyleParser = zettaStyleParser;
            this.actionParser = actionParser;
        }

        /**
         * Calls all the servers and parses the styling and adds them to a list, does the same for devices
         * @param servers a list of ZettaKit server items
         * @return returns a list of all servers and devices with styling
         */
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

        /**
         * Creates a server list item
         * @param style
         * @param zikServer
         * @return a ServerListItem that uses the serverName and the ZettaStyle
         */
        @NonNull
        private ServerListItem createServerListItem(@NonNull ZettaStyle style, @NonNull ZIKServer zikServer) {
            String serverName = zikServer.getName();
            return new ServerListItem(serverName, style);
        }

        /**
         * Creates an item that represents an empty server
         * @param style
         * @return a emptylistitem using a specific zetta style
         */
        @NonNull
        private ListItem.EmptyListItem createEmptyServerListItem(@NonNull ZettaStyle style) {
            return new ListItem.EmptyListItem("No devices online for this server", style);
        }

        /**
         * Creates a device list item for a specific server
         * @param server the zetta server object in question
         * @param device the device object in question
         * @return a DeviceListItem based on the server and device and the state
         */
        @NonNull
        public DeviceListItem createDeviceListItem(@NonNull ZIKServer server, @NonNull ZIKDevice device) {
            ZettaStyle style = zettaStyleParser.parseStyle(server, device);
            String state = getState(server, device);

            Map<String, Object> properties = device.getProperties();
            String val = String.valueOf(properties.get("units"));
            if (val != "null") {
                return createDeviceListItem(style, device, state, val);
            } else {
                return createDeviceListItem(style, device, state, "N/A");
            }

        }

        /**
         * Finds the state to show on the main page
         * @param server the zetta server in question
         * @param device the zetta device in question
         * @return the state of the device
         */
        private String getState(@NonNull ZIKServer server, @NonNull ZIKDevice device) {
            String state = device.getState();
            Map serverPropsStyle = (Map) server.getProperties().get("style");
            if (serverPropsStyle == null) {
                Map<String, Object> properties = device.getProperties();
                String val = String.valueOf(properties.get("vitals"));


                if (val != "null") {
                    Double dub = Double.parseDouble(val);
                    val = String.format( "%.2f", dub );
                    return val;
                } else {
                    return state;
                }
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
            return "bibi";
        }

        /**
         * This helper function is called to create a new DeviceListItem (which is another private function)
         * @param style
         * @param device
         * @param state
         * @return
         */
        @NonNull
        private DeviceListItem createDeviceListItem(@NonNull ZettaStyle style,
                                                    @NonNull ZIKDevice device,
                                                    @NonNull String state,
                                                    @NonNull String units) {
            return new DeviceListItem(
                getDeviceId(device),
                device.getName(),
                state,
                style,
                    units
            );
        }


        private StreamListItem createStreamListItem(@NonNull ZettaStyle style,
                                                    @NonNull ZIKDevice device,
                                                    @NonNull ZIKStream zikStream) {
            String stream = zikStream.getTitle();
            String value = "";
            Map<String, Object> properties = device.getProperties();
            if (properties.containsKey(stream)) {
                value = String.valueOf(properties.get(stream));
            }
            return new StreamListItem(
                    getDeviceId(device),
                    stream,
                    value,
                    style
            );
        }

        /**
         * Fetches the ID of a device
         * @param device the device object
         * @return the ZettaDeviceId object
         */
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

        /**
         * Creates the quick actions for each device according to their mapping on the device driver
         * @param zikServer
         * @param zikDevice
         * @return a List of ListItems
         */
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

        /**
         *
         * @param zikDevice
         * @return
         */
        @NonNull
        private ListItem.HeaderListItem createDeviceHeaderListItem(@NonNull ZIKDevice zikDevice) {
            return new ListItem.HeaderListItem(zikDevice.getName());
        }

        /**
         *
         * @param style
         * @return
         */
        @NonNull
        private ListItem.EmptyListItem createEmptyQuickActionsListItem(@NonNull ZettaStyle style) {
            return new ListItem.EmptyListItem("No actions for this device.", style);
        }
    }

}
