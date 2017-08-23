package com.zetta.android.browse;

import com.zetta.android.GraphEntry;

import java.util.LinkedList;

/**
 * Created by Greg on 8/23/2017.
 * This class is used for Graph cards used in the RecyclerView StatListAdapter
 */
public class GraphStatItem extends SimpleStatItem {
    private LinkedList<GraphEntry> entries = new LinkedList<GraphEntry>();

    public GraphStatItem(String deviceName,
                         String imgURL,
                         String statName,
                         String start,
                         String end,
                         String units,
                         LinkedList<GraphEntry> entries) {
        super(deviceName, imgURL, statName, start, end, units, 0);
        this.entries = entries;
    }

    /**
     *
     * @return the coordinates to the entries for the graph
     */
    public LinkedList<GraphEntry> getEntries() {
        return entries;
    }

    /**
     *
     * @return the type of device, which is line graph
     */
    @Override
    public int getType() {
        return TYPE_LINE_GRAPH;
    }
}
