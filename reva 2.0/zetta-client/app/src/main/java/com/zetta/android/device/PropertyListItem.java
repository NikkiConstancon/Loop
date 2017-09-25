package com.zetta.android.device;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

class PropertyListItem implements ListItem {

    @NonNull private final ZettaDeviceId zettaDeviceId;
    @NonNull private final String property;
    @NonNull private final String value;
    @NonNull private final ZettaStyle style;

    public PropertyListItem(@NonNull ZettaDeviceId zettaDeviceId,
                            @NonNull String property,
                            @NonNull String value,
                            @NonNull ZettaStyle style) {
        this.zettaDeviceId = zettaDeviceId;
        this.property = property;
        this.value = value;
        this.style = style;
    }

    @Override
    public int getType() {
        return TYPE_PROPERTY;
    }

    @NonNull
    public String getProperty() {
        return property;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    @NonNull
    public Drawable createBackgroundDrawable() {
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
        if (!(o instanceof PropertyListItem)) {
            return false;
        }

        PropertyListItem that = (PropertyListItem) o;

        return zettaDeviceId.equals(that.zettaDeviceId)
            && property.equals(that.property);

    }

    @Override
    public int hashCode() {
        int result = zettaDeviceId.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }
}
