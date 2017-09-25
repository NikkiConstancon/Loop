package com.zetta.android.browse;

import com.zetta.android.StatItem;

/**
 * Created by Greg on 8/23/2017.
 * This class is used for holding the data needed by simple statistic
 * entries, e.g., heart-rate average for the last week for the
 * StatListAdapter
 */
public class SimpleStatItem implements StatItem {
    protected String deviceName;
    protected String imgURL;
    protected String statName;
    protected String start;
    protected String end;
    protected String units;
    private double statistic;

    /**
     * Constructs the SimpleStatItem, usually used in the list for
     * the RecyclerView
     * @param deviceName the name of the device, e.g., Heart-rate
     * @param imgURL the URL for the image of the device
     * @param statName the name of the stat, e.g., average or line-graph
     * @param start the start of the period for the statistic
     * @param end the end of the period for the statistic
     * @param units the units of measurement used for the particular device
     * @param statistic the actual statistic
     */
    public SimpleStatItem(String deviceName,
                          String imgURL,
                          String statName,
                          String start,
                          String end,
                          String units,
                          double statistic) {
        this.statistic = statistic;
        this.deviceName = deviceName;
        this.imgURL = imgURL;
        this.statName = statName;
        this.start = start;
        this.end = end;
        this.units = units;
    }

    /**
     * Gets the device name
     * @return the device name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     *
     * @return the icon url
     */
    public String getImgURL() {
        return imgURL;
    }

    /**
     *
     * @return the statistic name, e.g.,  average or line-graph
     */
    public String getStatName() {
        return statName;
    }

    /**
     *
     * @return the start of the statistic period
     */
    public String getStart() {
        return start;
    }

    /**
     *
     * @return the end of the statistic period
     */
    public String getEnd() {
        return end;
    }

    /**
     *
     * @return the units of measurement of the device
     */
    public String getUnits() {
        return units;
    }

    /**
     *
     * @return the value of the statistic for this item
     */
    public double getStatistic() {
        return statistic;
    }

    /**
     *
     * @return the device type (SIMPLE STAT)
     */
    public int getType() {
        return TYPE_SIMPLE_STAT;
    }
}
