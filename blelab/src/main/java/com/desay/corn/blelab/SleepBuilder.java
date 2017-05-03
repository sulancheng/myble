package com.desay.corn.blelab;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by corn on 2016/7/8.
 */
public class SleepBuilder {
    //睡眠周期，用于计算
    private int SLEEP_PERIOD = 90;//90min
    private static final int SLEEP_STATE_START = 101;
    private static final int SLEEP_STATE_END = 103;
    private static int currentBuilderState = SLEEP_STATE_END;
    private static long startTime = 0;

    //sleep data 还需要做睡眠算法过滤
    private static List<SleepData> sleepDataList = new ArrayList<>();
    public static void buildSleepData(SleepData mSleepData){
        DesayLog.d("mSleepData.Sleep_Type = " + mSleepData.Sleep_Type + ",currentBuilderState="+currentBuilderState);
            switch(currentBuilderState){
                case SLEEP_STATE_START://如果当前已经开始了睡眠计算
                    if(mSleepData.Sleep_Type==SleepData.SLEEP_TYPE_SLEEP_NODE){
                        //已经开始了，又来了一个节点，说明当前是个END
                        if(sleepDataList.size()<3){//如果里边只有20分钟以内的数据，则为无效
                            sleepDataList.removeAll(sleepDataList);
                        }else{//计算是否为睡眠
                            buildSleep();
                        }
                        currentBuilderState = SLEEP_STATE_END;
                    }else{
                        sleepDataList.add(mSleepData);
                    }
                    break;
                case SLEEP_STATE_END://等待起始点
                    if(mSleepData.Sleep_Type==SleepData.SLEEP_TYPE_SLEEP_NODE){
                        startTime = mSleepData.Start_Time;
                        currentBuilderState = SLEEP_STATE_START;
                    }
                    break;
            }
    }

    private static void buildSleep(){
        int deepSlp = 0;
        int lightSlp = 0;
        //get the sleep data
        for(SleepData mSleepData:sleepDataList){
            if(mSleepData.Sleep_Type==SleepData.SLEEP_TYPE_DEEP_SLEEP){
                deepSlp = deepSlp+mSleepData.Sleep_Long;
            }else if(mSleepData.Sleep_Type==SleepData.SLEEP_TYPE_LIGHT_SLEEP){
                lightSlp = lightSlp+mSleepData.Sleep_Long;
            }
        }

        double lightSlpModulus = lightSlp*1.0/(deepSlp+lightSlp)*1.0;
        DesayLog.d("lightSlpModulus=" + lightSlpModulus);
        if(lightSlpModulus>0.3){//if deep sleep too more, it is invalid slp,so we just consider the normal slp
            long start_time = 0;
            if(startTime!=0){
                start_time = startTime;
            }else{
                start_time = sleepDataList.get(0).Start_Time;
            }
        SleepCycle mSleepCycle = new SleepCycle(start_time,deepSlp,lightSlp);
        BLEContentProvider.addSleepData(mSleepCycle);
        }
        //remove all data
        sleepDataList.removeAll(sleepDataList);
    }

    /*时间戳转换成字符窜*/
    private static String getDateToString(long time) {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(time));
    }

}
