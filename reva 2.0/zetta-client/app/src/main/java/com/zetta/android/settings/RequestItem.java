package com.zetta.android.settings;

/**
 * Created by gregoryGreg on 03/10/2017.
 */

public class RequestItem implements SettingsItem {
    private String title;
    private String subtitle;

    public int getType() {
        return TYPE_REQUEST;
    }

    public RequestItem(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public RequestItem(String title) {
        this(title, "");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
