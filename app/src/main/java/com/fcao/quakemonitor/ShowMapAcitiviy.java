package com.fcao.quakemonitor;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

/**
 * Created by Frank on 10/7/2017.
 */

public class ShowMapAcitiviy extends Activity {
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private boolean isFirstLoc = true; // 是否首次定位
    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.map);

        mTextView = findViewById(R.id.loc_tv);
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();// 地图初始化
        mBaiduMap.setMyLocationEnabled(true);// 开启定位图层

    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}
