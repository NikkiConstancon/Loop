package com.zetta.android.device;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

class StreamListItem implements ListItem {

    @NonNull private final ZettaDeviceId deviceId;
    @NonNull private final String stream;
    @NonNull private final String value;
    @NonNull private final ZettaStyle style;

    public StreamListItem(@NonNull ZettaDeviceId deviceId,
                          @NonNull String stream,
                          @NonNull String value,
                          @NonNull ZettaStyle style) {
        this.deviceId = deviceId;
        this.stream = stream;
        this.value = value;
        this.style = style;
    }

    @Override
    public int getType() {
        return TYPE_STREAM;
    }

    @NonNull
    public String getStream() {
        return stream;
    }

    @NonNull
    public String getValue() {
        return value;
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
        if (!(o instanceof StreamListItem)) {
            return false;
        }

        StreamListItem that = (StreamListItem) o;

        return deviceId.equals(that.deviceId)
            && stream.equals(that.stream);
    }

    @Override
    public int hashCode() {
        int result = deviceId.hashCode();
        result = 31 * result + stream.hashCode();
        return result;
    }
}
