package com.zetta.android.settings;

/**
 * Created by gregoryGreg on 03/10/2017.
 */

public class TitleItem implements SettingsItem {

    public int getType() {
        return TYPE_TITLE;
    }

    private String title;

    public TitleItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
