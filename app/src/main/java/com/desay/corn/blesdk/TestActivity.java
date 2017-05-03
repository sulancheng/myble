package com.desay.corn.blesdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.desay.corn.blelab.DesayLog;

/**
 * Created by corn on 2016/7/21.
 */
public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        this.startService(new Intent(this, BluetoothLoaderService.class));
        DesayLog.d("onCreate startService");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
