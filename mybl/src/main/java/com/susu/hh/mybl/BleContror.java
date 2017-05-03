package com.susu.hh.mybl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.susu.hh.mybl.utils.CommenUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2016/11/24.
 */
public class BleContror extends BleContrParter{


    private static final int ZHILING_VERSION = 6;
    private static final int ZHILING_MORE_ROOM_ONE= 7;
    private static final int ZHILING_MORE_ROOM_TWO= 8;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothGatt mBluetoothGatt;
    public BluetoothGatt getBlueToothGatt(){
        return mBluetoothGatt;
    }
    private BleContror(Context mContext, BluetoothAdapter mBluetoothAdapter) {
        this.mContext = mContext;
        this.mBluetoothAdapter = mBluetoothAdapter;
        registbrasted(mContext);
    }
    private void registbrasted(Context mContext) {
        String [] actions = {"android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED",
                android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED,
                android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED,
                android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED,
                "android.bluetooth.BluetoothAdapter.STATE_OFF",
                "android.bluetooth.BluetoothAdapter.STATE_ON",
                BluetoothAdapter.ACTION_STATE_CHANGED,
                BluetoothAdapter.EXTRA_PREVIOUS_STATE};
        MyBroadCast mybroadcast = new MyBroadCast();
        CommenUtils.registBroad(mContext,actions,mybroadcast);
    }
    public void openBluetooth(View v) {
        // 先获取一个蓝牙适配器对象
        // 判断设备是否支持蓝牙
        if (mBluetoothAdapter == null) {
           // Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        // 判断蓝牙是否打开
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();  // 直接打开蓝牙
            //Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(intent, 1);
        }
    }//直接打开蓝牙的方法。
    private static BleContror mBleContror;
    private  BleZt connectionState;

    public enum BleZt{
        STATE_DISCONNECTED,STATE_SCANNING,
        STATE_CONNECTING,
        STATE_CONNECTED,STATE_DISCOVERED,
        ACTION_ACL_DISCONNECTED  //断开
    }
    public static BleContror getInstance(Context mContext, BluetoothAdapter mBluetoothAdapter) {
        if (null == mBleContror) {
            mBleContror = new BleContror(mContext, mBluetoothAdapter);
        }
        return mBleContror;
    }
    public static BleContror getInstance() {
        if (null == mBleContror) {
            return null;
        }
        return mBleContror;
    }
    public BleZt getState(){
       return connectionState;
    }
    private boolean mScanning = false;
    private static final int CHARA_RESPONE = 1;
    private static final int ENABLE_GATT_NOTIFY = 2;
    private static final int ZHILING_BAND = 3;
    private static final int ZHILING_BUSHU = 5;
    private static final int ZHILING_BYTT = 4;
    private int uuid_a  = 1;
    private int uuid_b  = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case CHARA_RESPONE://change的响应
                    String res = (String) msg.obj;
                    MyLog.i("response", res);//OK成功  AT+BOND:OK
                    handleData(res);
                    break;
                case ENABLE_GATT_NOTIFY:
                    BluetoothGatt mBluetoothGatt = (BluetoothGatt) msg.obj;
                    boolean NotifiResult = BleUtils.setCharacteristicNotification(mBluetoothAdapter,mBluetoothGatt, mBluetoothGatt.getServices(), true, 1);
                    //boolean NotifiResult = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    MyLog.i("NotifiResult", NotifiResult + "==");
                    mHandler.sendEmptyMessageDelayed(ZHILING_BAND,500);//绑定
                    break;
                case ZHILING_BAND:
//                    BleUtils.addCMD("AT+BOND", 1);
                    BleUtils.addCMDbyte("AT+BOND".getBytes(), 1);
                    break;
                case ZHILING_BYTT:
                    MyLog.i("zhiling", BleUuidConstant.CMD_BATTERY);
                    BleUtils.addCMD(BleUuidConstant.CMD_BATTERY, 1);
                    break;
                case ZHILING_VERSION:
                    MyLog.i("zhiling", BleUuidConstant.CMD_BAND_VERSION);
                    BleUtils.addCMD(BleUuidConstant.CMD_BAND_VERSION, uuid_a);
                    break;
                case ZHILING_MORE_ROOM_ONE:
                    String cmd = (String) msg.obj;
                    MyLog.i("zhilingMORE", cmd);
                    BleUtils.addCMD(cmd, uuid_a);
                    break;
                case ZHILING_MORE_ROOM_TWO:
                    String cmd_two = (String) msg.obj;
                    MyLog.i("zhilingMORE_two", BleUuidConstant.CMD_BAND_VERSION);
                    BleUtils.addCMD(cmd_two, uuid_b);
                    break;
            }
        }
    };



    public interface OnBLECallBackListener {
        void bindResp(boolean isBind, String data);
    }

    private OnBLECallBackListener mLister;

    public void setOnBLECallBackListener(OnBLECallBackListener lister) {
        if (lister != null) {
            mLister = lister;
        }
    }
    private List<OnBLECallBackListener> onBLECallBackListeners = new ArrayList<>();
    public void addOnBLECallBackListener(OnBLECallBackListener lister) {
        if (lister != null) {
            onBLECallBackListeners.add(lister);
        }
    }
    public void removeOnBLECallBackListener(OnBLECallBackListener lister) {
        if (lister != null) {
            onBLECallBackListeners.remove(lister);
        }
    }
    private void handleData(String data) {
        //AT+BOND:OK
        if(mLister!=null){
            mLister.bindResp(true, data);
        }
        for (OnBLECallBackListener onBLECallBackListener: onBLECallBackListeners){
            if(onBLECallBackListeners!=null){
                onBLECallBackListener.bindResp(true,data);
            }
        }
       /* String data_arry[] = data.split(":");
        String data_key = data_arry[0];
        String data_value = data_arry[1];
        if (data_key.equals(BleUuidConstant.CMD_BAND_VERSION)) {//check band version
//                    BLEContentProvider.setBandVersion(data_value);
            if (data_value.toLowerCase().contains("err")) {
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_VERSION,false,data_value);
            } else {
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_VERSION,true,data_value);
            }
        } else if (data_key.equals(BleUuidConstant.CMD_BOND)) {//bond band
            MyLog.i("data_value", data_value);
            if (data_value.toLowerCase().equals("ok")) {
                mLister.bindResp(true, data);
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_BOND,true,FUNCTION_ENABLE);
            } else if (data_value.toLowerCase().equals("err")) {
                mLister.bindResp(false, data);
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_BOND,false,FUNCTION_ENABLE);
            }
        }*/
    }

//    private boolean setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
//                                                  List<BluetoothGattService> gattServices, boolean enabled, int witch) {
//        if (mBluetoothAdapter == null || gattServices == null) {
//            return false;
//        }
//        UUID uuid = null;
//        switch (witch) {
//            case 1:
//                uuid = BleUuidConstant.SERVER_A_UUID_NOTIFY;//"00000002-0000-1000-8000-00805f9b34fb"
//                break;
//            case 3:
//                uuid = BleUuidConstant.SERVER_B_UUID_NOTIFY;
//                break;
//            default:
//                break;
//        }
//        BluetoothGattDescriptor descriptor = null;
//        boolean NotifiResult = false;
//        for (BluetoothGattService gattService : gattServices) {
//            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                if (BleUuidConstant.SERVER_A_UUID_NOTIFY.toString().equals(gattCharacteristic.getUuid().toString()) ||
//                        BleUuidConstant.SERVER_B_UUID_NOTIFY.toString().equals(gattCharacteristic.getUuid().toString())) {
//                    mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, enabled);//打开响应
//                    if (BleUuidConstant.SERVER_A_UUID_NOTIFY.toString().equals(gattCharacteristic.getUuid().toString())) {
//                        //设置描述
//                        if (gattCharacteristic != null) {
//                            //这里获取的描述是一样的  通用的   除非特殊
//                            descriptor = gattCharacteristic.getDescriptor(BleUuidConstant.SERVER_A_UUID_DESCRIPTOR);
//                        } else {
//                            return false;
//                        }
//                        if (descriptor != null) {
//                        /*descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00,
//                                0x00});*/
//                            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                            NotifiResult = mBluetoothGatt.writeDescriptor(descriptor);
//                        } else {
//                            return false;
//                        }
//                    }
//                }
//            }
//        }
//        return NotifiResult;
//    }

    public void scanLeDevice(boolean enable, long SCAN_PERIOD) {
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();  // 直接打开蓝牙
        }
        Runnable mRunable = new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        };
        mHandler.removeCallbacks(mRunable);
        if (enable) {
            mHandler.postDelayed(mRunable
                    , SCAN_PERIOD);
            mScanning = true;
            boolean suc = mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描的方法（接口）
            if (suc) {
                connectionState = BleZt.STATE_SCANNING;
            }
        } else {

            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.cancelDiscovery();//停止

            if (connectionState == BleZt.STATE_SCANNING) {
                connectionState = BleZt.STATE_DISCONNECTED;
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    if (device != null) {
                        if (rssi > -80) {//靠近些再连，保证连接的稳定性
                        }
                    }
                }
            };

    public void autoconnect(String address) {
        connect(address);
    }
    public void connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
            MyLog.i("BluetoothAdapter not initialized or unspecified address.");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);//获得一个设备实例
        String DeviceSelector = device.getName().substring(0, 2);//get "b1" or "b5"
        if (device == null) {
            MyLog.i("Device not found.  Unable to connect.");
            return;
        }
        connectionState = BleZt.STATE_CONNECTING;
        //mGattCallback发送数据后返回数据的回调。
        device.connectGatt(mContext, false, mGattCallback);
    }

    public void getBytt() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mHandler.obtainMessage(ZHILING_BYTT).sendToTarget();
    }

    public void bingMybind() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mHandler.obtainMessage(ZHILING_BAND).sendToTarget();
    }
    public void checkBandVersion() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mHandler.obtainMessage(ZHILING_VERSION).sendToTarget();
    }
    public void diconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        mBleContror = null;

        //mBluetoothGatt.close();//容易报133错误  这个写了 蓝牙回调就不会调用
    }
    //这里直接接收指令
    public void zhiling(String cmd,int room){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        if(room == 1){
            mHandler.obtainMessage(ZHILING_MORE_ROOM_ONE,cmd).sendToTarget();
        }else if(room == 2){
            mHandler.obtainMessage(ZHILING_MORE_ROOM_TWO,cmd).sendToTarget();
        }
    }



//    private static synchronized void addCMD(byte[] cmdCodeBytes, int write_UUID) {//举例设置闹钟。进行了分包
//        //purifit中 的分包的方法
//        int packageNum = (cmdCodeBytes.length + 19) / 20;
//        for (int i = 0; i < packageNum; i++) {
//            int start = i * 20;
//            int end = Math.min(start + 20, cmdCodeBytes.length);
//            byte[] buffer = Arrays.copyOfRange(cmdCodeBytes, start, end);
//            sendCMD(buffer, write_UUID);
//        }
//    }

//    private  synchronized static void sendCMD(byte[] writeByte, int write_uuid) {
//        String write = null;
//        if(write_uuid == 1){
//            write = BleUuidConstant.SERVER_A_UUID_REQUEST.toString();
//        }else if(write_uuid == 2){
//            write = BleUuidConstant.SERVER_B_UUID_REQUEST.toString();
//        }
//        List<BluetoothGattService> bluetoothGattServices = mBluetoothGatt.getServices();//我们没有用serviceuuid自己循环。
//        MyLog.i("bluetoothGattServices", bluetoothGattServices.toString());
//        if (bluetoothGattServices == null) {
//            return;
//        }
//        BluetoothGattService bluetoothGattService;//获取服务的时候可以根据uuid进行筛选
//        BluetoothGattCharacteristic characteristic;
//        for (int i = 0; i < bluetoothGattServices.size(); i++) {
//            bluetoothGattService = bluetoothGattServices.get(i);
//            int size = bluetoothGattService.getCharacteristics().size();
//            for (int j = 0; j < size; j++) {
//                String string = bluetoothGattService.getCharacteristics().get(j).getUuid().toString();
//                MyLog.i("bluetoothGattServicesand--", string);
//                if (string.equals(write)) {//if have REQUEST_UUID//根据区分uuid 来获得characteristic 在看了purifit的代码之后觉得不必要
//                    MyLog.i("bluetoothGattServicesand", string);
//                    characteristic = bluetoothGattService.getCharacteristics().get(j);
//                    characteristic.setValue(writeByte);
//                    mBluetoothGatt.writeCharacteristic(characteristic);
//                }
//            }
//        }
//    }

    private void sendCMDTwo(byte[] writeByte) {
        UUID serviceUUID = BleUuidConstant.SERVER_A_UUID_SERVER;
        UUID charactUUID = BleUuidConstant.SERVER_A_UUID_REQUEST;  //write
        UUID descriptorUUID = null;
        BluetoothGattService service = null;
        if (serviceUUID != null && mBluetoothGatt != null) {
            service = mBluetoothGatt.getService(serviceUUID);
           /* for(BluetoothGattService mService : mBluetoothGatt.getServices()){
                if(mService.getUuid().toString().equals(serviceUUID.toString())){
                    service = mService;
                }
            }*/
        }
        BluetoothGattCharacteristic characteristic = null;
        BluetoothGattDescriptor descriptor = null;
        if (service != null && charactUUID != null) {
            characteristic = service.getCharacteristic(charactUUID);
        }
        if (characteristic != null && descriptorUUID != null) {
            descriptor = characteristic.getDescriptor(descriptorUUID);
        }
        //写值
        characteristic.setValue(writeByte);
        mBluetoothGatt.writeCharacteristic(characteristic);

    }

    //对外提供一个获取mBluetoothGatt的方法
    public BluetoothAdapter getmBluetoothGatt(){
        if(mBluetoothGatt!= null){
            return mBluetoothAdapter;
        }
        return null;
    }
    public boolean refreshDeviceCache() {
        MyLog.i("refreshDeviceCache","要准备清除蓝牙缓存");
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod(
                        "refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(
                            localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }

                MyLog.i("refreshDeviceCache","清除蓝牙缓存");
            } catch (Exception localException) {
               // Log.i(TAG, "An exception occured while refreshing device");
            }
        }
        return false;
    }
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            MyLog.i("BluetoothGattCallback", "onConnectionStateChange" +"=-------status = " +status +"-----newState = "+ newState + "====" + gatt);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Attempts to discover services after successful connection.
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//断开
                //蓝牙断开连接时候清除缓存
                //refreshDeviceCache();//通过反射的机制。
                connectionState = BleZt.STATE_DISCONNECTED;
                mBluetoothGatt.close();
                //mBluetoothGatt = null;

                //在这里可以发送一个连接失败的广播  android ble机制中有丰富的关于蓝牙的广播

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            MyLog.i("BluetoothGattCallbacks", "onServicesDiscovered" + status + "====" + gatt);
            if (status == BluetoothGatt.GATT_SUCCESS) {//找到服务
                mBluetoothGatt = gatt;
                connectionState = BleZt.STATE_CONNECTED;


                //这时候可以发一个连接成功的广播status = 3

                if (mBluetoothGatt != null) {
                    //setCharacteristicNotification(mBluetoothGatt,mBluetoothGatt.getServices(),true);
                    mHandler.obtainMessage(ENABLE_GATT_NOTIFY, mBluetoothGatt).sendToTarget();
                }
            } else {
                connectionState = BleZt.STATE_DISCONNECTED;
                gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //readCharacteristic与 onCharacteristicRead回调
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }

                MyLog.i("onCharacteristicRead , ble_address=" + gatt.getDevice().getAddress() + ",status= " + status + " read = " + stringBuilder);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            MyLog.i("------------->onCharacteristicWrite received: " + status);
            // TODO Auto-generated method stub
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                //这个是跟获取蓝牙传回来的方法一样  可以讲字节数组变成字符串。 write = AT+BOND
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data) {
                        if (byteChar != 0x0d && byteChar != 0x0a) {
                            stringBuilder.append((char) byteChar);
                        }
                    }
                    MyLog.i("onCharacteristicWriteUUID =" + characteristic.getUuid().toString() + "  write = " + stringBuilder.toString());
                }
                                                   //十进制：65
                //这个可以将我们具体发送过去的指令打印出来 write  41 54 2B 42 4F 4E 44 0D 0A（16进制 ascii 查）
//                StringBuilder stringBuilder = null;
//                if (data != null && data.length > 0) {
//                    stringBuilder = new StringBuilder(data.length);
//                    for (byte byteChar : data)
//                        stringBuilder.append(String.format("%02X ", byteChar));//转成16进制的字节。
//                }
//                MyLog.i("onCharacteristicWriteUUID =" + characteristic.getUuid().toString() + "  write  " + stringBuilder.toString());
            }
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            MyLog.i("------------->onDescriptorRead received: " + status + "  descriptor =" + descriptor.getValue());
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
            MyLog.i("------------->onDescriptorWrite received: " + status + ",descriptor.uuid = " + descriptor.getCharacteristic().getUuid().toString());
            // TODO Auto-generated method stub
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            MyLog.i("------------->onReliableWriteCompleted received: " + status);
            // TODO Auto-generated method stub
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            //Log.w("ellison1", "------------->onReadRemoteRssi received: " + status +"  rssi="+rssi);
            MyLog.i("------------->onReadRemoteRssi received: " + status + "  rssi=" + rssi);
            // TODO Auto-generated method stub
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //setCharacteristicNotification与 onCharacteristicChanged回调
            String uuid = characteristic.getUuid().toString();
            MyLog.i("onCharacteristicChanged", uuid);
            MyLog.i("onCharacteristicChanged", characteristic.getStringValue(0) + "===" + characteristic.getValue());
            if (uuid.equals(BleUuidConstant.SERVER_A_UUID_NOTIFY.toString()) || uuid.equals(BleUuidConstant.SERVER_B_UUID_NOTIFY.toString())) {
                byte[] data = characteristic.getValue();
                MyLog.i("characteristic.getValue", characteristic.getStringValue(0) + "===" + characteristic.getValue());
                MyLog.i("characteristic.getValuearrays", Arrays.toString(data));
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data) {
                        if (byteChar != 0x0d && byteChar != 0x0a) {
                            stringBuilder.append((char) byteChar);
                        }
                    }
                }
               /* if(synDataStart){//如果是同步数据处理
                    Bundle mBundle = new Bundle();
                    Message msg = Message.obtain(writeValueHandler, DATA_ANALYST);
                    mBundle.putByteArray(EXTRA_BYTES, data);
                    msg.setData(mBundle);
                    msg.sendToTarget();
                }else{*/
                MyLog.i("data.size = " + data.length + " notify_content = " + stringBuilder.toString());
                mHandler.obtainMessage(CHARA_RESPONE, stringBuilder.toString()).sendToTarget();
                //}

            }
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    class MyBroadCast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.i("onReceive", action);
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            MyLog.i("onReceivestate", state + "");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Toast.makeText(mContext, "蓝牙状态改变广播 !", Toast.LENGTH_SHORT).show();
            // Log.i("autoconnect","调用");
            //autoconnect("D0:7E:A3:AC:04:D4");//解绑酒吧mac清除。 不然 就一直有可以不停的重新绑定
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(mContext, device.getName() + " 设备已发现！！", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(mContext, device.getName() + "已连接", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Toast.makeText(mContext, device.getName() + "正在断开蓝牙连接。。。", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                connectionState = BleZt.ACTION_ACL_DISCONNECTED;
                Toast.makeText(mContext, device.getName() + "蓝牙连接已断开！！！", Toast.LENGTH_SHORT).show();
            } else if (state==(BluetoothAdapter.STATE_OFF)) {
                Toast.makeText(mContext, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                connectionState = BleZt.ACTION_ACL_DISCONNECTED;
            } else if (state==(BluetoothAdapter.STATE_ON)) {
                Toast.makeText(mContext, "蓝牙打开", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
