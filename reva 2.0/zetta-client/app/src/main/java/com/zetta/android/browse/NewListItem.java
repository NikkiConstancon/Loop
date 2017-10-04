package com.zetta.android.browse;

import com.zetta.android.ListItem;

/**
 * Created by gregoryGreg on 04/10/2017.
 */

public class NewListItem implements ListItem {
    private String title;
    public int getType() {
        return TYPE_NEW;
    }

    public NewListItem(String str) {
        this.title = str;
    }

    public String getTitle() {
        return title;
    }
}
