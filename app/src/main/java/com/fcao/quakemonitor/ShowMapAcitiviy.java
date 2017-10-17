package com.fcao.quakemonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.text.DecimalFormat;

/**
 * Created by Frank on 10/7/2017.
 */

public class ShowMapAcitiviy extends Activity {
    private static final double[] DEFAULT_LOCATION = {116.327779, 39.900769}; //北京
    private static final float DEFAULT_SCALE = 13f; //default scale of map
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private boolean isFirstLoc = true; // 是否首次定位
    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.map);

        Intent intent = getIntent();
        double longitude = intent.getDoubleExtra("longitude",DEFAULT_LOCATION[0]);
        double latitude = intent.getDoubleExtra("latitude", DEFAULT_LOCATION[1]);
        float speed = intent.getFloatExtra("speed", 0);
        //Record record = (Record) intent.getSerializableExtra("records");

        mTextView = findViewById(R.id.loc_tv);
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();// 地图初始化
        mBaiduMap.setMyLocationEnabled(true);// 开启定位图层
        setLocation(longitude, latitude);
        String locInfo = "经度：" + longitude  + "   纬度：" + latitude
                + "   速度：" + speed + " km/h";
        mTextView.setText(locInfo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
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

    private void setLocation(double longitude, double latitude) {
        MyLocationData locData = new MyLocationData.Builder()
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(latitude)
                .longitude(longitude).build();
        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(latitude,longitude);
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(ll).zoom(DEFAULT_SCALE);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory
                    .newMapStatus(builder.build()));
        }
    }
}
