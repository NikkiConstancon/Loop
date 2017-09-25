package com.zetta.android.device;

import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;
import com.zetta.android.device.actions.ActionMultipleInputListItem;
import com.zetta.android.device.actions.ActionSingleInputListItem;
import com.zetta.android.device.actions.ActionToggleListItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class DeviceDetailsMockService {

    private static final ZettaDeviceId DEVICE_ID = new ZettaDeviceId(UUID.fromString("86fee1a0-2fd1-11e6-a818-0002a5d5c51b"));

    @NonNull private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private RandomStreamGenerator streamGenerator;
    private static final ZettaStyle DEFAULT_STYLE = new ZettaStyle(Color.parseColor("#1111dd"), Color.parseColor("#d9d9d9"), Uri.EMPTY, ZettaStyle.TintMode.ORIGINAL);

    @NonNull
    public DeviceDetails getDetails() {
        List<ListItem> items = createListItems(DEFAULT_STYLE);

        return new DeviceDetails(getDeviceName(DEFAULT_STYLE), getServerName(DEFAULT_STYLE), DEFAULT_STYLE, items);
    }

    @NonNull
    private Spannable getServerName(ZettaStyle style) {
        Spannable name = new SpannableString("neworleans");
        name.setSpan(style.createBackgroundColorSpan(), 0, name.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        name.setSpan(style.createForegroundColorSpan(), 0, name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return name;
    }

    @NonNull
    private Spannable getDeviceName(ZettaStyle style) {
        Spannable name = new SpannableString("Porch Light");
        name.setSpan(style.createBackgroundColorSpan(), 0, name.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        name.setSpan(style.createForegroundColorSpan(), 0, name.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return name;
    }

    private List<ListItem> createListItems(ZettaStyle style) {
        List<ListItem> items = new ArrayList<>();

        items.add(new StateListItem(
            DEVICE_ID, "on",
            new ZettaStyle(style.getForegroundColor(), style.getBackgroundColor(), Uri.parse("http://www.zettaapi.org/icons/light-on.png"), ZettaStyle.TintMode.ORIGINAL)
        ));

        items.add(new ListItem.HeaderListItem("Actions"));
        items.add(new ActionToggleListItem(
            DEVICE_ID,
            "open", "open",
            style
        ));
        items.add(new ActionSingleInputListItem(
            DEVICE_ID,
            "brightness", "set-brightness",
            style
        ));
        items.add(new ActionToggleListItem(
            DEVICE_ID,
            "blink", "set-blinker",
            style
        ));
        items.add(new ActionToggleListItem(
            DEVICE_ID,
            "turn-off", "turn-off",
            style
        ));
        items.add(new ActionMultipleInputListItem(
            DEVICE_ID, Arrays.asList("direction", "speed", "duration", "walking style", "warning message"),
            "walk",
            style
        ));

        items.add(new ListItem.HeaderListItem("Streams"));
        items.add(new StreamListItem(DEVICE_ID, "temperature", "23.872342385", style));

        items.add(new ListItem.HeaderListItem("Properties"));
        items.add(new PropertyListItem(DEVICE_ID, "type", "light", style));
        items.add(new PropertyListItem(DEVICE_ID, "style", "", style));
        items.add(new PropertyListItem(DEVICE_ID, "brightness", "", style));
        items.add(new PropertyListItem(DEVICE_ID, "name", "Porch Light", style));
        items.add(new PropertyListItem(DEVICE_ID, "id", "5113a9d2-0dfa-4061-8034-8cde5bbb41b2", style));
        items.add(new PropertyListItem(DEVICE_ID, "state", "on", style));
        items.add(new PropertyListItem(DEVICE_ID, "color", "", style));
        items.add(new PropertyListItem(DEVICE_ID, "blink", "", style));

        items.add(new ListItem.HeaderListItem("Events"));
        items.add(new EventsListItem(DEVICE_ID, "View Events (42)", style));

        return items;
    }

    public void startMonitoringDeviceUpdates(@NonNull DeviceDetailsService.DeviceListener listener) {
        streamGenerator = new RandomStreamGenerator(mainThreadHandler, listener);
        mainThreadHandler.postDelayed(streamGenerator, TimeUnit.SECONDS.toMillis(1));
    }

    public void updateDetails(@NonNull ZettaDeviceId deviceId,
                              @NonNull String action,
                              @NonNull Map<String, Object> labelledInput,
                              @NonNull DeviceDetailsService.DeviceListener listener) {
        List<ListItem> listItems = getDetails().getListItems();
        int i = listItems.indexOf(new PropertyListItem(deviceId, "color", "", DEFAULT_STYLE));
        listItems.remove(i);
        String newValue = String.valueOf(labelledInput.values().iterator().next());
        listItems.add(i, new PropertyListItem(deviceId, "color", newValue, DEFAULT_STYLE));

        listener.onUpdated(listItems);
    }

    private class RandomStreamGenerator implements Runnable {

        @NonNull private final Random random = new Random();

        @NonNull private final Handler handler;
        @NonNull private final DeviceDetailsService.DeviceListener listener;

        private RandomStreamGenerator(@NonNull Handler handler, @NonNull DeviceDetailsService.DeviceListener listener) {
            this.handler = handler;
            this.listener = listener;
        }

        @Override
        public void run() {
            String value = "23." + random.nextInt();
            int foregroundColor = Color.parseColor("#1111dd");
            int backgroundColor = Color.parseColor("#d9d9d9");
            Uri image = Uri.EMPTY;
            ZettaStyle.TintMode tintMode = ZettaStyle.TintMode.ORIGINAL;
            ZettaStyle style = new ZettaStyle(foregroundColor, backgroundColor, image, tintMode);
            List<ListItem> listItems = createListItems(style);
            StreamListItem streamListItem = new StreamListItem(DEVICE_ID, "temperature", value, style);
            int position = listItems.indexOf(streamListItem);
            listItems.remove(position);
            listItems.add(position, streamListItem);
            listener.onUpdated(listItems);
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
        }
    }

    public void stopMonitoringDeviceUpdates() {
        mainThreadHandler.removeCallbacks(streamGenerator);
    }
}
