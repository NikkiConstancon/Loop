package com.zetta.android;

/**
 * Created by Greg on 8/23/2017.
 * Used for key value pairs of Graphs
 */

public class GraphEntry
{
    private final Double x;
    private final Double y;

    /**
     *
     * @param aKey x value
     * @param aValue y value
     */
    public GraphEntry(Double aKey, Double aValue)
    {
        x   = aKey;
        y = aValue;
    }

    /**
     *
     * @return x value
     */
    public Double x()   { return x; }

    /**
     *
     * @return y value
     */
    public Double y() { return y; }
}