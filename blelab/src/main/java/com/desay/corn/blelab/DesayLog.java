package com.desay.corn.blelab;

import android.util.Log;

/**
 * Created by corn on 2016/7/18.
 */
public class DesayLog {
    private static final String TAG = "sdk_debug";
    private static boolean DEBUG = true;
    public static void d(String msg) {
        if(DEBUG){
            Log.i(TAG, msg);
        }
    }

    public static void e(String msg) {
        if(DEBUG){
            Log.e(TAG, msg);
        }
        }
}
