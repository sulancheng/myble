package com.susu.hh.mybl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.susu.hh.mybl.utils.CommenUtils;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Administrator on 2016/11/24.
 */
public class BleContror extends BleContrParter{
    private static final int ZHILING_VERSION = 6;
    private static final int ZHILING_MORE_ROOM_ONE= 7;
    private static final int ZHILING_MORE_ROOM_TWO= 8;
    private static final int CHARA_RESPONE = 1;
    private static final int ENABLE_GATT_NOTIFY = 2;
    private static final int ZHILING_BAND = 3;
    private static final int ZHILING_BUSHU = 5;
    private static final int ZHILING_BYTT = 4;
    private Context mContext;



    private BleContror(Context mContext) {
        this.mContext = mContext;
        registbrasted(mContext);
    }
    private void registbrasted(Context mContext) {
        MyLog.i("slcbleconnectliuc", "注册蓝牙状态广播" );
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
//    public void openBluetooth(View v) {
//        // 先获取一个蓝牙适配器对象
//        // 判断设备是否支持蓝牙
//        if (mBluetoothAdapter == null) {
//           // Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
//            return;
//        }
//        // 判断蓝牙是否打开
//        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();  // 直接打开蓝牙
//            //Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            //startActivityForResult(intent, 1);
//        }
//    }//直接打开蓝牙的方法。
    private static BleContror mBleContror;

    public static BleContror getInstance(Context mContext) {
        if (null == mBleContror) {
            mBleContror = new BleContror(mContext);
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







//    public void setOnBLECallBackListener(OnBLECallBackListener lister) {
//        if (lister != null) {
//            mLister = lister;
//        }
//    }
    //private List<OnBLECallBackListener> onBLECallBackListeners = new ArrayList<>();
//    public void addOnBLECallBackListener(OnBLECallBackListener lister) {
//        if (lister != null) {
//            onBLECallBackListeners.add(lister);
//        }
//    }
//    public void removeOnBLECallBackListener(OnBLECallBackListener lister) {
//        if (lister != null) {
//            onBLECallBackListeners.remove(lister);
//        }
//    }


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

//    public void scanLeDevice(boolean enable, long SCAN_PERIOD) {
//        if(!mBluetoothAdapter.isEnabled()){
//            mBluetoothAdapter.enable();  // 直接打开蓝牙
//        }
//        Runnable mRunable = new Runnable() {
//            @Override
//            public void run() {
//                mScanning = false;
//                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            }
//        };
//        mHandler.removeCallbacks(mRunable);
//        if (enable) {
//            mHandler.postDelayed(mRunable
//                    , SCAN_PERIOD);
//            mScanning = true;
//            boolean suc = mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描的方法（接口）
//            if (suc) {
//                connectionState = BleZt.STATE_SCANNING;
//            }
//        } else {
//
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            mBluetoothAdapter.cancelDiscovery();//停止
//
//            if (connectionState == BleZt.STATE_SCANNING) {
//                connectionState = BleZt.STATE_DISCONNECTED;
//            }
//        }
//    }

//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//
//                @Override
//                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
//                    if (device != null) {
//                        if (rssi > -80) {//靠近些再连，保证连接的稳定性
//                        }
//                    }
//                }
//            };

    public void autoconnect(String address) {
        connect(address);
    }
    public void connect(String address) {
        if (mparBluetoothAdapter == null || address == null) {
            MyLog.i("BluetoothAdapter not initialized or unspecified address.");
            return;
        }
        super.address = address;
        MyLog.i("slcbleconnectliuc", "start connect" );
        if (!mparBluetoothAdapter.isEnabled()) {
            return;
        }
        BluetoothDevice device = mparBluetoothAdapter.getRemoteDevice(address);//获得一个设备实例
        String DeviceSelector = device.getName().substring(0, 2);//get "b1" or "b5"
        if (device == null) {
            MyLog.i("Device not found.  Unable to connect.");
            return;
        }
        connectionState = BleZt.STATE_CONNECTING;
        //mGattCallback发送数据后返回数据的回调。
        device.connectGatt(mContext, false, mGattCallback);
        connecttype = Connect.chongl;
        MyLog.i("slcbleconnectliuc", "start connect 2" );
    }

    public enum Connect{
        normal,
        chongl
    }
    public void getBytt() {
        if (mparBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mHandler.obtainMessage(ZHILING_BYTT).sendToTarget();
    }

    public void bingMybind() {
        MyLog.i("bingMybind","coming");
        if (mparBluetoothAdapter == null || mBluetoothGatt == null) {
            MyLog.i("bingMybind","comingtrturn");
            return;
        }
        mHandler.obtainMessage(ZHILING_BAND,"AT+BOND").sendToTarget();
        MyLog.i("bingMybind","comied");
    }
    public void checkBandVersion() {
        if (mparBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mHandler.obtainMessage(ZHILING_VERSION).sendToTarget();
    }
    public void diconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        MyLog.i("slcbleconnectliuc", "mBluetoothGatt disconnect" );
        connecttype = BleContror.Connect.normal;
        mBluetoothGatt.disconnect();
        mBleContror = null;
        mCharacteristic = null;
        //mBluetoothGatt.close();//容易报133错误  这个写了 蓝牙回调就不会调用
    }
    //这里直接接收指令
    public void zhiling(String cmd,int room){
        if (mparBluetoothAdapter == null || mBluetoothGatt == null) {
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
            return mparBluetoothAdapter;
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
