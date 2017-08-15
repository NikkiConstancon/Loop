package com.zetta.android.device;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaDeviceId;
import com.zetta.android.ZettaStyle;

class PromotedListItem implements ListItem {

    @NonNull private final ZettaDeviceId zettaDeviceId;
    @NonNull private final String property;
    @NonNull private final String value;
    @NonNull private final String symbol;
    @NonNull private final ZettaStyle style;

    public PromotedListItem(@NonNull ZettaDeviceId zettaDeviceId,
                            @NonNull String property,
                            @NonNull String value,
                            @NonNull String symbol,
                            @NonNull ZettaStyle style) {
        this.zettaDeviceId = zettaDeviceId;
        this.property = property;
        this.value = value;
        this.symbol = symbol;
        this.style = style;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_PROMOTED;
    }

    @NonNull
    public String getProperty() {
        return property;
    }

    @ColorInt
    public int getForegroundColor() {
        return style.getForegroundColor();
    }

    @NonNull
    public String getValue() {
        return value;
    }

    @NonNull
    public String getSymbol() {
        return symbol;
    }

    @ColorInt
    public int getBackgroundColor() {
        return style.getBackgroundColor();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PromotedListItem)) {
            return false;
        }

        PromotedListItem that = (PromotedListItem) o;

        if (!zettaDeviceId.equals(that.zettaDeviceId)) {
            return false;
        }
        return property.equals(that.property);

    }

    @Override
    public int hashCode() {
        int result = zettaDeviceId.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }
}
