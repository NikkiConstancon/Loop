package com.zetta.android.browse;

import com.zetta.android.StatItem;

/**
 * Created by gregoryGreg on 09/10/2017.
 */

public class StatTitle implements StatItem {
    private String title;


    public int getType() {
        return TYPE_TITLE;
    }

    public StatTitle(String tit) {
        this.title = tit;
    }

    public String getTitle() {
        return title;
    }
}
