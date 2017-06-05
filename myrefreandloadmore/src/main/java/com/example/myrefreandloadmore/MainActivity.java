package com.example.myrefreandloadmore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
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


      ptr_header，设置头部 id 。
ptr_content，设置内容 id 。
ptr_resistance，阻尼系数，默认:1.7f，越大，感觉下拉时越吃力。
ptr_ratio_of_header_height_to_refresh，触发刷新时移动的位置比例，默认，1.2f，移动达到头部高度 1.2 倍时可触发刷新操作。
ptr_duration_to_close，回弹延时，默认200ms，回弹到刷新高度所用时间。
ptr_duration_to_close_header，头部回弹时间，默认1000ms。
ptr_pull_to_fresh，刷新是否保持头部，默认值true。
ptr_keep_header_when_refresh，下拉刷新 / 释放刷新，默认为释放刷新。
      * */
    private PtrFrameLayout mPtrFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPtrFrameLayout = (PtrFrameLayout) findViewById(R.id.mpfl);
        init();
    }

    private void init() {
        //默认下拉头 可自定义。
        PtrClassicDefaultHeader header = new PtrClassicDefaultHeader(this);
        mPtrFrameLayout.addPtrUIHandler(header);
        mPtrFrameLayout.setHeaderView(header);
        //mPtrFrameLayout.setFooterView();
        mPtrFrameLayout.setPinContent(false);//是否让儿子跟着动
       // mPtrFrameLayout.setDurationToClose(50);
        // default is true
        mPtrFrameLayout.setKeepHeaderWhenRefresh(true);//false一拉就会收上去
//        mPtrFrameLayout.setPtrHandler(new PtrDefaultHandler() {
//            @Override
//            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
//                return super.checkCanDoRefresh(frame, content, header);
//            }
//
//            @Override
//            public void onRefreshBegin(PtrFrameLayout frame) {
//                int arr[] = {6,4,3,7,9,8,4,3};
//                //maop(arr);
//                xuanz(arr);
//                Log.i("onRefreshBegin", Arrays.toString(arr));
//                frame.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mPtrFrameLayout.refreshComplete();
//                    }
//                }, 3000);
//            }
//        });
        mPtrFrameLayout.setPtrHandler(new PtrDefaultHandler2() {
            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrFrameLayout.refreshComplete();
                    }
                },4000);

            }
            @Override
            public boolean checkCanDoLoadMore(PtrFrameLayout frame, View content, View footer) {
                return true;
            }
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return super.checkCanDoRefresh(frame,content,header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrFrameLayout.refreshComplete();
                    }
                },3000);
            }

        });

    }

    public void tojd(View view) {
        //mPtrFrameLayout.autoRefresh();//自动刷新
        startActivity(new Intent(MainActivity.this,JindianActivity.class));
    }

    public void maop(int []arr){
        for(int i =0;i<arr.length;i++){
            for (int j = 0;j<arr.length-1;j++){
                if(arr[j]>arr[j+1]){
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
    }
    public void xuanz(int []arr){
        for(int i =0;i<arr.length;i++){
            for (int j = i+1;j<arr.length;j++){
                if(arr[i]>arr[j]){
                    int temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
        }
    }
}
