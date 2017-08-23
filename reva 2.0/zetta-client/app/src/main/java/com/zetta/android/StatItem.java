package com.zetta.android;

/**
 * Created by Greg on 8/23/2017.
 */

/**
 * This interface is used to determine types of views when loading
 * them into the statistics view page.
 */
public interface StatItem {
    int TYPE_SIMPLE_STAT = 0;
    int TYPE_LINE_GRAPH = 1;


    int getType();

}
