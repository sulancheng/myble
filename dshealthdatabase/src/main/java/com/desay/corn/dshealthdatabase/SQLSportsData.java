package com.desay.corn.dshealthdatabase;

/**
 * Created by corn on 2016/7/22.
 */
public class SQLSportsData {

    private long data_start_time ;
    private int data_sports_time;
    private int data_steps;

    public SQLSportsData(long data_start_time,int data_sports_time,int data_steps) {
        this.data_start_time = data_start_time;
        this.data_sports_time = data_sports_time;
        this.data_steps = data_steps;
    }

}
