package com.zetta.android.settings;

/**
 * Created by gregoryGreg on 03/10/2017.
 */

import java.io.Serializable;

public interface SettingsItem {
    int TYPE_REQUEST = 0;
    int TYPE_TITLE = 1;
    int TYPE_PENDING = 2;
    int TYPE_EXISTING = 3;
    int TYPE_STD_SETTINGS = 4;

    int getType();
}
