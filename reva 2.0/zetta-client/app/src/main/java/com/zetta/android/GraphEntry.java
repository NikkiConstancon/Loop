package com.zetta.android;

import java.io.Serializable;

/**
 * Created by Greg on 8/23/2017.
 * Used for key value pairs of Graphs
 */

public class GraphEntry implements Serializable
{
    private Float x;
    private Float y;

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

    public GraphEntry() {
        this(0f,0f);
    }

    public void setX(Float x) {
        this.x = x;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }
}