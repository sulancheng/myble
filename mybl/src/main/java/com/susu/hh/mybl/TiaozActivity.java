package com.susu.hh.mybl;

import android.app.Activity;
import android.os.Bundle;

public class TiaozActivity extends Activity {
    private String TAG = "TiaozActivity";
    private BleContrParter instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiaoz);
        instance = BleContrParter.getBleContrpartInstance();
//        instance.addOnBLECallBackListener(new BleContror.OnBLECallBackListener() {
//            @Override
//            public void bindResp(boolean isBind, String data) {
//                Log.i(TAG,isBind+"===="+data);
//            }
//        });
        instance.zhiling("AT+BOND",1);
    }
}
