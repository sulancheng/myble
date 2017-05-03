package com.example.myrefreandloadmore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class JindianActivity extends AppCompatActivity {
    private PtrClassicFrameLayout mPtrFrameLayout;
    private TextView tv_remind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jindian);
        init();
    }

    private void init() {
        mPtrFrameLayout = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_grid_view_frame);
        tv_remind = (TextView) findViewById(R.id.tv_remind);
        //设置自定义头部样式
        //UltraCustomerHeader.setUltraCustomerHeader(mPtrFrame, context);
        //设置下拉刷新上拉加载
        mPtrFrameLayout.disableWhenHorizontalMove(true);//解决横向滑动冲突
        mPtrFrameLayout.setPtrHandler(new PtrDefaultHandler2() {

            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {

            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {


                mPtrFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrFrameLayout.refreshComplete();
                    }
                },3000);
            }
            @Override
            public boolean checkCanDoLoadMore(PtrFrameLayout frame, View content, View footer) {
                return false;
            }

        });
    }

}
