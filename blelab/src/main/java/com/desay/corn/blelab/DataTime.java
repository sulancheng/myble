package com.desay.corn.blelab;

/**
 * Created by corn on 2016/7/7.
 */
public class DataTime {
    public int timeAttri = -1;
    public long time = 0;
    DataTime(int attri,long time){
        this.timeAttri = attri;
        this.time = time;
    }

    public String getTime(long timestamp){
        return new java.text.SimpleDateFormat("yyyy/MM/DD HH:mm:ss").format(new java.util.Date(timestamp * 1000));
    }

}
