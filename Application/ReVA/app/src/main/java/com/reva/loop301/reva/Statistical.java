package com.reva.loop301.reva;

/**
 * Created by Hristian Vitrychenko on 08/07/2017.
 */

/**
 * Statistics class developed to store Statistical patient data received from the server
 */
public class Statistical {
    private int img_id;
    private String aboveText, minimum, maximum, average;

    /**
     * Constructor for the Statistical class
     * @param img holds the image index
     * @param aText holds the above text
     * @param min holds the statistically minimum value
     * @param max holds the statistically maximum value
     * @param avg holds the statistically average value
     */
    public Statistical(int img, String aText, String min, String max, String avg)
    {
        setImg_id(img);
        setAboveText(aText);
        setMinimum(min);
        setMaximum(max);
        setAverage(avg);
    }

    /**
     * getter for the image index of the statistical data
     * @return image index
     */
    public int getImg_id() {
        return img_id;
    }

    /**
     * getter for the above text of the statistical data
     * @return above text
     */
    public String getAboveText() {
        return aboveText;
    }

    /**
     * getter for the statistically minimum value of the data
     * @return minimum value
     */
    public String getMinimum() {
        return minimum;
    }

    /**
     * getter for the statistically maximum value of the data
     * @return maximum value
     */
    public String getMaximum() {
        return maximum;
    }

    /**
     * getter for the statistically avergae value
     * @return average value
     */
    public String getAverage() {
        return average;
    }

    /**
     * setter for the image index of the statistical data
     * @param img_id holds the image index
     */
    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }

    /**
     * setter for the above text of the statistical data
     * @param aboveText holds the above text
     */
    public void setAboveText(String aboveText) {
        this.aboveText = aboveText;
    }

    /**
     * setter for the statistically minimum value of the statistical data
     * @param minimum holds the minimum
     */
    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    /**
     * setter for the statistically maximum value of the statistical data
     * @param maximum holds the maximum value
     */
    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    /**
     * setter for the statistically average value of the statistical data
     * @param average holds the average value
     */
    public void setAverage(String average) {
        this.average = average;
    }
}
