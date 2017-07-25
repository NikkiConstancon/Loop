package com.zetta.android;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public interface ListItem {

    int TYPE_SERVER = 0;
    int TYPE_EMPTY = 2;
    int TYPE_DEVICE = 1;
    int TYPE_HEADER = 3;
    int TYPE_ACTION_TOGGLE = 4;
    int TYPE_ACTION_SINGLE_INPUT = 5;
    int TYPE_ACTION_MULTIPLE_INPUT = 6;
    int TYPE_STREAM = 7;
    int TYPE_PROPERTY = 8;
    int TYPE_EVENTS = 9;
    int TYPE_EVENT = 10;
    int TYPE_STATE = 11;
    int TYPE_LOADING = 12;
    int TYPE_PROMOTED = 13;

    int getType();

    class HeaderListItem implements ListItem {

        @NonNull private final String title;

        public HeaderListItem(@NonNull String title) {
            this.title = title;
        }

        @Override
        public int getType() {
            return TYPE_HEADER;
        }

        @NonNull
        public String getTitle() {
            return title;
        }
    }

    class EmptyListItem implements ListItem {

        @NonNull private final String message;
        @NonNull private final ZettaStyle style;

        public EmptyListItem(@NonNull String message, @NonNull ZettaStyle style) {
            this.message = message;
            this.style = style;
        }

        @Override
        public int getType() {
            return ListItem.TYPE_EMPTY;
        }

        @NonNull
        public String getMessage() {
            return message;
        }

        @NonNull
        public Drawable createBackground() {
            return style.createBackgroundDrawable();
        }

        @ColorInt
        public int getTextColor() {
            return style.getForegroundColor();
        }
    }

    class LoadingListItem implements ListItem {

        @Override
        public int getType() {
            return ListItem.TYPE_LOADING;
        }
    }
}
