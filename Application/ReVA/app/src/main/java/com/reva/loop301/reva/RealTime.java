package com.reva.loop301.reva;

/**
 * Created by Hristian Vitrychenko on 07/07/2017.
 */

/**
 * Real time class developed to hold real time patient data received
 * from the server.
 */
public class RealTime {

    /**
     * Constructor method for the RealTime class
     * @param img stores image index
     * @param aText stores above text of data
     * @param val stores the value of the data
     * @param exVal stores possible extra value used for specialised data
     */
    public RealTime(int img, String aText, String val, String exVal)
    {
        this.setImg_id(img);
        this.setAboveText(aText);
        this.setValue(val);
        this.setExtVal(exVal);
    }

    private int img_id;
    private String aboveText, value, extVal;

    /**
     * getter method for image index
     * @return image index
     */
    public int getImg_id() {
        return img_id;
    }

    /**
     * getter for above text
     * @return above text of real time data
     */
    public String getAboveText() {
        return aboveText;
    }

    /**
     * getter for value of real time data
     * @return value of real time data
     */
    public String getValue() {
        return value;
    }

    /**
     * getter for possible extra value
     * @return possible extra value
     */
    public String getExtVal() {
        return extVal;
    }

    /**
     * setter for image index
     * @param img_id holds image index
     */
    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }

    /**
     * setter for above text of real time data
     * @param aboveText holds above text of real time data
     */
    public void setAboveText(String aboveText) {
        this.aboveText = aboveText;
    }

    /**
     * setter for value of real time data
     * @param value holds value of real time data
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * setter for possible extra value
     * @param extVal holds possible extra value for specialised data
     */
    public void setExtVal(String extVal) {
        this.extVal = extVal;
    }
}
