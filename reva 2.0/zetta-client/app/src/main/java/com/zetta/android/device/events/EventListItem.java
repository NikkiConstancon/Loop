package com.zetta.android.device.events;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;

public class EventListItem implements ListItem {

    @NonNull private final String transition;
    @NonNull private final String timestamp;
    private final int foregroundColor;
    private final int backgroundColor;

    public EventListItem(@NonNull String transition,
                         @NonNull String timestamp, int foregroundColor, int backgroundColor) {
        this.transition = transition;
        this.timestamp = timestamp;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public int getType() {
        return TYPE_EVENT;
    }

    @NonNull
    public String getTransition() {
        return transition;
    }

    @NonNull
    public String getTimeStamp() {
        return timestamp;
    }

    @ColorInt
    public int getForegroundColor() {
        return foregroundColor;
    }

    @ColorInt
    public int getBackgroundColor() {
        return backgroundColor;
    }
}
