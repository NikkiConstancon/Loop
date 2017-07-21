package com.reva.loop301.reva;

/**
 * Created by Hristian Vitrychenko on 08/07/2017.
 */

public class Statistical {
    private int img_id;
    private String aboveText, minimum, maximum, average;

    public Statistical(int img, String aText, String min, String max, String avg)
    {
        setImg_id(img);
        setAboveText(aText);
        setMinimum(min);
        setMaximum(max);
        setAverage(avg);
    }

    public int getImg_id() {
        return img_id;
    }

    public String getAboveText() {
        return aboveText;
    }

    public String getMinimum() {
        return minimum;
    }

    public String getMaximum() {
        return maximum;
    }

    public String getAverage() {
        return average;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }

    public void setAboveText(String aboveText) {
        this.aboveText = aboveText;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    public void setAverage(String average) {
        this.average = average;
    }
}
