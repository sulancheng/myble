package com.example.myrefreandloadmore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class JindianActivity extends AppCompatActivity {
    private PtrClassicFrameLayout mPtrFrameLayout;
    private TextView tv_remind;
    private TextView textjiao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jindian);
        init();
    }

    private void init() {
        mPtrFrameLayout = (PtrClassicFrameLayout) findViewById(R.id.rotate_header_grid_view_frame);
        mPtrFrameLayout.setKeepHeaderWhenRefresh(true);
        //以下为自定义header需要
        //StoreHouseHeader header = new StoreHouseHeader(this);
        PtrClassicDefaultHeader header = new PtrClassicDefaultHeader(this);
        //PtrClassicDefaultFooter footer = new PtrClassicDefaultFooter(this);
        View footer = View.inflate(this, R.layout.footer, null);
        header.setPadding(0, LocalDisplay.dp2px(20), 0, LocalDisplay.dp2px(20));
        mPtrFrameLayout.setKeepHeaderWhenRefresh(true);
        mPtrFrameLayout.setDurationToCloseHeader(1500);
        mPtrFrameLayout.setFooterView(footer);
        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.addPtrUIHandler(header);
        textjiao = (TextView) footer.findViewById(R.id.jiao);

        tv_remind = (TextView) findViewById(R.id.tv_remind);
        //设置自定义头部样式
        //UltraCustomerHeader.setUltraCustomerHeader(mPtrFrame, context);
        //设置下拉刷新上拉加载
        mPtrFrameLayout.disableWhenHorizontalMove(true);//解决横向滑动冲突
        mPtrFrameLayout.setPtrHandler(new PtrDefaultHandler2() {

            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {
                textjiao.setText("正在加载...");
                mPtrFrameLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrFrameLayout.refreshComplete();
                        textjiao.setText("加载完成");
                    }
                },3000);
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
                textjiao.setText("上拉加载");
                return super.checkCanDoLoadMore(frame, content, footer);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return super.checkCanDoRefresh(frame, content, header);
            }
        });
        mPtrFrameLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPtrFrameLayout.autoLoadMore();
            }
        },1500);
    }

}
