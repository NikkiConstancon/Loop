package com.zetta.android.device.actions;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

public class ActionSingleInputListItem implements ListItem {

    @NonNull private final ZettaDeviceId deviceId;
    @NonNull private final String label;
    @NonNull private final String action;
    @NonNull private final ZettaStyle zettaStyle;

    public ActionSingleInputListItem(@NonNull ZettaDeviceId deviceId,
                                     @NonNull String label,
                                     @NonNull String action,
                                     @NonNull ZettaStyle zettaStyle) {
        this.deviceId = deviceId;
        this.label = label;
        this.action = action;
        this.zettaStyle = zettaStyle;
    }

    @NonNull
    public ZettaDeviceId getDeviceId() {
        return deviceId;
    }

    @Override
    public int getType() {
        return TYPE_ACTION_SINGLE_INPUT;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    @NonNull
    public String getAction() {
        return action;
    }

    @NonNull
    public ColorStateList getActionTextColor() {
        return zettaStyle.getBackgroundColorList();
    }

    @NonNull
    public ColorStateList getActionInputTextColor() {
        return zettaStyle.getForegroundColorList();
    }

    @NonNull
    public Drawable getActionBackground() {
        return zettaStyle.createForegroundDrawable();
    }

    @NonNull
    public Drawable getBackground() {
        return zettaStyle.createBackgroundDrawable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActionSingleInputListItem)) {
            return false;
        }

        ActionSingleInputListItem that = (ActionSingleInputListItem) o;

        return deviceId.equals(that.deviceId)
            && label.equals(that.label);

    }

    @Override
    public int hashCode() {
        int result = deviceId.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }
}
