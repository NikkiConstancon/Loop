package com.zetta.android.settings;

/**
 * Created by gregoryGreg on 04/10/2017.
 */

public class ButtonItem implements SettingsItem {
    public int getType() {
        return TYPE_BUTTON;
    }
    String name;

    public ButtonItem(String name) {
        this.name = name;
    }

}
