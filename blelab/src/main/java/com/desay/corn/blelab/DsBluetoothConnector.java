package com.desay.corn.blelab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by corn on 2016/6/27.
 */
public class DsBluetoothConnector {
    private static boolean DEBUG = true;
    public void setDEBUG(boolean debug){
        this.DEBUG = debug;
    }
    public static boolean SLEEP_INTEGRATE = false;
    public void setSleepIntegrate(boolean Integrate){
        this.SLEEP_INTEGRATE = Integrate;
    }

    private int Current_Device = NordicSeriesDevices.NORDIC_DEVICE;


    public interface OnBLECallBackListener{
        void OnConnectCallBack(int event,int status);
        /**
         *  event 事件号，映射如下KEY_BAND_VERSION等值,你可以在DsBluetoothConnector查询到这些值
         *  value 返回值，例如返回版本号
         *  status 返回操作状态，操作成功或者操作失败
         *  state 当前指令状态，例如关闭找手机功能，ENABLE = 1； DISABLE = 0;
         *        同时，state在特定的事件中也用于返回int值，例如查询步数，此时返回值state代表返回的步数
         *        再例如设置当前手环语言，state代表着当前手环已经设置的语言
         *
         */
        void OnCallBack(int event,boolean status,String value);
        void OnCallBack(int event,boolean status,int state);
        /**
         * @param event  事件号，映射如下KEY_BAND_VERSION等值
         * @param mSleepCycleList 睡眠数据List
         *
         */
        void OnSleepDataCallBack(int event,List<SleepCycle> mSleepCycleList);

        /**
         * @param event 事件号，映射如下KEY_BAND_VERSION等值
         * @param mHeartRateDataList 心率数据List
         */
        void OnHeartRateDataCallBack(int event,List<HeartRateData> mHeartRateDataList);

        /**
         * @param event 事件号，映射如下KEY_BAND_VERSION等值
         * @param mSportsData 运动数据List
         */
        void OnSportsDataCallBack(int event,List<SportsData> mSportsData);

        /**
         * @param event 事件号
         */
        void OnCameraShotCallBack(int event);

        /**
         * @param event 事件号
         * @param cmd 音乐指令，你可以在BLEContentProvider里找到这些指令对应的映射
         */
        void OnMusicCtrolCallBack(int event,int cmd);

        /**
         * @param event 事件号
         */
        void OnFindPhoneCallBack(int event);
    }

    OnBLECallBackListener mOnBLECallBackListener=null;
    public void setOnBLECallBackListener(OnBLECallBackListener listener){
        mOnBLECallBackListener=listener;
    }


    private static DsBluetoothConnector Instance = null;
    private Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = BLEContentProvider.STATE_DISCONNECTED;

    protected static final int GATT_CMD = 1011;
    protected static final int ENABLE_GATT_NOTIFY = 1012;
    protected static final int CHARA_CHANGED = 1013;
    protected static final int DATA_ANALYST = 1014;
    protected static final int PUSH_CH_WORDS = 1015;
    protected static final int PHONE_CALL = 1016;
    protected static final int FIND_BAND = 1017;
    protected static final int ON_LISTEN_CONNECT_STATE = 1018;
    protected static final int ON_LISTEN_BOND_STATE = 1019;

    protected static final String EXTRA_BYTES = "extra_byte";
    protected static final String EXTRA_INT = "extra_int";
    protected static final String EXTRA_STRING = "extra_string";
    private static int DATA_LENGTH = 20;
    private void addCMD(String cmdCode,int write_UUID){//举例设置闹钟。进行了分包
        if(synDataStart){//synchronizing data, prohibit writing data to avoid data confusion
            return;
        }
        if(Instance != null && cmdCode !=null){
            cmdCode = cmdCode +"\r\n";//add the end flag
            int strLength = cmdCode.length();
            for (int i = 0; i < strLength;) {
                int end =  i+DATA_LENGTH;
                if((i+DATA_LENGTH)>strLength){
                    end = strLength;
                }
                Log.i("cmdCode.substring", cmdCode.substring(i, end));
                byte[] writeByte =  cmdCode.substring(i, end).getBytes();
                sendCMD(writeByte,write_UUID);
                i = i + DATA_LENGTH;
            }
        }
    }

    //cmd bytes
    private void pushBytes(byte[] bytes,int write_UUID){
        if(synDataStart){//synchronizing data, prohibit writing data to avoid data confusion
            return;
        }
        bytes = addEndFlag(bytes);
        if(Instance != null && bytes !=null){
            int strLength = bytes.length;
            for (int i = 0; i < strLength;) {
                int end =  i+DATA_LENGTH;
                if((i+DATA_LENGTH)>strLength){
                    end = strLength;
                }
                byte[] writeByte =  new byte[(end-i)];
                System.arraycopy(bytes, i, writeByte, 0, (end-i));
                DesayLog.d("i = " + i + ",end=" + end);
                sendCMD(writeByte,write_UUID);
                i = i + DATA_LENGTH;
            }
        }
    }

    private byte[] addEndFlag(byte[] data1){
        byte[] data2 = "\r\n".getBytes();
        byte[] data3 = new byte[data1.length+data2.length];
        System.arraycopy(data1,0,data3,0,data1.length);
        System.arraycopy(data2,0,data3,data1.length,data2.length);
        DesayLog.d("data3 = " + data3.length);
        return data3;
    }

    ArrayList<byte[]> PUSH_BYTES = new ArrayList<byte[]>();
    private int MSG_TYPE = 0;//for phone call cn
    private void addCH(String cn_code,int msgType){
        try{
            PUSH_BYTES = generateCallerName(cn_code);
            MSG_TYPE = msgType;
            DesayLog.d("PUSH_BYTES.size() = " + PUSH_BYTES.size());
        }catch (Exception e){
            DesayLog.d("addCH e = " + e);
        }
    }

    private void pushCH(){
        if(PUSH_BYTES.size()!=0){
            DesayLog.d("PUSH_BYTES.size() = " + PUSH_BYTES.size() + "，PUSH_BYTES.get(0).size = "+ PUSH_BYTES.get(0).length);
            pushBytes(PUSH_BYTES.get(0),BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
            PUSH_BYTES.remove(0);
        }
    }

    private ArrayList<byte[]> generateCallerName(String str) throws IOException {
        char[] chars = str.toCharArray();
        ArrayList<byte[]> bytes = new ArrayList<byte[]>();
        for (char c : chars) {
            bytes.add(MatrixUtil.unicode2Bytes(mContext, c));
        }
        return bytes;
    }

    private void sendCMD(byte[] mbyte,int write_UUID){//通过handle发送指令
        Bundle mBundle = new Bundle();
        Message msg = Message.obtain(writeValueHandler, GATT_CMD);
        mBundle.putByteArray(EXTRA_BYTES, mbyte);
        mBundle.putInt(EXTRA_INT, write_UUID);
        msg.setData(mBundle);
        msg.sendToTarget();
        //如果是int
        //writeValueHandler.obtainMessage(GATT_CMD,int a ,0).sendToTarget();
    }


    public int getConnectState(){
        return mConnectionState;
    }

    //给外部的接口，需要设置bond的address，这样的话，我才能resetn内部的绑定状态
//    private String bond_address = "";
//    public void setBondAddress(String address){
//        bond_address = address;
//    }



//    protected static final String EXTRA_RSSI = "RSSI";
//    protected static final String EXTRA_STATUS = "STATUS";

    protected static final int GATT_CHARACTERISTIC_RSSI_MSG = 100;
    public static  DsBluetoothConnector getInstance(Context context,BluetoothAdapter mAdapter) {
        if (Instance == null) {
            Instance = new DsBluetoothConnector(context,mAdapter);
        }
        return Instance;
    }

    public static  DsBluetoothConnector getInstance(Context context) {
        if (Instance == null) {
            Instance = new DsBluetoothConnector(context);
        }
        return Instance;
    }

    private DsBluetoothConnector(Context context) {
        mContext = context;
        initialize(context);
    }

    private DsBluetoothConnector(Context context,BluetoothAdapter mAdapter) {
        mContext = context;
        initialize(mAdapter);
    }

    // for check service discover
    private Handler mHandler = new Handler();
    private boolean isServiceDiscover = false;
    private Runnable checkDiscoverRunable;
    private void checkServiceDiscover(final BluetoothGatt gatt){
        isServiceDiscover = false;
        checkDiscoverRunable = new Runnable() {
            @Override
            public void run() {
                if (!isServiceDiscover) {
                    gatt.disconnect();
                    Toast.makeText(mContext, "未能发现服务，请重新连接", Toast.LENGTH_LONG).show();
                }
            }
        };
        mHandler.postDelayed(checkDiscoverRunable, 5000);
    }

    private void disableServiceDiscover(){
        if(checkDiscoverRunable!=null){
            mHandler.removeCallbacks(checkDiscoverRunable);
        }
    }

    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            DesayLog.d("------------->onConnectionStateChange received: " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Attempts to discover services after successful connection.
                gatt.discoverServices();
                checkServiceDiscover(gatt);
            } else {
                if(mConnectionState==BLEContentProvider.STATE_CONNECTING){//connecting before,so this is a connect fail
                    mConnectionState = BLEContentProvider.STATE_CONNECTED_FAIL;
                }else{//disconnect
                    mConnectionState = BLEContentProvider.STATE_DISCONNECTED;
                }
                gatt.close();
                //disconnect now, reset the synDataStart
                synDataStart = false;
                mBluetoothGatt = null;
                DesayLog.d("------------->lose() ");
                //handle the connect state
                handleConnectState();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Log.i("onServicesDiscovered","------------->onServicesDiscovered received: " + status);
            //call back coming,disable the check
            disableServiceDiscover();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGatt = gatt;
                isServiceDiscover = true;
                enableNotifications();
                mConnectionState = BLEContentProvider.STATE_CONNECTED;
                handleConnectState();
//                if(bond_address.equals(gatt.getDevice().getAddress())){
//                    Bond_State = true;
//                }else{
//                    Bond_State = false;
//                }
            } else {
                gatt.disconnect();
                //gatt.close();//网上找到加上这个比较好
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }

                DesayLog.d( "onCharacteristicRead , ble_address=" + gatt.getDevice().getAddress() + ",status= " + status + " read = " + stringBuilder);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            DesayLog.d( "------------->onCharacteristicWrite received: " + status);
            // TODO Auto-generated method stub
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }
                DesayLog.d( "UUID =" + characteristic.getUuid().toString() + "  write  " + stringBuilder);
            }
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            DesayLog.d("------------->onDescriptorRead received: " + status + "  descriptor =" + descriptor.getValue());
            final byte[] data = descriptor.getValue();
            StringBuilder stringBuilder = null;
            if (data != null && data.length > 0) {
                stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
            }
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            DesayLog.d("------------->onDescriptorWrite received: " + status + ",descriptor.uuid = " + descriptor.getCharacteristic().getUuid().toString());
            // TODO Auto-generated method stub
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            DesayLog.d("------------->onReliableWriteCompleted received: " + status);
            // TODO Auto-generated method stub
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            //Log.w("ellison1", "------------->onReadRemoteRssi received: " + status +"  rssi="+rssi);
            DesayLog.d("------------->onReadRemoteRssi received: " + status + "  rssi=" + rssi);
            // TODO Auto-generated method stub
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            DesayLog.d( "onCharacteristicChanged uuid = "+characteristic.getUuid().toString());
            String uuid = characteristic.getUuid().toString();
            if (uuid.equals(BLEContentProvider.SERVER_A_UUID_NOTIFY.toString())||uuid.equals(BLEContentProvider.SERVER_B_UUID_NOTIFY.toString())) {
                byte[] data = characteristic.getValue();
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data) {
                        if(byteChar!=0x0d && byteChar!=0x0a){
                            stringBuilder.append((char) byteChar);
                        }
                    }
                }
                if(synDataStart){//如果是同步数据处理
                    Bundle mBundle = new Bundle();
                    Message msg = Message.obtain(writeValueHandler, DATA_ANALYST);
                    mBundle.putByteArray(EXTRA_BYTES, data);
                    msg.setData(mBundle);
                    msg.sendToTarget();
                }else{
                    DesayLog.d( "data.size = " + data.length + " notify_content = " + stringBuilder.toString());
                    Bundle mBundle = new Bundle();
                    Message msg = Message.obtain(writeValueHandler, CHARA_CHANGED);
                    mBundle.putString(EXTRA_STRING, stringBuilder.toString());
                    msg.setData(mBundle);
                    msg.sendToTarget();
                }

            }
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    private void handleConnectState(){
        Message msg = Message.obtain(writeValueHandler, ON_LISTEN_CONNECT_STATE);
        msg.sendToTarget();
    }


    private void notifyConnectState(){
        try {
            mOnBLECallBackListener.OnConnectCallBack(KEY_CONNECT_STATUS,mConnectionState);
        }catch (Exception e){
            DesayLog.d("e = " + e);
        }
    }

//    private boolean Bond_State = false;
//    private void notifyBondState(){
//        try {
//            mOnBLECallBackListener.OnCallBack(KEY_BAND_BOND,Bond_State,FUNCTION_ENABLE);
//        }catch (Exception e){
//            DesayLog.d("e = " + e);
//        }
//    }

    public static int TimeZoneOffset = 8*60*60;//h 东八区
    public static void setTimeZone(int time_zone){
        TimeZoneOffset = time_zone*60*60;
    }

    private void enableNotifications() {
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog had only one notify characteristic
            Bundle mBundle = new Bundle();
            Message msg = Message.obtain(writeValueHandler, ENABLE_GATT_NOTIFY);
            mBundle.putInt(EXTRA_INT, BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
            msg.setData(mBundle);
            msg.sendToTarget();
        }else{
            for(int i=0;i<5;i++){
                if(i==BLEContentProvider.SERVER_A_UUID_REQUEST_INT){
                    Bundle mBundle = new Bundle();
                    Message msg = Message.obtain(writeValueHandler, ENABLE_GATT_NOTIFY);
                    mBundle.putInt(EXTRA_INT, BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
                    msg.setData(mBundle);
                    msg.sendToTarget();
                }else if(i==BLEContentProvider.SERVER_B_UUID_REQUEST_INT){
                    Bundle mBundle = new Bundle();
                    Message msg = Message.obtain(writeValueHandler, ENABLE_GATT_NOTIFY);
                    mBundle.putInt(EXTRA_INT, BLEContentProvider.SERVER_B_UUID_REQUEST_INT);
                    msg.setData(mBundle);
                    msg.sendToTarget();
                }
            }
        }

    }

    private boolean setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
                                                  List<BluetoothGattService> gattServices, boolean enabled,int witch) {
        if (mBluetoothAdapter == null || gattServices == null) {
            return false;
        }
        UUID uuid = null;
        switch(witch){
            case BLEContentProvider.SERVER_A_UUID_REQUEST_INT:
                uuid = BLEContentProvider.SERVER_A_UUID_NOTIFY;
                break;
            case BLEContentProvider.SERVER_B_UUID_REQUEST_INT:
                uuid = BLEContentProvider.SERVER_B_UUID_NOTIFY;
                break;
            default:
                break;
        }
        boolean NotifiResult =false;
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if(uuid.toString().equals(gattCharacteristic.getUuid().toString())){
                    NotifiResult = mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, enabled)
                            &&notifyDescriptorWrite(gattCharacteristic,enabled);
                }
            }
        }
        return NotifiResult;
    }

    private boolean notifyDescriptorWrite(BluetoothGattCharacteristic characteristic,boolean enabled){
        BluetoothGattDescriptor descriptor = null;
        boolean NotifiResult =false;
        if(characteristic!=null){
            descriptor =   characteristic.getDescriptor(BLEContentProvider.SERVER_A_UUID_DESCRIPTOR);
        }
        if (descriptor != null) {
            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[] { 0x00,
                    0x00 });
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            NotifiResult = mBluetoothGatt.writeDescriptor(descriptor);
        }
        return NotifiResult;
    }


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    private boolean initialize(BluetoothAdapter mAdapter) {
        mBluetoothAdapter = mAdapter;
        return true;
    }


    private boolean initialize(Context mContext) {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                DesayLog.e("Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            DesayLog.e( "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }


    public void readRssi(){
        if (mBluetoothGatt !=null) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            DesayLog.e("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }


   private String  CMD_BOND = NordicSeriesDevices.CMD_BOND;//default nordic divice
   private String  CMD_BAND_USER = NordicSeriesDevices.CMD_BAND_USER;//default nordic divice
   private String  CMD_BAND_VERSION = NordicSeriesDevices.CMD_BAND_VERSION;//default nordic divice
   private String  CMD_SYN_TIME = NordicSeriesDevices.CMD_SYN_TIME;//default nordic divice
   private String  CMD_SET_CLOCK_ONE = NordicSeriesDevices.CMD_SET_CLOCK_ONE;//default nordic divice
   private String  CMD_SET_CLOCK_TWO = NordicSeriesDevices.CMD_SET_CLOCK_TWO;//default nordic divice
   private String  CMD_BATTERY = NordicSeriesDevices.CMD_BATTERY;//default nordic divice
   private String  CMD_STEP = NordicSeriesDevices.CMD_STEP;//default nordic divice
   private String  CMD_SIT_SETTINGS = NordicSeriesDevices.CMD_SIT_SETTINGS;//default nordic divice
   private String  CMD_PHONE_CALL = NordicSeriesDevices.CMD_PHONE_CALL;//default nordic divice
   private String  CMD_PUSH_CH = NordicSeriesDevices.CMD_PUSH_CH;//default nordic divice
   private String  CMD_SET_LAN = NordicSeriesDevices.CMD_SET_LAN;//default nordic divice
   private String  CMD_SETSPORT_AIM = NordicSeriesDevices.CMD_SETSPORT_AIM;//default nordic divice
   private String  CMD_HANDUP = NordicSeriesDevices.CMD_HANDUP;//default nordic divice
   private String  CMD_SYN_TIMELY_STEPS = NordicSeriesDevices.CMD_SYN_TIMELY_STEPS;//default nordic divice
   private String  CMD_SET_SYN_FLAG = NordicSeriesDevices.CMD_SET_SYN_FLAG;//default nordic divice
   private String  CMD_SYN_MUSICPLAY_STATE = NordicSeriesDevices.CMD_SYN_MUSICPLAY_STATE;//default nordic divice
   private String  CMD_STATIC_HEART = NordicSeriesDevices.CMD_STATIC_HEART;//default nordic divice
   private String  CMD_FIND_BAND = NordicSeriesDevices.CMD_FIND_BAND;//default nordic divice
   private String  CMD_CAMERA_FLAG = NordicSeriesDevices.CMD_CAMERA_FLAG;//default nordic divice
   private String  CMD_MUSIC = NordicSeriesDevices.CMD_MUSIC;//default nordic divice
   private String  CMD_FIND_PHONE = NordicSeriesDevices.CMD_FIND_PHONE;//default nordic divice
   private String  CMD_SET_DISTANCE_UNITS = NordicSeriesDevices.CMD_SET_DISTANCE_UNITS;//default nordic divice
   private String  CMD_REQUEST_DATA = NordicSeriesDevices.CMD_REQUEST_DATA;//default nordic divice
   private String  TIMELY_STEPS_NOTIF = NordicSeriesDevices.TIMELY_STEPS_NOTIF;//default nordic divice
   private String  CMD_BAND_PHOTO = NordicSeriesDevices.CMD_BAND_PHOTO;//default nordic divice
   private String  BAND_MUSIC_CMD_PLAY = NordicSeriesDevices.BAND_MUSIC_CMD_PLAY;//default nordic divice
   private String  BAND_MUSIC_CMD_PAUSE = NordicSeriesDevices.BAND_MUSIC_CMD_PAUSE;//default nordic divice
   private String  BAND_MUSIC_CMD_NEXT = NordicSeriesDevices.BAND_MUSIC_CMD_NEXT;//default nordic divice
   private String  BAND_MUSIC_CMD_PRE = NordicSeriesDevices.BAND_MUSIC_CMD_PRE;//default nordic divice
   private String  BAND_FIND_PHONE_CMD = NordicSeriesDevices.BAND_FIND_PHONE_CMD;//default nordic divice


    /**
     * bind the band
     */
    public void bindMyBand(){
        addCMD(CMD_BOND,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     * @param user_height user height
     * @param user_weight user weight
     */
    public void synUserInfo(int user_height,int user_weight){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
           //dialog current , return
            return;
        }

        String height = user_height+"";
        if(height.length()<3){
            switch (height.length()){
                case 1:
                    height = "00"+height;
                    break;
                case 2:
                    height = "0"+height;
                    break;
            }
        }
        String weight = user_weight+"";
        if(weight.length()<3){
            switch (weight.length()){
                case 1:
                    weight = "00"+weight;
                    break;
                case 2:
                    weight = "0"+weight;
                    break;
            }
        }

        String cmd = CMD_BAND_USER+"="+"a"+","+height+","+weight;
        DesayLog.d("cmd = "+cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }

    /**
     *check Band Version
     */
    public void checkBandVersion(){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            addCMD(CMD_BAND_VERSION,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
            return;
        }
        addCMD(CMD_BAND_VERSION,BLEContentProvider.SERVER_B_UUID_REQUEST_INT);
    }

    /**
     * @param time 24h sample String "20140520135601"
     */
    public void synBandTime(String time){
        String cmd = CMD_SYN_TIME+"="+time;
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }

    /**
     * @param witch       witch alarm   the value should be  1/2
     * @param enable      enable or diable the value should be  1/0
     * @param cycle_time  should be String sample "1111110" means 周日周一周二周三周四周五有闹钟提醒 周六没有 0 for open  1 for close
     * @param alarm_time  should be String sample "0800"  for 08:00 in 24h
     * **/
    public void setBandAlarm(int witch,int enable,String cycle_time,String alarm_time){//Set the alarm
        if(cycle_time.length()!=7){
            return;
        }
        if(alarm_time.length()!=4){
            return;
        }
        //check the params
        String cmd = null;
        if(witch==1){
            cmd = CMD_SET_CLOCK_ONE+"="+(enable>0?1:0)+","+"00"+","+cycle_time+"1,"+alarm_time;
        }else if(witch==2){
            cmd = CMD_SET_CLOCK_TWO+"="+(enable>0?1:0)+","+"00"+","+cycle_time+"1,"+alarm_time;
        }
        DesayLog.d( "cmd = "+cmd);
        if(cmd!=null){
            addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
        }
    }

    /**
     *check band battry,you can get the result by call BLEContentProvider.getBandPower();
     */
    public void checkBandBattry(){
        String cmd = CMD_BATTERY;
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     *
     * @param steps    if steps <= 0 will be get the band total steps
     *                 else  will be set the band total steps
     *                 sample :
     *                 getBandTotalSteps(0);
     *                 return the band current steps,you can get the result by call BLEContentProvider.getBandCurrentSteps();
     *                 getBandTotalSteps(1000);
     *                 set the band current steps as 1000.
     *                 the steps set to band must be more than the band steps already had.
     *                 the steps set to band must Less than the maximum 99999
     *
     */
    public void getBandTotalSteps(int steps){
        String cmd;
        if(steps<=0){
            cmd = CMD_STEP;
        }else{
            String steps_str = ""+steps;
            switch(steps_str.length()){
                case 1:
                    steps_str = "0000" + steps;
                    break;
                case 2:
                    steps_str = "000" + steps;
                    break;
                case 3:
                    steps_str = "00" + steps;
                    break;
                case 4:
                    steps_str = "0" + steps;
                    break;
                case 5:
                    steps_str = "" + steps;
                    break;
            }

            cmd = CMD_STEP+"="+steps_str;
        }
        DesayLog.d("getBandTotalSteps cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }

    /**
     * @param _long    how long we sit the band will remind will be 30/60/90 (min)
     * @param start_time the remind start time in a day sample "0800" means start 08:00
     * @param end_time the remind end time in a day sample "2300" means end 23:00
     * @param _enable enable/disable the Sedentary function , must be 1/0
     */
    public void setSedentary(int _long,String start_time,String end_time,int _enable){
        if(start_time.length()!=4){
            return;
        }
        if(end_time.length()!=4){
            return;
        }

        if(_enable>0){
            _enable = 1;
        }else {
            _enable = 0;
        }

        String cmd = CMD_SIT_SETTINGS;
        if(_long<30){
            _long = 30;
        }else if(_long>90){
            _long = 90;
        }
        String long_str = "0"+_long;
        cmd = cmd+"="+long_str+","+start_time+","+end_time+","+_enable;
        DesayLog.d("getBandTotalSteps cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }



    private boolean PHONE_CALL_STATE = false;
    private String phone_call_cmd = "";
    /**
     * @param type type 0/1 to en/ch
     * @param content
     * @param msgType message type:
     *                              0-phone call，1-SMS，3-QQ，4-wechat，5-fackbook, 6-twitter,7-whatsapp,8-email,9-line,
     *                              you should get message type from @BLEContentProvider
     *                remember! this method must be used before setPhoneCallState();
     *                如果当前已经在内部通知状态，则不再接受其他的来电指令，不然逻辑会混乱
     */
    public void massageRemind(int type,String content,int msgType){
        DesayLog.d("synCallerInfo type = " + type + "，content = " + content);
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            type = 0;//default no chinese in dialog
            content = "a";
        }
        if(!PHONE_CALL_STATE){
            String cmd = null;
            switch(type){
                case 0://en
                    cmd = CMD_PHONE_CALL+"="+type+","+content+","+0+","+msgType;
                    phone_call_cmd = cmd;
                    DesayLog.d("synCallerInfo cmd = " + cmd + "，phone_call_cmd = " + phone_call_cmd);
                    break;
                case 1://ch
                    //add ready cmd
                    cmd = CMD_PUSH_CH;
                    //add the ch content
                    if(content.length()>4){//ch content length must less than four
                        content = content.substring(0,4);
                    }
                    addCH(content,msgType);
                    break;
            }
            if(cmd != null){
                addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
            }
        }
    }

    /**
     * @param isPhoneCallNow phone call state
     *        remember! this method must be used and must be used after synCallerInfo();
     */
    public void setPhoneCallState(boolean isPhoneCallNow){
        PHONE_CALL_STATE = isPhoneCallNow;
        if(!isPhoneCallNow){
            phoneCallStop();
        }
    }

//    public void getBandSerialNumber(){
//        addCMD(BLEContentProvider.CMD_GET_SN,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
//    }


    /**
     * @param language 1/0 - cn/en
     *                 you should get language from @BLEContentProvider
     */
    public void setBandLanguage(int language){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_SET_LAN + "="+ language;
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     * @param aim aim for sports,  the aim set to band must Less than the maximum 99999 and should be n*1000
     *            if aim = 0 means close the remind function
     */
    public void setBandSportsAim(int aim){
        String steps_str = ""+aim;
        switch(steps_str.length()){
            case 1:
                steps_str = "0000" + aim;
                break;
            case 2:
                steps_str = "000" + aim;
                break;
            case 3:
                steps_str = "00" + aim;
                break;
            case 4:
                steps_str = "0" + aim;
                break;
            case 5:
                steps_str = "" + aim;
                break;
        }
        String cmd = CMD_SETSPORT_AIM + "="+ steps_str;
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }

    /**
     * @param param the hand up param,
     *
     * * 参数：
     * 0：关闭
     * 1：自动
     * 2: 左手
     * 3：右手
     * you should get param from @BLEContentProvider
     *public static int HANDUP_CLOSE = 0;
     *public static int HANDUP_AUTO = 1;
     *public static int HANDUP_LEFT = 2;
     *public static int HANDUP_RIGHT = 3;
     */
    public void setBandHandUpParam(int param){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_HANDUP + "="+ param;
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     * @param enable
     *        enable Timely steps syn
     *        with true , you can get the timely steps from band.
     *        you can get the result by call BLEContentProvider.getTimelyStepsEnableState();
     */
    public void enableSynTimelySteps(boolean enable){
        String cmd = CMD_SYN_TIMELY_STEPS + "="+ (enable? 1 : 0);
        DesayLog.d("CMD_SYN_TIMELY_STEPS cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     * @param show    show the syn icon or close the icon show
     *                witch this method you can control the band syn show or not
     */
    public void showBandSynIcon(boolean show){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_SET_SYN_FLAG + "="+ (show? 1 : 0);
        DesayLog.d("CMD_SYN_TIMELY_STEPS cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     * @param play  Synchronize the phone music play state .
     *              the band would not know the play state if it is change by
     *              operating at phone,so need to syn the state to band.
     *
     *
     */
    public void synPhoneMusicPlayState(boolean play){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_SYN_MUSICPLAY_STATE + "="+ (play? 1 : 0);
        DesayLog.d("CMD_SYN_TIMELY_STEPS cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     *
     * control band start check user HeartRate
     * you can get the result by call BLEContentProvider.getBandHeartRateCheckState();
     */
    public void startCheckHeartRate(){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
//        String cmd = BLEContentProvider.CMD_STATIC_HEART + "="+ (true? 1 : 0);
        String cmd = CMD_STATIC_HEART + "="+ 1;
        DesayLog.d("CMD_SYN_TIMELY_STEPS cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     * You can get the result by call BLEContentProvider.getFindBandState();
     * But you must reset the state after getFindBandState().
     * Call BLEContentProvider.setFindBandState(false) after you get the result.
     */
    public void findMyBand(boolean start){
        if(start&&!findBandStart){
            String cmd = CMD_FIND_BAND + "="+ 1;
            DesayLog.d("CMD_FIND_BAND cmd = " + cmd);
            addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
        }else{
            findBandStop();
        }
    }


    /**
     * @param show show/close the band snapshot View
     *
     */
    public void showBandPhotoView(boolean show){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_CAMERA_FLAG + "="+ (show? 1 : 0);
        DesayLog.d("CMD_CAMERA_FLAG cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }

    /**
     *
     * @param enable    enable/disable band music function
     */
    public void enableMusicFunction(boolean enable){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_MUSIC + "="+ (enable? 1 : 0);
        DesayLog.d("CMD_CAMERA_FLAG cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     *
     * @param enable    enable/disable band find phone function
     */
    public void enableFindPhoneFunction(boolean enable){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_FIND_PHONE + "="+ (enable? 1 : 0);
        DesayLog.d("CMD_FIND_PHONE cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }


    /**
     *
     * @param units     you should get units from @BLEContentProvider
     *                   public static final int BAND_UNITS_MILE = 0;
     *                   public static final int BAND_UNITS_KILO = 1;
     */
    public void setBandUnits(int units){
        if(Current_Device==DialogSeriesDevices.DIALOG_DEVICE){
            //dialog current , return
            return;
        }
        String cmd = CMD_SET_DISTANCE_UNITS + "="+ units;
        DesayLog.d("CMD_SET_DISTANCE_UNITS cmd = " + cmd);
        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
    }



    /**
     *syn data
     */
    private boolean synDataStart = false;
    private int SYN_PAKEGE_NUMBER = 0;
    public void synData(){
        if(synDataStart){//如果当前已经在同步数据，则直接返回
            return;
        }
        synDataStart = true;
        String cmd = CMD_REQUEST_DATA + "="+ 0;
        DesayLog.d("CMD_CAMERA_FLAG cmd = " + cmd);
        synData(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
        SYN_PAKEGE_NUMBER = 0;
    }

    private void synData(String cmdCode,int write_UUID){
        monitorData = new DataAnalyst.MonitorData();
        if(Instance != null && cmdCode !=null){
            cmdCode = cmdCode +"\r\n";//add the end flag
            int strLength = cmdCode.length();
            for (int i = 0; i < strLength;) {
                int end =  i+DATA_LENGTH;
                if((i+DATA_LENGTH)>strLength){
                    end = strLength;
                }
                DesayLog.d("cmdCode.substring(i, end) = " + cmdCode.substring(i, end));
                byte[] writeByte =  cmdCode.substring(i, end).getBytes();
                sendCMD(writeByte,write_UUID);
                i = i + DATA_LENGTH;
            }
        }
    }

    //给上层反馈当前同步状态
    public boolean getSynState(){
        return synDataStart;
    }
    Handler proxyHandler =new Handler() {
        Bundle bundle;
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case PHONE_CALL:
                    DesayLog.d("phone_call_cmd " + phone_call_cmd);
                    if(!phone_call_cmd.equals("")){
                        addCMD(phone_call_cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
                    }
                    break;
                case FIND_BAND:
                    DesayLog.d("find band timer start");
                    String find_band_cmd = CMD_FIND_BAND + "="+ 1;
                    addCMD(find_band_cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
                    break;
            }
        }
    };
    private boolean phoneCallThreadStart = false;
    private boolean findBandStart = false;
    Timer mProxyTimer;
    TimerTask mPhoneCallTask;
    TimerTask mFindBandTask;
    private void phoneCallStart(){
        if(findBandStart){
            findBandStop();
        }
        mProxyTimer = new Timer();
        mPhoneCallTask = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                proxyHandler.sendEmptyMessage(PHONE_CALL);
            }
        };
        mProxyTimer.schedule(mPhoneCallTask,0,6000);
        phoneCallThreadStart = true;
    }
    private void phoneCallStop(){
        phone_call_cmd = "";
        phoneCallThreadStart = false;
        if(mProxyTimer != null){
            mProxyTimer.cancel();
        }
    }

    private void findBandStop(){
        findBandStart = false;
//        BLEContentProvider.setFindBandState(false);
        mProxyTimer.cancel();
    }
    private void findBandStart(){
        if(phoneCallThreadStart){
            phoneCallStop();
        }
        mProxyTimer = new Timer();
        mFindBandTask = new TimerTask() {
            @Override
            public void run() {
                // 需要做的事:发送消息
                proxyHandler.sendEmptyMessage(FIND_BAND);
            }
        };
        mProxyTimer.schedule(mFindBandTask,2000,2000);
        findBandStart = true;
    }

    DataAnalyst.MonitorData monitorData;
    Handler writeValueHandler =new Handler() {
        Bundle bundle;
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
//                case ON_LISTEN_BOND_STATE:
//                        notifyBondState();
//                    break;
                case ON_LISTEN_CONNECT_STATE:
                    notifyConnectState();
                    break;
                case GATT_CMD:
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    bundle = msg.getData();
                    byte[] writeByte = bundle.getByteArray(EXTRA_BYTES);
                    Log.i("转回字符串",writeByte.toString());
                    int uuid = bundle.getInt(EXTRA_INT);
                    WriteCMD(writeByte,uuid);
                    break;
                case ENABLE_GATT_NOTIFY:
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    bundle = msg.getData();
                    int witch = bundle.getInt(EXTRA_INT);
                    if(mBluetoothGatt!=null){
                        setCharacteristicNotification(mBluetoothGatt,mBluetoothGatt.getServices(),true,witch);
                    }
                    break;
                case CHARA_CHANGED:
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    bundle = msg.getData();
                    String data = bundle.getString(EXTRA_STRING);
                    //handle the data
                    DesayLog.d("data = "+data);
                    handleData(data);
                    break;
                case PUSH_CH_WORDS:
                    if(PUSH_BYTES.size()!=0){
                        pushCH();
                    }
                    break;
                case DATA_ANALYST:
                    bundle = msg.getData();
                    byte[] analystByte = bundle.getByteArray(EXTRA_BYTES);
                    if(analystByte!=null && !monitorData.isFull()){
                        monitorData.addData(analystByte);
                    }
                    if(monitorData.isFull()){
                        analystData(monitorData);
                        SYN_PAKEGE_NUMBER++;
                        DesayLog.d("monitorData.isFull() = " + monitorData.isFull() + ",SYN_PAKEGE_NUMBER = " + SYN_PAKEGE_NUMBER);
                        if(DEBUG){
                            if(SYN_PAKEGE_NUMBER<monitorData.header.total){
                                //continue get the data
                                String cmd = CMD_REQUEST_DATA + "="+ SYN_PAKEGE_NUMBER;
                                DesayLog.d("syndata----------------- cmd = " + cmd);
                                synData(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
                            }else{
                                synDataStart = false;
                                mOnBLECallBackListener.OnCallBack(KEY_SYN_DATA,true,1);
                            }
                        }else{
                            if(SYN_PAKEGE_NUMBER<=monitorData.header.total){
                                //continue get the data
                                String cmd = CMD_REQUEST_DATA + "="+ SYN_PAKEGE_NUMBER;
                                DesayLog.d("syndata----------------- cmd = " + cmd);
                                synData(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
                            }else{
                                synDataStart = false;
                                mOnBLECallBackListener.OnCallBack(KEY_SYN_DATA,true,1);
                            }
                        }
                    }
                    break;
            }
        }
    };


    /**
     * @param monitorData
     */
    private void analystData(DataAnalyst.MonitorData monitorData){
        DesayLog.d("type = " + monitorData.header.type+"original = " + monitorData.header.original);
        List<int[]> l = DataAnalyst.splitData(monitorData.data, 6);
        List<DataAnalyst.ParserData> mParserDataList = new ArrayList<>();
        for (int[] bsTmp : l) {
            DataAnalyst.ParserData parserData = DataAnalyst.parser(bsTmp, false);
            if(parserData!=null){
                mParserDataList.add(parserData);
            }
        }
        switch(monitorData.header.type){
            case BLEContentProvider.DATA_STEPS:
                //add sports data
                for (DataAnalyst.ParserData data:mParserDataList){
                    DataTime dataTime = new DataTime(data.flag,data.secondTime);
                    SportsData mSportsData = new SportsData(dataTime,data.value);
                    BLEContentProvider.addSportsData(mSportsData);
                }
//                for(SportsData mSportsData:mSportsDataList){
//                    Log.d(TAG,"startTime = "+mSportsData.startTime+",sportsLong = "
//                            +mSportsData.sportsLong+",steps"+mSportsData.steps);
//                }
                List<SportsData> mSportsDataList = BLEContentProvider.getSportsData();
                if(mSportsDataList.size()!=0){
                    mOnBLECallBackListener.OnSportsDataCallBack(KEY_SYN_DATA_SPORTS,mSportsDataList);
                }
                break;
            case BLEContentProvider.DATA_HEARTRATE:
                //add heartRate data
                for (DataAnalyst.ParserData data:mParserDataList){
                    DataTime dataTime = new DataTime(data.flag,data.secondTime);
                    HeartRateData mHeartRateData = new HeartRateData(dataTime,data.value);
                    BLEContentProvider.addHeartRateData(mHeartRateData);
                }

//                for(HeartRateData mHeartRateData:mHeartRateDataList){
//                    Log.d(TAG,"Test_Time = "+mHeartRateData.Test_Time+",heartRate = "
//                            +mHeartRateData.Heart_Rate);
//                }
                List<HeartRateData> mHeartRateDataList= BLEContentProvider.getHeartRateData();
                if(mHeartRateDataList.size()!=0){
                    mOnBLECallBackListener.OnHeartRateDataCallBack(KEY_SYN_DATA_HEARTRATE,mHeartRateDataList);
                }
                break;
            case BLEContentProvider.DATA_SLEEP:
                //add heartRate data
                for (DataAnalyst.ParserData data:mParserDataList){
                    DataTime dataTime = new DataTime(data.flag,data.secondTime);
                    SleepData mSleepData = new SleepData(dataTime,data.value);
                    SleepBuilder.buildSleepData(mSleepData);
                }
//                for(SleepCycle mSleepCycle:mSleepDataList){
//
//                }
                 List<SleepCycle> mSleepDataList= BLEContentProvider.getSleepData();
                if(mSleepDataList.size()!=0){
                    mOnBLECallBackListener.OnSleepDataCallBack(KEY_SYN_DATA_SLEEP,mSleepDataList);
                }
                break;
        }

    }
    public static final int KEY_CONNECT_STATUS = 998;//连接状态
    public static final int KEY_SYN_DATA = 999;//连接状态
    public static final int KEY_SYN_DATA_SLEEP = 1000;//同步睡眠数据
    public static final int KEY_SYN_DATA_HEARTRATE = 1001;//同步心率数据
    public static final int KEY_SYN_DATA_SPORTS = 1002;//同步运动数据
    public static final int KEY_BAND_VERSION = 1003;//查询版本号
    public static final int KEY_BAND_BOND = 1004;//绑定手环
    public static final int KEY_SYN_TIME = 1005;//同步时间
    public static final int KEY_CLOCK_ONE = 1006;//手环闹钟1
    public static final int KEY_CLOCK_TWO = 1007;//手环闹钟2
    public static final int KEY_BAND_BATTERY = 1008;//手环电池电量
    public static final int KEY_BAND_STEPS = 1009;//手环当前步数
    public static final int KEY_SIT = 1010;//久坐提醒
    public static final int KEY_INCOMING_CALL = 1011;//来电提醒
    public static final int KEY_BAND_LANGUAGE = 1012;//手环语言设置
    public static final int KEY_SPORTS_AIM = 1013;//运动目标设置
    public static final int KEY_HAND_UP = 1014;//抬手亮屏设置
    public static final int KEY_TIMELY_STEPS_SYN = 1015;//实时步数通知
    public static final int KEY_BAND_SYN_FLAG = 1016;//手环同步图标显示
    public static final int KEY_SYN_MUSIC = 1017;//手机同步当前音乐播放状态到手机
    public static final int KEY_TEST_HEARTRATE = 1018;//控制手环测试心率
    public static final int KEY_FIND_BAND = 1019;//找手环
    public static final int KEY_BAND_CAMERA_FLAG = 1020;//手环拍照控制入口
    public static final int KEY_BAND_MUSIC_FLAG = 1021;//手环音乐控制入口
    public static final int KEY_FIND_PHONE_FLAG = 1022;//手环找手机控制入口
    public static final int KEY_BAND_UNITS = 1023;//手环距离单位
    public static final int KEY_BAND_USER = 1024;//手环用户信息
    public static final int KEY_CAMERA_SHOT = 1025;//拍照通知
    public static final int KEY_MUSIC_NOTIFY = 1026;//音乐播放通知
    public static final int KEY_FIND_PHONE_NOTIFY = 1027;//音乐播放通知




    public final int FUNCTION_ENABLE = 1;//功能当前状态，开启
    public final int FUNCTION_DISABLE = 0;//功能当前状态，关闭


    private void handleData(String data){
        if(data.contains(":")){
            String data_arry[] = data.split(":");
            String data_key = data_arry[0];
            String data_value = data_arry[1];
            if(data_key.equals(CMD_BAND_VERSION)){//check band version
//                    BLEContentProvider.setBandVersion(data_value);
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_VERSION,false,data_value);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_VERSION,true,data_value);
                }
            }else if(data_key.equals(CMD_BOND)){//bond band
                if(data_value.toLowerCase().equals("ok")){
//                        BLEContentProvider.setBondState(true);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_BOND,true,FUNCTION_ENABLE);
                }else if(data_value.toLowerCase().equals("err")){
//                        BLEContentProvider.setBondState(false);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_BOND,false,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_SYN_TIME)){//syn time
//                    BLEContentProvider.setSynTime(data_value);
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_SYN_TIME,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_SYN_TIME,true,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_SET_CLOCK_ONE)){//set clock
//                    BLEContentProvider.setAlarmClockOne(data_value);
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_CLOCK_ONE,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_CLOCK_ONE,true,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_SET_CLOCK_TWO)){//set clock
//                    BLEContentProvider.setAlarmClockTwo(data_value);
                if(data_value.toLowerCase().contains("error")){
                        mOnBLECallBackListener.OnCallBack(KEY_CLOCK_TWO,false,FUNCTION_ENABLE);
                }else{
                        mOnBLECallBackListener.OnCallBack(KEY_CLOCK_TWO,true,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_BATTERY)){//set battery
//                    BLEContentProvider.setBandPower(data_value);
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_BATTERY,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_BATTERY,true,Integer.parseInt(data_value));
                }
            }else if(data_key.equals(CMD_STEP)){//set steps
//                    BLEContentProvider.setBandCurrentSteps(Integer.parseInt(data_value));
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_STEPS,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_STEPS,true,Integer.parseInt(data_value));
                }
            }else if(data_key.equals(CMD_SIT_SETTINGS)){
//                    BLEContentProvider.setCurrentSit(data_value);
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_SIT,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_SIT,true,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_PHONE_CALL)){
//                    BLEContentProvider.setIncomingCallMsg(data_value);
                if(data_value.equals("OK")){
                    DesayLog.d("PHONE_CALL PHONE_CALL_STATE = "+PHONE_CALL_STATE+",phoneCallThreadStart = "+phoneCallThreadStart);
                    if(PHONE_CALL_STATE && !phoneCallThreadStart){
                        phoneCallStart();
                    }
                    mOnBLECallBackListener.OnCallBack(KEY_INCOMING_CALL,true,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_INCOMING_CALL,false,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_PUSH_CH)){//push ch ready
                if(data_value.equals("RDY")){//ready to receive ch words
                    if(PUSH_BYTES.size()!=0){
                        writeValueHandler.sendEmptyMessage(PUSH_CH_WORDS);
                    }
                }else if(data_value.equals("OK")){
                    String cmd = null;
                    if(PUSH_BYTES.size()!=0){
                        cmd = CMD_PUSH_CH;
                        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
                    }else{// push over, remind phone call now
                        cmd = CMD_PHONE_CALL+"="+1+","+0+","+0+","+MSG_TYPE;
                        addCMD(cmd,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
                        phone_call_cmd = cmd;
                    }
                }
            }else if(data_key.equals(CMD_SET_LAN)){
//                    BLEContentProvider.setBandLanguage(Integer.parseInt(data_value));
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_LANGUAGE,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_LANGUAGE,true,Integer.parseInt(data_value));
                }
            }else if(data_key.equals(CMD_SETSPORT_AIM)){
//                    BLEContentProvider.setMovingTarget(Integer.parseInt(data_value));
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_SPORTS_AIM,false,0);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_SPORTS_AIM,true,Integer.parseInt(data_value));
                }
            }else if(data_key.equals(CMD_HANDUP)){
//                    BLEContentProvider.setRaisingParam(Integer.parseInt(data_value));
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_HAND_UP,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_HAND_UP,true,Integer.parseInt(data_value));
                }
            }else if(data_key.equals(CMD_SYN_TIMELY_STEPS)){
                if(data_value.toLowerCase().equals("ok")){
//                        BLEContentProvider.setTimelyStepsEnableState(true);
                    mOnBLECallBackListener.OnCallBack(KEY_TIMELY_STEPS_SYN,true,FUNCTION_ENABLE);

                }else if(data_value.toLowerCase().equals("error")){
//                        BLEContentProvider.setTimelyStepsEnableState(false);
                    mOnBLECallBackListener.OnCallBack(KEY_TIMELY_STEPS_SYN,false,FUNCTION_ENABLE);
                }else{
//                        BLEContentProvider.setTimelyStepsEnableState(false);
                    mOnBLECallBackListener.OnCallBack(KEY_TIMELY_STEPS_SYN,true,FUNCTION_DISABLE);
                }
            }else if(data_key.equals(TIMELY_STEPS_NOTIF)){
//                    BLEContentProvider.setBandCurrentSteps(Integer.parseInt(data_value));
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_STEPS,false,0);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_STEPS,true,Integer.parseInt(data_value));
                }
            }else if(data_key.equals(CMD_SET_SYN_FLAG)){
                if(data_value.equals("1")){
//                        BLEContentProvider.setBandSynFlagstate(true);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_SYN_FLAG,true,FUNCTION_ENABLE);
                }else if(data_value.equals("0")){//关闭同步图标
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_SYN_FLAG,true,FUNCTION_DISABLE);
//                        BLEContentProvider.setBandSynFlagstate(false);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_SYN_FLAG,false,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_SYN_MUSICPLAY_STATE)){
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_SYN_MUSIC,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_SYN_MUSIC,true,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_STATIC_HEART)){
                if(data_value.toLowerCase().equals("ok")){
//                        BLEContentProvider.setBandHeartRateCheckState("stop");
                }else if(data_value.toLowerCase().equals("err")){
//                        BLEContentProvider.setBandHeartRateCheckState("error");
                    mOnBLECallBackListener.OnCallBack(KEY_TEST_HEARTRATE,false,FUNCTION_ENABLE);
                }else{
//                        BLEContentProvider.setBandHeartRateCheckState(""+Integer.parseInt(data_value));
                    mOnBLECallBackListener.OnCallBack(KEY_TEST_HEARTRATE,true,Integer.parseInt(data_value));
                }
            }else if(data_key.equals(CMD_FIND_BAND)){
                if(data_value.toLowerCase().equals("ok")){
//                        BLEContentProvider.setFindBandState(true);
                    if(!findBandStart){
                        findBandStart();
                    }
                    mOnBLECallBackListener.OnCallBack(KEY_FIND_BAND,true,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_FIND_BAND,true,FUNCTION_DISABLE);
                }
                DesayLog.d("CMD_SYN_MUSICPLAY_STATE data_value = "+data_value);
            }else if(data_key.equals(CMD_CAMERA_FLAG)){
                if(data_value.toLowerCase().equals("1")){
//                        BLEContentProvider.setBandCameraIconShowState(true);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_CAMERA_FLAG,true,FUNCTION_ENABLE);
                }else if(data_value.toLowerCase().equals("0")){
//                        BLEContentProvider.setBandCameraIconShowState(false);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_CAMERA_FLAG,true,FUNCTION_DISABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_CAMERA_FLAG,false,FUNCTION_ENABLE);
                }
            }else if(data_key.equals(CMD_MUSIC)){
                if(data_value.toLowerCase().equals("1")){
//                        BLEContentProvider.setMusicPlayFuncState(true);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_MUSIC_FLAG,true,FUNCTION_ENABLE);
                }else if(data_value.toLowerCase().equals("0")){
//                        BLEContentProvider.setMusicPlayFuncState(false);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_MUSIC_FLAG,true,FUNCTION_DISABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_MUSIC_FLAG,false,FUNCTION_DISABLE);
                }
            }else if(data_key.equals(CMD_FIND_PHONE)){
                if(data_value.toLowerCase().equals("1")){
//                        BLEContentProvider.setFindPhoneFuncState(true);
                    mOnBLECallBackListener.OnCallBack(KEY_FIND_PHONE_FLAG,true,FUNCTION_ENABLE);
                }else if(data_value.toLowerCase().equals("0")){
//                        BLEContentProvider.setFindPhoneFuncState(false);
                    mOnBLECallBackListener.OnCallBack(KEY_FIND_PHONE_FLAG,true,FUNCTION_DISABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_FIND_PHONE_FLAG,false,FUNCTION_DISABLE);
                }
            }else if(data_key.equals(CMD_SET_DISTANCE_UNITS)){
                if(data_value.toLowerCase().equals("1")){
//                        BLEContentProvider.setBandUint(BLEContentProvider.BAND_UNITS_KILO);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_UNITS,true,FUNCTION_ENABLE);
                }else if(data_value.toLowerCase().equals("0")){
//                        BLEContentProvider.setBandUint(BLEContentProvider.BAND_UNITS_MILE);
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_UNITS,true,FUNCTION_DISABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_UNITS,false,FUNCTION_DISABLE);
                }
            }else if(data_key.equals(CMD_BAND_USER)){
//                    BLEContentProvider.setUserInfo(data_value);
                if(data_value.toLowerCase().contains("error")){
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_USER,false,FUNCTION_ENABLE);
                }else{
                    mOnBLECallBackListener.OnCallBack(KEY_BAND_USER,true,FUNCTION_ENABLE);
                }
            }
            DesayLog.d(" data_key = "+ data_key +",data_value= "+data_value);
        }else{//不包含“：”
            DesayLog.d("data = "+data);
            if(data.equals(CMD_BAND_PHOTO)){
                //shot now
                DesayLog.d("shot now!");
//                    BLEContentProvider.setNotifyTakePictrueCMD(BLEContentProvider.NT_TAKE_PICTRUE_CMD_SHOT);
                mOnBLECallBackListener.OnCameraShotCallBack(KEY_CAMERA_SHOT);
            }else if(data.equals(BAND_MUSIC_CMD_PLAY)){
                DesayLog.d("BAND_MUSIC_CMD_PLAY!");
//                    BLEContentProvider.setNotifyMusicCMD(BLEContentProvider.NT_MUSIC_PLAY);
                mOnBLECallBackListener.OnMusicCtrolCallBack(KEY_MUSIC_NOTIFY,BLEContentProvider.NT_MUSIC_PLAY);
            }else if(data.equals(BAND_MUSIC_CMD_PAUSE)){
                DesayLog.d("BAND_MUSIC_CMD_PAUSE!");
//                    BLEContentProvider.setNotifyMusicCMD(BLEContentProvider.NT_MUSIC_PAUSE);
                mOnBLECallBackListener.OnMusicCtrolCallBack(KEY_MUSIC_NOTIFY,BLEContentProvider.NT_MUSIC_PAUSE);
            }else if(data.equals(BAND_MUSIC_CMD_NEXT)){
                DesayLog.d("BAND_MUSIC_CMD_NEXT!");
//                    BLEContentProvider.setNotifyMusicCMD(BLEContentProvider.NT_MUSIC_NEXT);
                mOnBLECallBackListener.OnMusicCtrolCallBack(KEY_MUSIC_NOTIFY,BLEContentProvider.NT_MUSIC_NEXT);
            }else if(data.equals(BAND_MUSIC_CMD_PRE)){
                DesayLog.d("BAND_MUSIC_CMD_PRE!");
//                    BLEContentProvider.setNotifyMusicCMD(BLEContentProvider.NT_MUSIC_PRE);
                mOnBLECallBackListener.OnMusicCtrolCallBack(KEY_MUSIC_NOTIFY,BLEContentProvider.NT_MUSIC_PRE);
            }else if(data.equals(BAND_FIND_PHONE_CMD)){
                DesayLog.d("BAND_FIND_PHONE_CMD!");
//                    BLEContentProvider.setNotifyFindPhoneCMD(BLEContentProvider.NT_FIND_PHONE_CMD_FIND);
                mOnBLECallBackListener.OnFindPhoneCallBack(KEY_FIND_PHONE_NOTIFY);
            }
        }
    }


    private boolean WriteCMD(byte[] mByte,int uuid) {
        UUID writeUUID = null;
        UUID serviceUUID = null;

        switch(uuid){
            case BLEContentProvider.SERVER_A_UUID_REQUEST_INT:
                writeUUID = BLEContentProvider.SERVER_A_UUID_REQUEST;
                serviceUUID = BLEContentProvider.SERVER_A_UUID_SERVER;
                break;
            case BLEContentProvider.SERVER_B_UUID_REQUEST_INT:
                writeUUID = BLEContentProvider.SERVER_B_UUID_REQUEST;
                serviceUUID = BLEContentProvider.SERVER_B_UUID_SERVER;
                break;
            default:
                break;
        }
        if(writeUUID==null){
            return false;
        }

        boolean result = false;
        if (Instance == null) {
            return false;
        }
        if(mBluetoothGatt ==null){
            return false;
        }

        List<BluetoothGattService> bluetoothGattServices = mBluetoothGatt.getServices();
        //BluetoothGattService service = mBluetoothGatt.getService(serviceUUID); //mine
        //characteristic = service.getCharacteristic(writeUUID);
        if (bluetoothGattServices == null) {
            return false;
        }
        /*BluetoothGattService service = gatt.getService(UUID.fromString(GattConstants.SERVICE_UUID));
        BluetoothGattCharacteristic writeCharacteristic_1 =
                service.getCharacteristic(UUID.fromString(GattConstants.CHARACTERISTIC_WRITE_UUID_1));*///针对uuid获得一个可携带16进制字符数组的characteristic
        BluetoothGattService bluetoothGattService;//获取服务的时候可以根据uuid进行筛选
        for (int i = 0; i < bluetoothGattServices.size(); i++) {
            bluetoothGattService = bluetoothGattServices.get(i);
            int size = bluetoothGattService.getCharacteristics().size();
            for (int j = 0; j < size; j++) {
                String string = bluetoothGattService.getCharacteristics().get(j).getUuid().toString();
                if (string.equals(writeUUID.toString())) {//if have REQUEST_UUID//根据区分uuid 来获得characteristic 在看了purifit的代码之后觉得不必要
                    BluetoothGattCharacteristic characteristic;
                    characteristic = bluetoothGattService.getCharacteristics().get(j);
                    if (Instance != null) {
                        result =writeCharacteristic(mBluetoothGatt, characteristic, mByte);
                    }
                }
            }
        }
        return result;
    }

    private boolean writeCharacteristic(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic,
                                        byte[] value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return false;
        }

        if (characteristic == null) {
            return false;
        }

        characteristic.setValue(value);
        boolean result = mBluetoothGatt.writeCharacteristic(characteristic);//ble写入api
        return result;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     * This method can only be used at your app destroy
     */
    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private void readDsc(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            DesayLog.d( "BluetoothAdapter not initialized");
            return;
        }
        if (descriptor != null) {
            mBluetoothGatt.readDescriptor(descriptor);
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address
     *            The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The
     *         connection result is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if(mConnectionState == BLEContentProvider.STATE_CONNECTING ){
            Toast.makeText(mContext, "STATE CONNECTING", Toast.LENGTH_LONG).show();
            mOnBLECallBackListener.OnConnectCallBack(KEY_CONNECT_STATUS,BLEContentProvider.STATE_CONNECTING);
            return false;
        }

        if( mConnectionState ==BLEContentProvider.STATE_CONNECTED ){
            mOnBLECallBackListener.OnConnectCallBack(KEY_CONNECT_STATUS,BLEContentProvider.STATE_CONNECTED);
            return false;
        }

        DesayLog.d("connect start");

        if (mBluetoothAdapter == null || address == null) {
            DesayLog.e("BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            return false;
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);//获得一个设备实例
        if (device == null) {
            DesayLog.e("Device not found.  Unable to connect.");
            return false;
        }


        String DeviceSelector = device.getName().substring(0,2);//get "b1" or "b5"
        DesayLog.e("DeviceSelector = " + DeviceSelector);
        if(DeviceSelector.equals(DialogSeriesDevices.DIALOG_DEVICE_NAME_FLAG)){
                 setDeviceCMD(DialogSeriesDevices.DIALOG_DEVICE);
        }else if(DeviceSelector.equals(NordicSeriesDevices.NORDIC_DEVICE_NAME_FLAG)){
                 setDeviceCMD(NordicSeriesDevices.NORDIC_DEVICE);
        }

        device.connectGatt(mContext, false, mGattCallback);//mGattCallback发送数据后返回数据的回调。
        mConnectionState = BLEContentProvider.STATE_CONNECTING;
        handleConnectState();
        return true;
    }

    private void setDeviceCMD(int device){
    if(device==10){//dialog device
        setDialogCMD();
    }else{//nordic device
        setNordicCMD();
    }
    }

    private void setDialogCMD(){
        Current_Device = DialogSeriesDevices.DIALOG_DEVICE;
        DesayLog.e("setDialogCMD() ");
        CMD_BOND = DialogSeriesDevices.CMD_BOND;//default nordic divice
        CMD_BAND_VERSION = DialogSeriesDevices.CMD_BAND_VERSION;//default nordic divice
        CMD_SYN_TIME = DialogSeriesDevices.CMD_SYN_TIME;//default nordic divice
        CMD_SET_CLOCK_ONE = DialogSeriesDevices.CMD_SET_CLOCK_ONE;//default nordic divice
        CMD_SET_CLOCK_TWO = DialogSeriesDevices.CMD_SET_CLOCK_TWO;//default nordic divice
        CMD_BATTERY = DialogSeriesDevices.CMD_BATTERY;//default nordic divice
        CMD_STEP = DialogSeriesDevices.CMD_STEP;//default nordic divice
        CMD_SIT_SETTINGS = DialogSeriesDevices.CMD_SIT_SETTINGS;//default nordic divice
        CMD_PHONE_CALL = DialogSeriesDevices.CMD_PHONE_CALL;//default nordic divice
        CMD_PUSH_CH = DialogSeriesDevices.CMD_PUSH_CH;//default nordic divice
        CMD_SETSPORT_AIM = DialogSeriesDevices.CMD_SETSPORT_AIM;//default nordic divice
        CMD_HANDUP = DialogSeriesDevices.CMD_HANDUP;//default nordic divice
        CMD_SYN_TIMELY_STEPS = DialogSeriesDevices.CMD_SYN_TIMELY_STEPS;//default nordic divice
        CMD_FIND_BAND = DialogSeriesDevices.CMD_FIND_BAND;//default nordic divice
        CMD_REQUEST_DATA = DialogSeriesDevices.CMD_REQUEST_DATA;//default nordic divice
        TIMELY_STEPS_NOTIF = DialogSeriesDevices.TIMELY_STEPS_NOTIF;//default nordic divice
        CMD_BAND_PHOTO = DialogSeriesDevices.CMD_BAND_PHOTO;//default nordic divice
    }
    private void setNordicCMD(){
        Current_Device = NordicSeriesDevices.NORDIC_DEVICE;
        DesayLog.e("setNordicCMD()  ");
        CMD_BOND = NordicSeriesDevices.CMD_BOND;//default nordic divice
        CMD_BAND_USER = NordicSeriesDevices.CMD_BAND_USER;//default nordic divice
        CMD_BAND_VERSION = NordicSeriesDevices.CMD_BAND_VERSION;//default nordic divice
        CMD_SYN_TIME = NordicSeriesDevices.CMD_SYN_TIME;//default nordic divice
        CMD_SET_CLOCK_ONE = NordicSeriesDevices.CMD_SET_CLOCK_ONE;//default nordic divice
        CMD_SET_CLOCK_TWO = NordicSeriesDevices.CMD_SET_CLOCK_TWO;//default nordic divice
        CMD_BATTERY = NordicSeriesDevices.CMD_BATTERY;//default nordic divice
        CMD_STEP = NordicSeriesDevices.CMD_STEP;//default nordic divice
        CMD_SIT_SETTINGS = NordicSeriesDevices.CMD_SIT_SETTINGS;//default nordic divice
        CMD_PHONE_CALL = NordicSeriesDevices.CMD_PHONE_CALL;//default nordic divice
        CMD_PUSH_CH = NordicSeriesDevices.CMD_PUSH_CH;//default nordic divice
        CMD_SET_LAN = NordicSeriesDevices.CMD_SET_LAN;//default nordic divice
        CMD_SETSPORT_AIM = NordicSeriesDevices.CMD_SETSPORT_AIM;//default nordic divice
        CMD_HANDUP = NordicSeriesDevices.CMD_HANDUP;//default nordic divice
        CMD_SYN_TIMELY_STEPS = NordicSeriesDevices.CMD_SYN_TIMELY_STEPS;//default nordic divice
        CMD_SET_SYN_FLAG = NordicSeriesDevices.CMD_SET_SYN_FLAG;//default nordic divice
        CMD_SYN_MUSICPLAY_STATE = NordicSeriesDevices.CMD_SYN_MUSICPLAY_STATE;//default nordic divice
        CMD_STATIC_HEART = NordicSeriesDevices.CMD_STATIC_HEART;//default nordic divice
        CMD_FIND_BAND = NordicSeriesDevices.CMD_FIND_BAND;//default nordic divice
        CMD_CAMERA_FLAG = NordicSeriesDevices.CMD_CAMERA_FLAG;//default nordic divice
        CMD_MUSIC = NordicSeriesDevices.CMD_MUSIC;//default nordic divice
        CMD_FIND_PHONE = NordicSeriesDevices.CMD_FIND_PHONE;//default nordic divice
        CMD_SET_DISTANCE_UNITS = NordicSeriesDevices.CMD_SET_DISTANCE_UNITS;//default nordic divice
        CMD_REQUEST_DATA = NordicSeriesDevices.CMD_REQUEST_DATA;//default nordic divice
        TIMELY_STEPS_NOTIF = NordicSeriesDevices.TIMELY_STEPS_NOTIF;//default nordic divice
        CMD_BAND_PHOTO = NordicSeriesDevices.CMD_BAND_PHOTO;//default nordic divice
        BAND_MUSIC_CMD_PLAY = NordicSeriesDevices.BAND_MUSIC_CMD_PLAY;//default nordic divice
        BAND_MUSIC_CMD_PAUSE = NordicSeriesDevices.BAND_MUSIC_CMD_PAUSE;//default nordic divice
        BAND_MUSIC_CMD_NEXT = NordicSeriesDevices.BAND_MUSIC_CMD_NEXT;//default nordic divice
        BAND_MUSIC_CMD_PRE = NordicSeriesDevices.BAND_MUSIC_CMD_PRE;//default nordic divice
        BAND_FIND_PHONE_CMD = NordicSeriesDevices.BAND_FIND_PHONE_CMD;//default nordic divice
    }











}
