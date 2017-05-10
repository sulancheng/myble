package com.susu.hh.mybl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by su
 * on 2017/4/19.
 */
public abstract class BleContrParter {
    private static BleContrParter mblecontrpart;
    public  BleZt connectionState;
    public static BluetoothAdapter mparBluetoothAdapter;
    public static BluetoothGatt mBluetoothGatt;
    public enum BleZt{
        STATE_DISCONNECTED,STATE_SCANNING,
        STATE_CONNECTING,
        STATE_CONNECTED,STATE_DISCOVERED,
        ACTION_ACL_DISCONNECTED  //断开
    }
    private static final int ZHILING_VERSION = 6;
    private static final int ZHILING_MORE_ROOM_ONE= 7;
    private static final int ZHILING_MORE_ROOM_TWO= 8;
    private static final int CHARA_RESPONE = 1;
    private static final int ENABLE_GATT_NOTIFY = 2;
    private static final int ZHILING_BAND = 3;
    private static final int ZHILING_BUSHU = 5;
    private static final int ZHILING_BYTT = 4;
    private int uuid_a  = 1;
    private int uuid_b  = 2;
    public Handler mHandler = new Handler() {
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
                    boolean NotifiResult = BleUtils.setCharacteristicNotification(mparBluetoothAdapter,mBluetoothGatt, mBluetoothGatt.getServices(), true, 1);
                    //boolean NotifiResult = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    MyLog.i("notifiresult", NotifiResult + "==");
                    //Message message = mHandler.obtainMessage(ZHILING_BAND, "AT+BOND");
//                    mHandler.sendMessageDelayed(message,500);
//                    mHandler.obtainMessage().sendToTarget();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(mblecontrpart!=null){
                        MyLog.i("bleconptrparter","fasong绑定指令");
                        mblecontrpart.bingMybind();
                    }
                    break;
                case ZHILING_BAND:
                    MyLog.i("bleconptrparter","收到绑定指令");
                    BleUtils.addCMD((String)msg.obj, 1);
                    //BleUtils.addCMDbyte("AT+BOND".getBytes(), 1);
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
    //链接流程走完了  可以用这个了
    public static BleContrParter getBleContrpartInstance(){
        if(null == mblecontrpart){
            return null;
        }
        return mblecontrpart;
    }


    public BluetoothGatt getBlueToothGatt(){
        return mBluetoothGatt;
    }
    public BluetoothGattCharacteristic mCharacteristic;
    public  BluetoothGattCharacteristic getCharacteristic(){
        return mCharacteristic;
    };
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
    //此方法只在绑定服务的时候使用。
    public static BleContrParter getBleContrpartInstance(String blename, BluetoothAdapter mBluetoothAdapter, Context mcontext){
        if(!"01".equals(blename)){
            Log.i("getblecontr","得到getblecontr");
            mparBluetoothAdapter = mBluetoothAdapter;
            mblecontrpart = BleContror.getInstance(mcontext, mBluetoothAdapter);
        }else{
            Toast.makeText(mcontext, "暂时没有设备", Toast.LENGTH_LONG).show();
        }
        return mblecontrpart;
    }
    private BleContror.OnBLECallBackListener mLister;
    public abstract void bingMybind();
    public interface OnBLECallBackListener {
        void bindResp(boolean isBind, String data);
    }
    //public abstract void setOnBLECallBackListener(BleContror.OnBLECallBackListener listener);
    public void setOnBLECallBackListener(BleContrParter.OnBLECallBackListener lister) {
        if (lister != null) {
            mLister = lister;
        }
    }

    //监听者模式
    private List<BleContrParter.OnBLECallBackListener> onBLECallBackListeners = new ArrayList<>();
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


    public abstract void connect(String address);
    public abstract void diconnect();
    public abstract BleContror.BleZt getState();
    public abstract void zhiling(String cmd,int room);

    public final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            MyLog.i("onConnectionStateChange", "onConnectionStateChange" +"=-------status = " +status +"-----newState = "+ newState + "====" + gatt);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Attempts to discover services after successful connection.
                MyLog.i("blecontror","start connect onConnectionStateChange");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//断开
                //蓝牙断开连接时候清除缓存
                //refreshDeviceCache();//通过反射的机制。
                MyLog.e("blecontror","erro disconnect onConnectionStateChange");
                connectionState = BleZt.STATE_DISCONNECTED;
                if(mBluetoothGatt !=null){
                    mCharacteristic = null;
                    //mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
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
                MyLog.i("blecontror","start connect onServicesDiscovered");


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
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            MyLog.i("------------->onReliableWriteCompleted received: " + status);
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
                MyLog.i("characteristic.getValue", "蓝牙发过来的：" + Arrays.toString(data));
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
}
