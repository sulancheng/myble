package com.susu.hh.mybl;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/11/23.
 */
public class BleService extends Service {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    //private BleContror mBleContror;
    private BleContrParter bleContrpartInstance;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        initialize();
        return new MyBinder();
    }

    private void initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        //mBleContror = BleContror.getInstance(this, mBluetoothAdapter);
        //mBleContror.setOnBLECallBackListener(listener);
    }

    public void getblecontr(String blename) {
        if(mBluetoothAdapter == null){
            return;
        }
        //在这里区分设备
        //多态根据不同的设备名 new 不同的子类。  父类持有子类的对象。 调用父类抽象方法 去寻找实现该抽象方法的子类的方法。
        bleContrpartInstance = BleContrParter.getBleContrpartInstance(blename, this);
//        if(!"01".equals(blename)){
//            Log.i("getblecontr","得到getblecontr");
//            mBleContror = BleContror.getInstance(this, mBluetoothAdapter);
//        }else{
//            Toast.makeText(this, "暂时没有设备", Toast.LENGTH_LONG).show();
//        }
    }

    //对外的回调 先必须绑定服务
    public void setBleContrParter(BleContror.OnBLECallBackListener listener, String blename) {
        if (!"01".equals(blename)) {
            MyLog.i("bleservice","set OnBLECallBackListener");
            if (bleContrpartInstance != null) {
                bleContrpartInstance.setOnBLECallBackListener(listener);
            }
        }

    }

    /* BleContror.OnBLECallBackListener  listener = new BleContror.OnBLECallBackListener() {
          @Override
          public void bindResp(boolean isBind) {
              MyLog.i("bandding",isBind+"");
          }
      };*/
    class MyBinder extends Binder implements Iservice {
        public BleService getService() {
            MyLog.i("TAG", "getService ---> " + BleService.this);
            return BleService.this;
        }

        @Override
        public void callScanLeDevice(boolean enable, long SCAN_PERIOD) {
            //scanLeDevice(enable, SCAN_PERIOD);
        }

        @Override
        public void callTest() {
            MyLog.i("callTest", "callTestcallTestcallTest");
        }
    }

    private static final int CHARA_RESPONE = 1;
    private static final int ENABLE_GATT_NOTIFY = 2;
    private boolean mScanning = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case CHARA_RESPONE://change的响应
                    String res = (String) msg.obj;
                    MyLog.i("receiver response", res);//OK成功  AT+BOND:OK    用来看回复的内容
                    handleData(res);
                    break;
                case ENABLE_GATT_NOTIFY:
                   /* BluetoothGatt mBluetoothGatt = (BluetoothGatt) msg.obj;
                    boolean NotifiResult = setCharacteristicNotification(mBluetoothGatt, mBluetoothGatt.getServices(), true, 1);
                    //boolean NotifiResult = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    MyLog.i("NotifiResult", NotifiResult + "==");*/
                    break;
            }
        }
    };

    private void handleData(String data) {
        //AT+BOND:OK
        String data_arry[] = data.split(":");
        String data_key = data_arry[0];
        String data_value = data_arry[1];
        if (data_key.equals(BleUuidConstant.CMD_BAND_VERSION)) {//check band version
//                    BLEContentProvider.setBandVersion(data_value);
            if (data_value.toLowerCase().contains("error")) {
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_VERSION,false,data_value);
            } else {
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_VERSION,true,data_value);
            }
        } else if (data_key.equals(BleUuidConstant.CMD_BOND)) {//bond band
            if (data_value.toLowerCase().equals("ok")) {
//                        BLEContentProvider.setBondState(true);
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_BOND,true,FUNCTION_ENABLE);
            } else if (data_value.toLowerCase().equals("err")) {
//                        BLEContentProvider.setBondState(false);
                //mOnBLECallBackListener.OnCallBack(KEY_BAND_BOND,false,FUNCTION_ENABLE);
            }
        }
    }

//    private boolean setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
//                                                  List<BluetoothGattService> gattServices, boolean enabled, int witch) {
//        if (mBluetoothAdapter == null || gattServices == null) {
//            return false;
//        }
//        UUID uuid = null;
//        switch (witch) {
//            case BleUuidConstant.SERVER_A_UUID_REQUEST_INT:
//                uuid = BleUuidConstant.SERVER_A_UUID_NOTIFY;
//                break;
//            case BleUuidConstant.SERVER_B_UUID_REQUEST_INT:
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
//                if (uuid.toString().equals(gattCharacteristic.getUuid().toString())) {
//                    mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, enabled);
//                    if (gattCharacteristic != null) {
//                        descriptor = gattCharacteristic.getDescriptor(BleUuidConstant.SERVER_A_UUID_DESCRIPTOR);
//                    } else {
//                        return false;
//                    }
//                    if (descriptor != null) {
//                        descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00,
//                                0x00});
//                        // descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                        NotifiResult = mBluetoothGatt.writeDescriptor(descriptor);
//                    } else {
//                        return false;
//                    }
//                }
//            }
//        }
//        return NotifiResult;
//    }

//    public void scanLeDevice(boolean enable, long SCAN_PERIOD) {
//        if (enable) {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                }
//            }, SCAN_PERIOD);
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描的方法（接口）
//        } else {
//            mScanning = false;
//            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            mBluetoothAdapter.cancelDiscovery();//停止
//        }
//    }


//    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//        @Override
//        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            MyLog.i("addresswode  = " + device.getAddress() + ",rssi =wode " + rssi);//返回扫描到的蓝牙设备
//            if (device != null && rssi > -80) {
//            }
//        }
//    };


    //    public void bindMyBand() {
//        if (mBleContror == null || mBluetoothAdapter == null) {
//            return;
//        }
//        mBleContror.bandMyband();
//    }
//    public void checkBandVersion() {
//        if (mBleContror == null || mBluetoothAdapter == null) {
//            return;
//        }
//        mBleContror.checkBandVersion();
//    }
    public void disconnect() {
        if (bleContrpartInstance == null || mBluetoothAdapter == null) {
            return;
        }
        /*mBluetoothGatt.disconnect();
        mBluetoothGatt.close();*/
        bleContrpartInstance.diconnect();
    }

//    public void getBytt() {
//        if (mBleContror == null || mBluetoothAdapter == null) {
//            return;
//        }
//        mBleContror.getBytt();
//    }

    public void connect(String address) {
        if (bleContrpartInstance != null || mBluetoothAdapter != null) {
            bleContrpartInstance.connect(address);
        }
    }

//    public void autoconnect(String address) {
//        if (mBleContror != null || mBluetoothAdapter != null) {
//            mBleContror.autoconnect(address);
//        }
//    }

    /*private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            MyLog.i("BluetoothGattCallback", "onConnectionStateChange" + status + newState + "====" + gatt);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Attempts to discover services after successful connection.
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close();
                mBluetoothGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            MyLog.i("BluetoothGattCallbacks", "onServicesDiscovered" + status + "====" + gatt);
            if (status == BluetoothGatt.GATT_SUCCESS) {//找到服务
                mBluetoothGatt = gatt;
                if (mBluetoothGatt != null) {
                    //setCharacteristicNotification(mBluetoothGatt,mBluetoothGatt.getServices(),true);
                    mHandler.obtainMessage(ENABLE_GATT_NOTIFY, mBluetoothGatt).sendToTarget();
                }
            } else {
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
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }
                MyLog.i("UUID =" + characteristic.getUuid().toString() + "  write  " + stringBuilder);
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
                StringBuilder stringBuilder = null;
                if (data != null && data.length > 0) {
                    stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data) {
                        if (byteChar != 0x0d && byteChar != 0x0a) {
                            stringBuilder.append((char) byteChar);
                        }
                    }
                }
               *//* if(synDataStart){//如果是同步数据处理
                    Bundle mBundle = new Bundle();
                    Message msg = Message.obtain(writeValueHandler, DATA_ANALYST);
                    mBundle.putByteArray(EXTRA_BYTES, data);
                    msg.setData(mBundle);
                    msg.sendToTarget();
                }else{*//*
                MyLog.i("data.size = " + data.length + " notify_content = " + stringBuilder.toString());
                mHandler.obtainMessage(CHARA_RESPONE, stringBuilder.toString()).sendToTarget();
                //}

            }
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };*/
}
