package com.zetta.android;

/**
 * Created by Greg on 8/23/2017.
 * Used for key value pairs of Graphs
 */

public class GraphEntry
{
    private final Float x;
    private final Float y;

    /**
     *
     * @param aKey x value
     * @param aValue y value
     */
    public GraphEntry(Float aKey, Float aValue)
    {
        x   = aKey;
        y = aValue;
    }

    /**
     *
     * @return x value
     */
    public Float x()   { return x; }

    /**
     *
     * @return y value
     */
    public Float y() { return y; }
}