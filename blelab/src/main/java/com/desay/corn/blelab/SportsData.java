package com.desay.corn.blelab;

/**
 * Created by corn on 2016/7/7.
 */
public class SportsData {
    public long startTime = 0; //睡眠起始时间 时间戳格式 单位为秒
    public int sportsLong = 0; //运动时长 单位为秒
    public int steps =  0;//sportsLong时长内运动的步数
    private int time_offset = 35;//SECONDS

    SportsData(DataTime time,int sports_data){

        if(time.time%300 == time_offset){//Auto Store data
            this.startTime = time.time - 300 - (DsBluetoothConnector.TimeZoneOffset);
            sportsLong = 300;
            if(this.startTime<0){
                this.startTime = 0;
            }
        }else{//user Store data
            sportsLong = (int) ((time.time-time_offset)%300);
            this.startTime = time.time-sportsLong - (DsBluetoothConnector.TimeZoneOffset);
            if(this.startTime<0){
                this.startTime = 0;
            }
            if(sportsLong<0){
                sportsLong = 0;
            }
        }
        steps = sports_data;
    }
}
