package com.zetta.android.device.actions;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

import java.util.List;

public class ActionMultipleInputListItem implements ListItem {

    @NonNull private final ZettaDeviceId deviceId;
    @NonNull private final List<String> labels;
    @NonNull private final String action;
    @NonNull private final ZettaStyle style;

    public ActionMultipleInputListItem(@NonNull ZettaDeviceId deviceId,
                                       @NonNull List<String> labels,
                                       @NonNull String action,
                                       @NonNull ZettaStyle style) {
        this.deviceId = deviceId;
        this.labels = labels;
        this.action = action;
        this.style = style;
    }

    @NonNull
    public ZettaDeviceId getDeviceId() {
        return deviceId;
    }

    @Override
    public int getType() {
        return TYPE_ACTION_MULTIPLE_INPUT;
    }

    @NonNull
    public List<String> getLabels() {
        return labels;
    }

    @NonNull
    public String getAction() {
        return action;
    }

    @NonNull
    public ColorStateList getActionTextColor() {
        return style.getBackgroundColorList();
    }

    @NonNull
    public ColorStateList getActionInputTextColor() {
        return style.getForegroundColorList();
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
        if (!(o instanceof ActionMultipleInputListItem)) {
            return false;
        }

        ActionMultipleInputListItem that = (ActionMultipleInputListItem) o;

        return deviceId.equals(that.deviceId)
            && labels.equals(that.labels);

    }

    @Override
    public int hashCode() {
        int result = deviceId.hashCode();
        result = 31 * result + labels.hashCode();
        return result;
    }
}
