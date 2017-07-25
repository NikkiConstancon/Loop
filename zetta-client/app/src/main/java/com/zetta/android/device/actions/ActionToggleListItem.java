package com.zetta.android.device.actions;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

public class ActionToggleListItem implements ListItem {

    @NonNull private final ZettaDeviceId deviceId;
    @NonNull private final String label;
    @NonNull private final String action;
    @NonNull private final ZettaStyle style;

    public ActionToggleListItem(@NonNull ZettaDeviceId deviceId,
                                @NonNull String label,
                                @NonNull String action,
                                @NonNull ZettaStyle style) {
        this.deviceId = deviceId;
        this.label = label;
        this.action = action;
        this.style = style;
    }

    @NonNull
    public ZettaDeviceId getDeviceId() {
        return deviceId;
    }

    @Override
    public int getType() {
        return TYPE_ACTION_TOGGLE;
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
    public ColorStateList getActionInputTextColor() {
        return style.getForegroundColorList();
    }

    @NonNull
    public ColorStateList getActionTextColor() {
        return style.getBackgroundColorList();
    }

    @NonNull
    public Drawable createActionBackground() {
        return style.createForegroundDrawable();
    }

    @NonNull
    public Drawable createBackground() {
        return style.createBackgroundDrawable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActionToggleListItem)) {
            return false;
        }

        ActionToggleListItem that = (ActionToggleListItem) o;

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
