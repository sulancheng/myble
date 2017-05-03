package com.desay.corn.blelab;


/**
 * Created by corn on 2016/7/7.
 */
public class SleepData {
    public long Start_Time = 0;
    public int Sleep_Type =  0;
    public int Sleep_Long =  0;
    public static int SLEEP_TYPE_SLEEP_NODE = 0;
    public static int SLEEP_TYPE_LIGHT_SLEEP = 11;
    public static int SLEEP_TYPE_DEEP_SLEEP = 12;
    private int Time_offset = 10;

    SleepData(DataTime time,int sleep_type){
    if(sleep_type == SLEEP_TYPE_SLEEP_NODE){//说明是一个时间节点
        this.Start_Time = time.time - (DsBluetoothConnector.TimeZoneOffset);
        this.Sleep_Long = 0;
    }else{
        this.Start_Time = (time.time-Time_offset*60 - (DsBluetoothConnector.TimeZoneOffset));
        this.Sleep_Long = Time_offset*60;
    }
    this.Sleep_Type = sleep_type;
    }
}
