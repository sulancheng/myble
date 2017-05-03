package com.susu.hh.mybl;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by su
 * on 2017/4/19.
 */
public abstract class BleContrParter {
    private static BleContrParter mblecontrpart;

    //链接流程走完了  可以用这个了
    public static BleContrParter getBleContrpartInstance(){
        if(null == mblecontrpart){
            return null;
        }
        return mblecontrpart;
    }

    //此方法只在绑定服务的时候使用。
    public static BleContrParter getBleContrpartInstance(String blename, BluetoothAdapter mBluetoothAdapter, Context mcontext){
        if(!"01".equals(blename)){
            Log.i("getblecontr","得到getblecontr");
            mblecontrpart = BleContror.getInstance(mcontext, mBluetoothAdapter);
        }else{
            Toast.makeText(mcontext, "暂时没有设备", Toast.LENGTH_LONG).show();
        }
        return mblecontrpart;
    }
    public abstract void bingMybind();
    public abstract void setOnBLECallBackListener(BleContror.OnBLECallBackListener listener);
    public abstract void connect(String address);
    public abstract void diconnect();
    public abstract BleContror.BleZt getState();
    public abstract void zhiling(String cmd,int room);
}
