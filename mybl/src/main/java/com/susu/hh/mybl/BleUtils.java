package com.susu.hh.mybl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by sucheng
 * on 2017/4/20.
 */
public class BleUtils {

    public static void enableNotifications(BluetoothGatt mBluetoothGatt) {
        // Get the service status UUID from the gatt and enable notifications
        List<BluetoothGattService> services = mBluetoothGatt.getServices();
        for (BluetoothGattService service : services) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                if (characteristic.getUuid().equals(BleUuidConstant.SPOTA_SERV_STATUS_UUID)) {
                    mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                            BleUuidConstant.SPOTA_DESCRIPTOR_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }
    }
    public static boolean setCharacteristicNotification(BluetoothAdapter mBluetoothAdapter, BluetoothGatt mBluetoothGatt,
                                                        List<BluetoothGattService> gattServices, boolean enabled, int witch) {
        if (mBluetoothAdapter == null || gattServices == null) {
            return false;
        }
        //enableNotifications(mBluetoothGatt);
        UUID uuid = null;
        switch (witch) {
            case 1:
                uuid = BleUuidConstant.SERVER_A_UUID_NOTIFY;//"00000002-0000-1000-8000-00805f9b34fb"
                break;
            case 3:
                uuid = BleUuidConstant.SERVER_B_UUID_NOTIFY;
                break;
            default:
                break;
        }
        BluetoothGattDescriptor descriptor = null;
        boolean NotifiResult = false;
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if (BleUuidConstant.SERVER_A_UUID_NOTIFY.toString().equals(gattCharacteristic.getUuid().toString()) ||
                        BleUuidConstant.SERVER_B_UUID_NOTIFY.toString().equals(gattCharacteristic.getUuid().toString())||
                        BleUuidConstant.SERVER_C_UUID_NOTIFY.toString().equals(gattCharacteristic.getUuid().toString())) {
                    mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, enabled);//打开响应
                        //设置描述
                        if (gattCharacteristic != null) {
                            //这里获取的描述是一样的  通用的   除非特殊
                            descriptor = gattCharacteristic.getDescriptor(BleUuidConstant.SERVER_A_UUID_DESCRIPTOR);
                        } else {
                            return false;
                        }
                        if (descriptor != null) {
                        /*descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00,
                                0x00});*/
                            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            NotifiResult = mBluetoothGatt.writeDescriptor(descriptor);
                        } else {
                            return false;
                        }

                }
            }
        }
        return NotifiResult;
    }

    private static int DATA_LENGTH = 20;

    public static synchronized void addCMD(String cmdCode, int write_UUID) {//举例设置闹钟。进行了分包
        if (cmdCode != null) {
            cmdCode = cmdCode + "\r\n";//add the end flag
            int strLength = cmdCode.length();
            for (int i = 0; i < strLength; ) {
                int end = i + DATA_LENGTH;
                if ((i + DATA_LENGTH) > strLength) {
                    end = strLength;
                }
                Log.i("cmdCode.substring", cmdCode.substring(i, end));
                byte[] writeByte = cmdCode.substring(i, end).getBytes();
//                Log.i("addCMDbyteup", Arrays.toString(writeByte));
                sendCMD(writeByte, write_UUID);
                i = i + DATA_LENGTH;
            }
        }
    }

    public static synchronized void addCMDbyte(byte[] cmdCodeBytes, int write_UUID) {//举例设置闹钟。进行了分包
        //purifit中 的分包的方法
        //cmdCodeBytes = new byte[]{0x41,0x54,0x2B,0x42,0x4F,0x4E,0x44};//可以
        byte[] byteadd = "\r\n".getBytes();
        byte[] byte_he = new byte[cmdCodeBytes.length + byteadd.length];
        System.arraycopy(cmdCodeBytes, 0, byte_he, 0, cmdCodeBytes.length);
        System.arraycopy(byteadd, 0, byte_he, cmdCodeBytes.length, byteadd.length);

        cmdCodeBytes = byte_he;
        int packageNum = (cmdCodeBytes.length + 19) / 20;
        for (int i = 0; i < packageNum; i++) {
            int start = i * 20;
            int end = Math.min(start + 20, cmdCodeBytes.length);
            byte[] buffer = Arrays.copyOfRange(cmdCodeBytes, start, end);
//            Log.i("addCMDbyte",Arrays.toString(buffer));
            sendCMD(buffer, write_UUID);
        }
    }

    public synchronized static void sendCMD(byte[] writeByte, int write_uuid) {
        String write = null;
        if (write_uuid == 1) {
            write = BleUuidConstant.SERVER_A_UUID_REQUEST.toString();
        } else if (write_uuid == 2) {
            write = BleUuidConstant.SERVER_B_UUID_REQUEST.toString();
        }
        BluetoothGatt mBluetoothGatt = BleContrParter.getBleContrpartInstance().getBlueToothGatt();
        BluetoothGattCharacteristic mCharacteristic1 = BleContrParter.getBleContrpartInstance().getCharacteristic();

        //获取所有的UUID：

//        File path = Environment.getExternalStorageDirectory();
//        String savepath = path.getAbsoluteFile() + "/mysave/uuid/";
//        File file3 = new File(savepath);
//        // File sd = new File(savepath);
//        if (!file3.exists()) {
//            boolean mkdirs = file3.mkdirs();
//            Log.i("fileText",mkdirs+" == "+ file3.exists()+"=== "+file3.getPath()+" === "+file3.getAbsolutePath());
//        }
//        try
//        {
//            // 创建文件对象
//            File fileText = new File(savepath+ "uuid.txt");//要么写file 要么写 savepath   不要写file3.getpath.
//            Log.i("fileText",fileText.exists()+"");
//            // 向文件写入对象写入信息
//            FileOutputStream fileWriter = new FileOutputStream(fileText,true);
//
//            List<BluetoothGattService> bluetoothGattsers = mBluetoothGatt.getServices();//我们没有用serviceuuid自己循环。
//            for (BluetoothGattService bhgs: bluetoothGattsers){
//                List<BluetoothGattCharacteristic> characteristics = bhgs.getCharacteristics();
//                for (BluetoothGattCharacteristic bluetoothgt: characteristics){
//                    MyLog.i("列出uuid = "+bluetoothgt.getUuid());
//                    fileWriter.write((bluetoothgt.getUuid()+"\r\n").toString().getBytes());
//                }
//            }
//            // 写文件
//
//            // 关闭
//            fileWriter.close();
//        }
//        catch (IOException e)
//        {
//            //
//            e.printStackTrace();
//        }

        if (mBluetoothGatt != null && mCharacteristic1 != null) {
            MyLog.i("有自己的mCharacteristic1啦");
            mCharacteristic1.setValue(writeByte);
            mBluetoothGatt.writeCharacteristic(mCharacteristic1);
        } else {
            List<BluetoothGattService> bluetoothGattServices = mBluetoothGatt.getServices();//我们没有用serviceuuid自己循环。
            MyLog.i("bluetoothGattServices", bluetoothGattServices.toString());
            if (bluetoothGattServices == null) {
                return;
            }
            BluetoothGattService bluetoothGattService;//获取服务的时候可以根据uuid进行筛选

            for (int i = 0; i < bluetoothGattServices.size(); i++) {
                bluetoothGattService = bluetoothGattServices.get(i);
                int size = bluetoothGattService.getCharacteristics().size();
                for (int j = 0; j < size; j++) {
                    String string = bluetoothGattService.getCharacteristics().get(j).getUuid().toString();
                    MyLog.i("bluetoothGattServicesand--", string);
                    if (string.equals(write)) {//if have REQUEST_UUID//根据区分uuid 来获得characteristic 在看了purifit的代码之后觉得不必要
                        MyLog.i("bluetoothGattServicesandcoming", string);
                        BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristics().get(j);
                        BleContrParter.getBleContrpartInstance().mCharacteristic = characteristic;
                        characteristic.setValue(writeByte);
                        mBluetoothGatt.writeCharacteristic(characteristic);
                        break;
                    }
                }
            }
        }

    }
}
