package com.reva.loop301.reva;

/**
 * Created by Hristian Vitrychenko on 07/07/2017.
 */

public class RealTime {

    public RealTime(int img, String aText, String val, String exVal)
    {
        this.setImg_id(img);
        this.setAboveText(aText);
        this.setValue(val);
        this.setExtVal(exVal);
    }

    private int img_id;
    private String aboveText, value, extVal;

    public int getImg_id() {
        return img_id;
    }

    public String getAboveText() {
        return aboveText;
    }

    public String getValue() {
        return value;
    }

    public String getExtVal() {
        return extVal;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }

    public void setAboveText(String aboveText) {
        this.aboveText = aboveText;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setExtVal(String extVal) {
        this.extVal = extVal;
    }
}
