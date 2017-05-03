package com.desay.corn.blelab;

/**
 * Created by corn on 2016/7/7.
 */
public class HeartRateData {
    public long Test_Time=0;
    public int Heart_Rate =  0;
    HeartRateData(DataTime time,int rate){
        this.Test_Time = time.time - (DsBluetoothConnector.TimeZoneOffset);
        this.Heart_Rate = rate;
    }
}
