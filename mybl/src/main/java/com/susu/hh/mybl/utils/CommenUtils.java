package com.susu.hh.mybl.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.widget.Toast;

import com.susu.hh.mybl.MyLog;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by Administrator on 2016/5/3.
 * <p>
 * 工具类：
 */
public class CommenUtils {

    private static Toast sToast;

    public static float dip2px(Context context, float value) {

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static Handler getHandler() {

        return sHandler;
    }

    public static void showSafeToast(final Context context, final String text) {

        sHandler.post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }


    public static void showSingleToast(Context context, String text) {

        if (sToast == null) {

            sToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        }

        sToast.setText(text);
        sToast.show();
    }

    public static boolean isEn(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("en"))
            return true;
        else
            return false;
    }

    public static void sendBroadCast(Context mContext, String action, String content) {
       /* String str = "foo";
        b = ( str instanceof String );   // true
        b = ( str instanceof Object );   // also true
        b = ( str instanceof Date );     // false, not a Date or subclass*/
        Intent intent = new Intent(action);
        intent.putExtra("key", content);
        mContext.sendBroadcast(intent);
    }

    public static void registBroad(Context mContext, String[] actions, BroadcastReceiver receiver) {
        if (actions.length > 0) {
            for (int i= 0; i < actions.length;i++){
                MyLog.i("registBroad","注册广播："+actions[i]);
                registBroad(mContext,actions[i],receiver);
            }
        }
    }

    public static void registBroad(Context mContext, String action, BroadcastReceiver receiver) {
        MyLog.i("registBroad","注册广播："+action);
        IntentFilter filter = new IntentFilter(action);
        //LocalBroadcastManager.getInstance(mContext).registerReceiver(receiver, filter);
        mContext.registerReceiver(receiver, filter);
    }


    /**
     * 把毫秒转换成：1:20:30这里形式
     *
     * @param timeMs
     * @return
     */
    public static String stringForTime(int timeMs) {
        final DecimalFormat decimalFormat2 = new DecimalFormat("00");
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;

        int minutes = (totalSeconds / 60) % 60;

        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return decimalFormat2.format(hours) + ":" + decimalFormat2.format(minutes) + ":" + decimalFormat2.format(seconds);
        } else {
            return decimalFormat2.format(minutes) + ":" + decimalFormat2.format(seconds);
        }
    }

    /**
     * 判断是否是网络的资源
     *
     * @param uri
     * @return
     */
    public static boolean isNetUri(String uri) {
        boolean reault = false;
        if (uri != null) {
            if (uri.toLowerCase().startsWith("http") || uri.toLowerCase().startsWith("rtsp") || uri.toLowerCase().startsWith("mms")) {
                reault = true;
            }
        }
        return reault;
    }

    /**
     * 得到网络速度
     * 每隔两秒调用一次
     *
     * @param context
     * @return
     */
    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;

    public static String getNetSpeed(Context context) {
        String netSpeed = "0 kb/s";
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB;
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        netSpeed = String.valueOf(speed) + " kb/s";
        return netSpeed;
    }
}
