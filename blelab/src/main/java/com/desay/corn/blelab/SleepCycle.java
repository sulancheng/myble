package com.desay.corn.blelab;

/**
 * Created by corn on 2016/7/8.
 */
public class SleepCycle {
    public long Start_Time = 0;//睡眠起始时间 时间戳格式 单位为秒
    public int Deep_Sleep_Long =  0;//深度睡眠时长 单位为秒
    public int Light_Sleep_Long =  0;//浅睡眠时长 单位为秒
    public int Sleep_Long =  0;//总的睡眠时长 单位为秒  PS： Sleep_Long= Deep_Sleep_Long+Light_Sleep_Long
    SleepCycle(long start_time,int deep_slp_time,int light_slp_time){
        this.Sleep_Long = deep_slp_time+light_slp_time;
        this.Deep_Sleep_Long = deep_slp_time;
        this.Light_Sleep_Long = light_slp_time;
        this.Start_Time = start_time;
    }
}
