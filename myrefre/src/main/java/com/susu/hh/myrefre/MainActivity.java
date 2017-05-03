package com.susu.hh.myrefre;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class MainActivity extends Activity {
    /*
    * // the following are default settings
    mPtrFrame.setResistance(1.7f);
    mPtrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
    mPtrFrame.setDurationToClose(200);
    mPtrFrame.setDurationToCloseHeader(1000);
    // default is false
    mPtrFrame.setPullToRefresh(false);
    // default is true
    mPtrFrame.setKeepHeaderWhenRefresh(true);
    * */
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        PtrFrameLayout mPtrFrameLayout = (PtrFrameLayout) findViewById(R.id.store_house_ptr_frame);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll_parent);
        tv = (TextView) findViewById(R.id.tv);
        mPtrFrameLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                tv.setText("loading...");
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        frame.refreshComplete();
                        tv.setText("刷新成功！");
                    }
                }, 4000);
            }
        });
        //默认下拉头 可自定义。
        PtrClassicDefaultHeader header = new PtrClassicDefaultHeader(this);
        mPtrFrameLayout.addPtrUIHandler(header);
        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.setPinContent(true);
    }
}
