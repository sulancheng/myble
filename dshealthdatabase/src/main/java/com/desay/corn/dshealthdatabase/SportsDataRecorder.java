package com.desay.corn.dshealthdatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by corn on 2016/7/22.
 */
public class SportsDataRecorder {
    private static final String TAG = "wxf_Dolen_db";
    //month sports in history
    public static final String MONTH_SPORTS_TB_NAME = "month_sports_table";
    public static final String MONTH_SPORTS_FIELD_TIME = "month_sports_time";
    public static final String MONTH_SPORTS_FIELD_STEPS = "month_sports_steps";
    //week sports in history
    public static final String WEEK_SPORTS_TB_NAME = "week_sports_table";
    public static final String WEEK_SPORTS_FIELD_TIME = "week_sports_time";
    public static final String WEEK_SPORTS_FIELD_STEPS = "week_sports_steps";
    //day sports in history
    public static final String DAY_SPORTS_TB_NAME = "day_sports_table";
    public static final String DAY_SPORTS_FIELD_TIME = "day_sports_time";
    public static final String DAY_SPORTS_FIELD_STEPS = "day_sports_steps";


    public static final String ORIGINAL_SPORTS_TB_NAME = "original_sports_table";



    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_ADDRESS = "device_address";
    public static final String KEY_DEVICE_CONNECT_STATE = "device_connect_state";
    public static final String KEY_DEVICE_VERSIONS = "device_versions";
    public static final String KEY_DEVICE_LOGO_URI = "device_logo_uri";
    public static final String KEY_DEVICE_PRIORITY = "device_priority";


    private Context mContext;
    private SQLiteHelper dbHelper;
    private SQLiteDatabase db;

    public SportsDataRecorder(Context context) {
        mContext = context;
        dbHelper = SQLiteHelper.getInstance(mContext);
        db = dbHelper.getWritableDatabase();
    }


    private long LastDataTime = 0;
    public boolean addSportsData(SQLSportsData mSQLSportsData){
           if(LastDataTime==0){
               //get a recently data for calculating new data if last sports data ==null


           }

        return false;
    }


//
//
//
//
//
//    public boolean bindDevice(BLEDeviceInfo mBLEDiviceInfo, boolean overWrite) {// OK
//        Log.d(TAG, mBLEDiviceInfo.getDeviceAddress());
//        String deviceAddress =mBLEDiviceInfo.getDeviceAddress();
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//        Log.d(TAG, deviceAddress);
//        Log.d(TAG, "isTaskExist(deviceAddress)="+isDeviceExist(deviceAddress));
//
//        if (isDeviceExist(deviceAddress)) {
//            if (overWrite) {
////				return updateDiviceConnectState(mBLEDiviceInfo, State);   overWrite we do not need now
//            }
//            return false;
//        } else {
//            return bindDevice(mBLEDiviceInfo);
//        }
//    }
//
//    public boolean isDeviceExist(String deviceAddress) {// OK
//        Cursor cursor;
//        boolean result = false;
//        try {
//            deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//            Log.d(TAG, "isTaskExist = "+deviceAddress);
//            cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                    + " where " + KEY_DEVICE_ADDRESS + " = " + "'"+deviceAddress+"'", null);
//            result = cursor.getCount() > 0;
//            cursor.close();
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//        return result;
//    }
//
//
//    public String getDeviceLogoUri(String deviceAddress) {// OK
//        Cursor cursor;
//        String uri =null;
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + KEY_DEVICE_ADDRESS + " = " + "'"+deviceAddress+"'", null);
//        if(cursor.moveToFirst()){
//            uri = cursor.getString(cursor.getColumnIndex(BluetoothDeviceSQLOperate.KEY_DEVICE_LOGO_URI));
//        }
//        cursor.close();
//        return uri;
//    }
//
//    public int getDeviceVersions(String deviceAddress) {// OK
//        Cursor cursor;
//        int versions =0;
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + KEY_DEVICE_ADDRESS + " = " + "'"+deviceAddress+"'", null);
//        if(cursor.moveToFirst()){
//            versions = cursor.getInt(cursor.getColumnIndex(BluetoothDeviceSQLOperate.KEY_DEVICE_VERSIONS));
//        }
//        cursor.close();
//        return versions;
//    }
//
//    public String getDeviceName(String deviceAddress) {// OK
//        Cursor cursor;
//        String device_name = null;
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + KEY_DEVICE_ADDRESS + " = " + "'"+deviceAddress+"'", null);
//        if(cursor.moveToFirst()){
//            device_name = cursor.getString(cursor.getColumnIndex(BluetoothDeviceSQLOperate.KEY_DEVICE_NAME));
//        }
//        cursor.close();
//        return device_name;
//    }
//
//    public int getDeviceConnectState(String deviceAddress) {// OK
//        Cursor cursor;
//        int connectState = 0;
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + KEY_DEVICE_ADDRESS + " = " + "'"+deviceAddress+"'", null);
//        if(cursor.moveToFirst()){
//            connectState = cursor.getInt(cursor.getColumnIndex(BluetoothDeviceSQLOperate.KEY_DEVICE_CONNECT_STATE));
//        }
//        cursor.close();
//        return connectState;
//    }
//
//
//    private boolean bindDevice(BLEDeviceInfo mBLEDiviceInfo) {
//        // ����һ������
//        boolean result = false;
//        String deviceAddress =mBLEDiviceInfo.getDeviceAddress();
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//        ContentValues values = new ContentValues();
//        values.put(KEY_DEVICE_NAME, mBLEDiviceInfo.getDeviceName());
//        values.put(KEY_DEVICE_ADDRESS, deviceAddress);
//        values.put(KEY_DEVICE_CONNECT_STATE, mBLEDiviceInfo.getDeviceConnectState());
//        values.put(KEY_DEVICE_LOGO_URI, mBLEDiviceInfo.getDeviceLogoURI());
//        values.put(KEY_DEVICE_PRIORITY, mBLEDiviceInfo.getDevicePriority());
//        result = db.insert(SQLiteHelper.DEVICE_TB_NAME, null, values) > 0;
//        return result;// rowID
//    }
//
//    public boolean updateDiviceConnectState(String address,int State) {// OK
//        // ����һ�������״̬
//        address=address.replace(":", "-");//format the address for ":" can not be store,
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_DEVICE_CONNECT_STATE, State);
//        return db.update(SQLiteHelper.DEVICE_TB_NAME, values, KEY_DEVICE_ADDRESS + "=?",
//                new String[] { address + ""}) > 0;
//    }
//
//
//    public boolean updateDiviceLogoURI(String deviceAddress , String uri) {// OK
//        // ����һ�������״̬
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_DEVICE_LOGO_URI, uri);
//        return db.update(SQLiteHelper.DEVICE_TB_NAME, values, KEY_DEVICE_ADDRESS + "=?",
//                new String[] { deviceAddress + ""}) > 0;
//    }
//
//    public boolean updateDiviceName(String deviceAddress , String name) {// OK
//        // ����һ�������״̬
//        boolean result = false;
//        deviceAddress=deviceAddress.replace(":", "-");//format the address for ":" can not be store,
//
//        if(isDeviceExist(deviceAddress)){
//            ContentValues values = new ContentValues();
//            values.put(KEY_DEVICE_NAME, name);
//            result = db.update(SQLiteHelper.DEVICE_TB_NAME, values, KEY_DEVICE_ADDRESS + "=?",
//                    new String[] { deviceAddress + ""}) > 0;
//        }
//        return false;
//    }
//
//
//    public List<BLEDeviceInfo> getAllDevice(){//ȡ���������ݿ�����豸
//        Cursor cursor;
//        boolean exist = false;
//        List<BLEDeviceInfo> allDivices = new ArrayList<BLEDeviceInfo>() ;
//        // ֻ�ж�TASKSTATE_NOT_STARTED"δ��ʼ������", ����task_order����
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + "id" + ">0", null);
//        exist = cursor.getCount() > 0;
//        if(exist){
//            String name ;
//            String address ;
//            int state ;
//            int version;
//            String uri ;
//            int priority ;
//            BLEDeviceInfo mBLEDiviceInfo;
//
//            if(cursor.moveToFirst()){
//                for(int t=0;t<cursor.getCount();t++){
//                    name = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)) ;
//                    address=cursor.getString(cursor.getColumnIndex(KEY_DEVICE_ADDRESS));
//                    address=address.replace("-", ":");//format the address for ":" can not be store,
//
//                    state = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_CONNECT_STATE)) ;
//                    version = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_VERSIONS)) ;
//                    uri =  cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOGO_URI)) ;
//                    priority = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_PRIORITY));
//
//                    mBLEDiviceInfo = new BLEDeviceInfo(name,address,state,version,uri,priority,0);
//                    allDivices.add(mBLEDiviceInfo);
//
//                    cursor.moveToNext();
//                }
//            }
//        }
//
//        cursor.close();
//
//        return allDivices;
//    }
//
//
//    public List<BLEDeviceInfo> getAllDevice(int state){//ȡ���������ݿ�����豸
//        Cursor cursor;
//        boolean exist = false;
//        List<BLEDeviceInfo> allDivices = new ArrayList<BLEDeviceInfo>() ;
//        // ֻ�ж�TASKSTATE_NOT_STARTED"δ��ʼ������", ����task_order����
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + KEY_DEVICE_CONNECT_STATE + " = "+ state, null);
//        exist = cursor.getCount() > 0;
//        if(exist){
//            String name ;
//            String address ;
//            String uri ;
//            int version;
//            int priority ;
//            BLEDeviceInfo mBLEDiviceInfo;
//
//            if(cursor.moveToFirst())
//                for(int t=0;t<cursor.getCount();t++){
//                    name = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)) ;
//                    address=cursor.getString(cursor.getColumnIndex(KEY_DEVICE_ADDRESS));
//                    address=address.replace("-", ":");//format the address for ":" can not be store,
//
//                    version = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_VERSIONS)) ;
//                    state = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_CONNECT_STATE)) ;
//                    uri =  cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOGO_URI)) ;
//                    priority = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_PRIORITY));
//
//                    mBLEDiviceInfo = new BLEDeviceInfo(name,address,state,version,uri,priority,0);
//                    allDivices.add(mBLEDiviceInfo);
//
//                    cursor.moveToNext();
//                }
//        }
//
//        cursor.close();
//        return allDivices;
//    }
//
//
//    public List<BLEDeviceInfo> getAllDeviceDesc(int state){//ȡ���������ݿ�����豸
//        Cursor cursor;
//        boolean exist = false;
//        List<BLEDeviceInfo> allDivices = new ArrayList<BLEDeviceInfo>() ;
//        // ֻ�ж�TASKSTATE_NOT_STARTED"δ��ʼ������", ����task_order����
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + KEY_DEVICE_CONNECT_STATE + " = "+ state + " order by id desc", null);
//        exist = cursor.getCount() > 0;
//        if(exist){
//            String name ;
//            String address ;
//            String uri ;
//            int version;
//            int priority ;
//            BLEDeviceInfo mBLEDiviceInfo;
//
//            if(cursor.moveToFirst())
//                for(int t=0;t<cursor.getCount();t++){
//                    name = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)) ;
//                    address=cursor.getString(cursor.getColumnIndex(KEY_DEVICE_ADDRESS));
//                    address=address.replace("-", ":");//format the address for ":" can not be store,
//
//                    version = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_VERSIONS)) ;
//                    state = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_CONNECT_STATE)) ;
//                    uri =  cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOGO_URI)) ;
//                    priority = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_PRIORITY));
//
//                    mBLEDiviceInfo = new BLEDeviceInfo(name,address,state,version,uri,priority,0);
//                    allDivices.add(mBLEDiviceInfo);
//
//                    cursor.moveToNext();
//                }
//        }
//
//        cursor.close();
//        return allDivices;
//    }
//
//
//
//    public int getAllDeviceCount(){//ȡ���������ݿ�����豸
//        Cursor cursor;
//        // ֻ�ж�TASKSTATE_NOT_STARTED"δ��ʼ������", ����task_order����
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME, null);
//        return cursor.getCount();
//    }
//
//
//
//
//    public BLEDeviceInfo getDeviceInfo(String address){//ȡ���������ݿ�����豸
//        Cursor cursor;
//        boolean exist = false;
//        BLEDeviceInfo mBLEDiviceInfo = null;
//        address=address.replace(":", "-");//format the address for ":" can not be store,
//        // ֻ�ж�TASKSTATE_NOT_STARTED"δ��ʼ������", ����task_order����
//        cursor = db.rawQuery("select * from " + SQLiteHelper.DEVICE_TB_NAME
//                + " where " + KEY_DEVICE_ADDRESS + " = "+ "'"+address+ "'", null);
//        exist = cursor.getCount() > 0;
//        if(exist){
//            String name ;
//            int version ;
//            int state ;
//            String uri ;
//            int priority ;
//
//            if(cursor.moveToFirst()){
//                name = cursor.getString(cursor.getColumnIndex(KEY_DEVICE_NAME)) ;
//                address=cursor.getString(cursor.getColumnIndex(KEY_DEVICE_ADDRESS));
//                address=address.replace("-", ":");//format the address to ":"
//
//                version = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_VERSIONS)) ;
//                state = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_CONNECT_STATE)) ;
//                uri =  cursor.getString(cursor.getColumnIndex(KEY_DEVICE_LOGO_URI)) ;
//                priority = cursor.getInt(cursor.getColumnIndex(KEY_DEVICE_PRIORITY));
//
//                mBLEDiviceInfo = new BLEDeviceInfo(name,address,state,version,uri,priority,0);
//            }
//            cursor.close();
//        }
//
//        return mBLEDiviceInfo;
//    }


}
