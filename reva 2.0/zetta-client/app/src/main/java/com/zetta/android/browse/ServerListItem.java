package com.zetta.android.browse;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.zetta.android.ListItem;
import com.zetta.android.ZettaStyle;

class ServerListItem implements ListItem {

    @NonNull private final String name;
    @NonNull private final ZettaStyle style;

    public ServerListItem(@NonNull String name, @NonNull ZettaStyle style) {
        this.name = name;
        this.style = style;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_SERVER;
    }

    @ColorInt
    public int getSwatchColor() {
        return style.getForegroundColor();
    }

    @ColorInt
    public int getTextColor() {
        return style.getForegroundColor();
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Drawable createBackground() {
        return style.createBackgroundDrawable();
    }
}
