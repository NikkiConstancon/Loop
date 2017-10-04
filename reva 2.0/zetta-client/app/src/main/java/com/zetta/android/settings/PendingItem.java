package com.zetta.android.settings;

/**
 * Created by gregoryGreg on 04/10/2017.
 */

public class PendingItem implements SettingsItem {
    public int getType() {
        return TYPE_PENDING;
    }

    private String title;

    public PendingItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
