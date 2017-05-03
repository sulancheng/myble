package com.desay.corn.blelab;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by corn on 2016/6/28.
 */
public class BLEContentProvider {
    public static final UUID SERVER_B_UUID_SERVER = UUID.fromString("0000190B-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVER_B_UUID_REQUEST = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");//WRITE
    public static final UUID SERVER_B_UUID_NOTIFY = UUID.fromString("00000004-0000-1000-8000-00805f9b34fb");//NOTIFY
    //ota Attributes
    public static final UUID SERVER_A_UUID_SERVER = UUID.fromString("0000190a-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVER_A_UUID_REQUEST = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");//WRITE
    public static final UUID SERVER_A_UUID_NOTIFY = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");//NOTIFY
    public final static UUID SERVER_A_UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int SERVER_A_UUID_REQUEST_INT = 1;// correspond OTA_UUID_REQUEST
    public static final int SERVER_B_UUID_REQUEST_INT = 3;//correspond UUID_REQUEST


    public static final int SNSTTPE_CALL = 0;//来电提醒
    public static final int SNSTTPE_SMS = 1;//短信提醒
    public static final int SNSTTPE_QQ = 2;
    public static final int SNSTTPE_WECHAT = 3;
    public static final int SNSTTPE_FACEBOOK = 4;
    public static final int SNSTTPE_TWITTER = 5;
    public static final int SNSTTPE_WHATSAPP = 6;
    public static final int SNSTTPE_INSTAGRAM = 7;
    public static final int SNSTTPE_EMAIL = 8;
    public static final int SNSTTPE_LINE = 9;
    public static final int SNSTTPE_SKYPE = 10;

    public static final int DATA_STEPS = 0;
    public static final int DATA_HEARTRATE = 3;
    public static final int DATA_SLEEP = 7;


    public static int ENGLISH = 0;
    public static int CHINESE = 1;


    public static int HANDUP_CLOSE = 0;
    public static int HANDUP_AUTO = 1;
    public static int HANDUP_LEFT = 2;
    public static int HANDUP_RIGHT = 3;

    public static final int BAND_UNITS_MILE = 0;
    public static final int BAND_UNITS_KILO = 1;


    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTED_FAIL = 3;
    public static final int STATE_AUTO_CONNECTING = 4;

//    //music control notify
    private static int CURRENT_NOTFY_MUSIC_CMD = 0;
//    public static final int NT_MUSIC_DEFAULT = 0;
    public static final int NT_MUSIC_PLAY = 1;
    public static final int NT_MUSIC_PAUSE = 2;
    public static final int NT_MUSIC_NEXT = 3;
    public static final int NT_MUSIC_PRE = 4;

    //data
    //sports data
    private static List<SportsData> sportsDataList = new ArrayList<>() ;
    public static void addSportsData(SportsData mSportsData){
        if(!(mSportsData.startTime<0||mSportsData.sportsLong<0||mSportsData.steps==0)){
            sportsDataList.add(mSportsData);
        }
    }
    public static List<SportsData> getSportsData(){
        List<SportsData> DataList = new ArrayList<>();
        DataList.addAll(sportsDataList);
        sportsDataList.removeAll(sportsDataList);
        DesayLog.d("DataList = " + DataList.size());
        return DataList;
    }
    //heartRate data
    private static List<HeartRateData> heartRateDataList = new ArrayList<>() ;
    public static void addHeartRateData(HeartRateData mHeartRateData){
        if(!(mHeartRateData.Test_Time<0||mHeartRateData.Heart_Rate<0)){
            heartRateDataList.add(mHeartRateData);
        }
    }
    public static List<HeartRateData> getHeartRateData(){
        List<HeartRateData> DataList = new ArrayList<>();
        DataList.addAll(heartRateDataList);
        heartRateDataList.removeAll(heartRateDataList);
        DesayLog.d("DataList" + DataList.size());
        return DataList;
    }
    //sleep data 还需要做睡眠算法过滤
    private static List<SleepCycle> sleepDataList = new ArrayList<>() ;
    public static void addSleepData(SleepCycle mSleepCycle){
        if(!(mSleepCycle.Start_Time<0||mSleepCycle.Light_Sleep_Long<0||mSleepCycle.Deep_Sleep_Long<0||mSleepCycle.Sleep_Long<0)){
            sleepDataList.add(mSleepCycle);
        }
        DesayLog.d("mSleepCycle  Start_Time=" + mSleepCycle.Start_Time +" Deep_Sleep_Long="
                +mSleepCycle.Deep_Sleep_Long +" Light_Sleep_Long=" +mSleepCycle.Light_Sleep_Long);
    }
    public static List<SleepCycle> getSleepData(){
        List<SleepCycle> DataList = new ArrayList<>();
        DataList.addAll(sleepDataList);
        DesayLog.d("DataList" + DataList.size());
        sleepDataList.removeAll(sleepDataList);
        return DataList;
    }

}
