package com.susu.hh.mybl.alarmtest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by Administrator
 * on 2017/2/24.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private Context mContext;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mContext.sendBroadcast(new Intent(mContext,AlarmReceiver.class));
        }
    };
    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
            //设置通知内容并在onReceive()这个函数执行时开启
        Toast.makeText(context, "闹铃响了, 可以做点事情了~~", Toast.LENGTH_LONG).show();
        //mHandler.sendEmptyMessageDelayed(1,5000);
    }

    public PendingIntent getDefalutIntent(int flags,Context mContext){
        PendingIntent pendingIntent= PendingIntent.getActivity(mContext, 1, new Intent(), flags);
        return pendingIntent;
    }

}

