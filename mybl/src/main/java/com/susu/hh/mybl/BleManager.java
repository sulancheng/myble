package com.susu.hh.mybl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by Administrator on 2016/11/23.
 */
public class BleManager {
    private BleService mblservice;
    private static Context mcontext;
    private static BleManager instance;
    private static String mblename;
    //private static Iservice iservice;
    private MyServiceConn conn = new MyServiceConn();


    public static synchronized BleManager getBleManagInstance(){
        if (null == instance) {
            return null;
        }
        return instance;
    }
    //只能初始化用
    public static synchronized BleManager getInstanceandName(Context context,BleContror.OnBLECallBackListener listener) {
        mcontext = context;
        instance = new BleManager(listener);
        return instance;
    }
    private void setMblename(String mblename){
        MyLog.i("setMblename "," setMblename = "+mblename);
        this.mblename = mblename;
    }
    private BleContror.OnBLECallBackListener listener;
    private BleManager(BleContror.OnBLECallBackListener listener) {
        this.listener = listener;
        init();
    }

    private void init() {
        Intent service = new Intent(mcontext, BleService.class);
        try {
            mcontext.bindService(service, conn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
        }
    }

    public void release(){
        if (BleContrParter.getBleContrpartInstance().getState() == BleContror.BleZt.STATE_CONNECTED) {
            disconnect();
        }
        if(mblservice != null){
            MyLog.e("解绑服务");
            mcontext.unbindService(conn);
        }
        instance = null;
    }
 /*   public BleService getService() {
        if (mblservice != null) {
           return mblservice;
        }
        return null;
    }*/

    class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //mblservice = (Iservice) service;
            BleService.MyBinder mBinder = (BleService.MyBinder) service;
            mblservice = mBinder.getService();
            //test

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
    public synchronized void startNewControl(String blename){
        if(mblservice!=null){
            MyLog.i("服务开始关联contrl并设置监听");
            mblservice.getblecontr(blename);
            mblservice.setBleContrParter(listener,blename);
        }
    }

    /*   public void test(){
           if(iservice !=null){
               iservice.callTest();
           }
       }
       public void scanLeDevice(boolean enable,long SCAN_PERIOD){
           if(iservice !=null){
               iservice.callScanLeDevice(enable,SCAN_PERIOD);
           }
       }*/
//    public void test() {
//        if (mblservice != null) {
//            mblservice.scanLeDevice(true,5000);
//        }
//    }
    public void connect(String adress){
        if (mblservice != null) {
            mblservice.connect(adress);
        }
    }

    public void disconnect(){
        if (mblservice != null) {
            mblservice.disconnect();
        }
    }
//    public void checkBandVersion() {
//        if (mblservice != null) {
//            mblservice.checkBandVersion();
//        }
//    }
//    public void bindMyBand(){
//        /*对于指令可以 放入到一个集合，之后发送空消息至handle
//        从集合中拿出第一个元素进入写入，可设置超时，时间超时则从集合删除指令。*/
//        if(mblservice!=null){
//            mblservice.bindMyBand();
//        }
//    }
//    public void getBytt() {
//        if (mblservice != null) {
//            mblservice.getBytt();
//        }
//    }
}
