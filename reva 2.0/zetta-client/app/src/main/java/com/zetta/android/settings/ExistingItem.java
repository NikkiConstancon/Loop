package com.zetta.android.settings;

/**
 * Created by gregoryGreg on 03/10/2017.
 */

public class ExistingItem implements SettingsItem {
    private String title;

    public int getType() {
        return TYPE_EXISTING;
    }

    public ExistingItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
