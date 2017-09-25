package com.zetta.android.device;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

class EventsListItem implements ListItem {

    @NonNull private final ZettaDeviceId deviceId;
    @NonNull private final String description;
    @NonNull private final ZettaStyle style;

    public EventsListItem(@NonNull ZettaDeviceId deviceId,
                          @NonNull String description,
                          @NonNull ZettaStyle style) {
        this.deviceId = deviceId;
        this.description = description;
        this.style = style;
    }

    @NonNull
    public ZettaDeviceId getDeviceId() {
        return deviceId;
    }

    @Override
    public int getType() {
        return TYPE_EVENTS;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public Drawable createBackground() {
        return style.createBackgroundDrawable();
    }

    public int getForegroundColor() {
        return style.getForegroundColor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventsListItem)) {
            return false;
        }

        EventsListItem that = (EventsListItem) o;

        return deviceId.equals(that.deviceId);

    }

    @Override
    public int hashCode() {
        return deviceId.hashCode();
    }
}
