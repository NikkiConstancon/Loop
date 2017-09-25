package com.zetta.android.device;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

class StateListItem implements ListItem {

    @NonNull private final ZettaDeviceId zettaDeviceId;
    @NonNull private final String state;
    @NonNull private final ZettaStyle style;

    public StateListItem(@NonNull ZettaDeviceId zettaDeviceId, @NonNull String state, @NonNull ZettaStyle style) {
        this.zettaDeviceId = zettaDeviceId;
        this.state = state;
        this.style = style;
    }

    @Override
    public int getType() {
        return TYPE_STATE;
    }

    @NonNull
    public String getState() {
        return state;
    }

    @NonNull
    public Uri getStateImageUri() {
        return style.getStateImage();
    }

    public int getStateColor() {
        return style.getTintColor();
    }

    public int getBackgroundColor() {
        return style.getBackgroundColor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StateListItem)) {
            return false;
        }

        StateListItem that = (StateListItem) o;

        return zettaDeviceId.equals(that.zettaDeviceId);

    }

    @Override
    public int hashCode() {
        return zettaDeviceId.hashCode();
    }
}
