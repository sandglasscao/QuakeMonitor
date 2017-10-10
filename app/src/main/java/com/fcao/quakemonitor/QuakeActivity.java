package com.fcao.quakemonitor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QuakeActivity extends Activity implements View.OnClickListener {
    private static final Map GRAPH_POSITION = new HashMap();
    private static String DB_NAME = "Quake.db";
    public static final int REQUEST_PERMISSION_CODE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public float[] mThreshold = {5, 10, 5, 10};
    //0: threshold level 1 of in station, default 5
    //1: threshold level 2 of in station, default 10
    //2: threshold level 1 of on track, default 5
    //3: threshold level 2 of on track, default 10

    private boolean isAllGranted = false;
    public List<Record> mRecords = new ArrayList<>();
    public List<Record> mOverTopRecords = new ArrayList<>();
    public int intervalTime; // default interval time for listener
    public LocationClient mLocClient;

    public MyDatabaseHelper mDBHelper;
    private boolean isRunning, isOnPlatform;
    private QuakeSurfaceView mQuakeView_tot, mQuakeView_x, mQuakeView_z;
    private QuakeListener mListener;
    private Switch mStart, mOntrack;
    private RelativeLayout mRelativeLayout_tot, mRelativeLayout_x, mRelativeLayout_z;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1003: {
                mThreshold = (resultCode == RESULT_OK) ? data.getFloatArrayExtra("threshold") : mThreshold;
                resetThreshold();
                break;
            }
            default:
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 如果是橫屏時候
        try {
            // Checks the orientation of the screen
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.d("HistoryFragment", "screen landscape");
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.d("HistoryFragment", "Back to portrait");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar2.setNavigationIcon(R.mipmap.ic_launcher);//the navigation icon, now it's app log
        //toolbar2.setLogo(R.mipmap.ic_launcher);//app logo

        toolbar.setTitle(R.string.app_name);
        toolbar.setBackgroundColor(0xFF1E90FF);
        toolbar.inflateMenu(R.menu.toolbar);//top-right menu
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_analysis: {
                        HistoryFragment fragment = new HistoryFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.main_fragment, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    }
                    case R.id.action_settings: {
                        SettingsFragment fragment = new SettingsFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.main_fragment, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    }
                    case R.id.action_about:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        initialize();
    }

    @Override
    protected void onDestroy() {
        mDBHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isRunning)
            mListener.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mListener.stop(); //unregister the listener
    }

    private void initialize() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        isAllGranted = checkPermissionAllGranted(PERMISSIONS);
        if (!isAllGranted)
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE);
        initDatabaseHelper();

        // 定位初始化
        initLocationClient();
        intervalTime = 20; //default interval time 20ms
        float thresholdlevel1, thresholdlevel2;
        thresholdlevel1 = isOnPlatform ? mThreshold[0] : mThreshold[2];
        thresholdlevel2 = isOnPlatform ? mThreshold[1] : mThreshold[3];
        float[] threshold = {thresholdlevel1, thresholdlevel2};

        mListener = new QuakeListener(this, mRecords, mOverTopRecords, intervalTime,
                threshold, mDBHelper, mLocClient);
        initSurfaceView();

        initGRAPH_POSITION();
        mStart = findViewById(R.id.monitor_switch);
        mStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                    mListener.start();
                else
                    mListener.stop();
                isRunning = isChecked;
            }
        });
        isRunning = mStart.isChecked();

        mOntrack = findViewById(R.id.platform_switch);
        mOntrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isRunning) {
                    mListener.stop();
                    mListener.start();
                }
                isOnPlatform = isChecked;
                resetThreshold();
            }
        });
        isOnPlatform = mOntrack.isChecked();
    }

    private void resetThreshold() {
        float thresholdlevel1, thresholdlevel2;
        thresholdlevel1 = isOnPlatform ? mThreshold[0] : mThreshold[2];
        thresholdlevel2 = isOnPlatform ? mThreshold[1] : mThreshold[3];
        float[] threshold = {thresholdlevel1, thresholdlevel2};
        mQuakeView_tot.setThreshold(threshold);
        mQuakeView_x.setThreshold(threshold);
        mQuakeView_z.setThreshold(threshold);
        mListener.setThreshold(threshold);
    }

    /**
     * 请求权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            isAllGranted = true;
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码
                initDatabaseHelper();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                openAppDetails();
            }
        }
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 打开 APP 的详情设置
     */
    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setMessage("需要访问内部存储器，请到 “应用信息 -> 权限” 中授予！");
        builder.setMessage("请到 “应用信息 -> 权限” 中授予！");
        builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void initSurfaceView() {
        mQuakeView_tot = new QuakeSurfaceView(this, intervalTime, mThreshold, mRecords, 0);
        mQuakeView_x = new QuakeSurfaceView(this, intervalTime, mThreshold, mRecords, 1);
        mQuakeView_z = new QuakeSurfaceView(this, intervalTime, mThreshold, mRecords, 2);
        mRelativeLayout_tot = findViewById(R.id.show_graph_tot);
        mRelativeLayout_x = findViewById(R.id.show_graph_x);
        mRelativeLayout_z = findViewById(R.id.show_graph_z);
        mRelativeLayout_tot.addView(mQuakeView_tot);
        mRelativeLayout_x.addView(mQuakeView_x);
        mRelativeLayout_z.addView(mQuakeView_z);
        mRelativeLayout_tot.setOnClickListener(this);
        mRelativeLayout_x.setOnClickListener(this);
        mRelativeLayout_z.setOnClickListener(this);
    }

    private void initDatabaseHelper() {
        try {
            mDBHelper = new MyDatabaseHelper(this, getMyDatabaseName(), null, 1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDBHelper.close();
        }
    }

    private String getMyDatabaseName() {
        boolean isSdcardEnable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {//SDCard是否插入
            isSdcardEnable = true;
        }
        String dbPath = null;
        if (isSdcardEnable && isAllGranted) {
            dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName();
        } else {//未插入SDCard，建在内存中
            dbPath = getFilesDir().getPath();
        }
        dbPath = dbPath + "/database/";
        File dbp = new File(dbPath);
        if (!dbp.exists())
            dbp.mkdirs();

        String databasename = dbPath + DB_NAME;
        return databasename;
    }

    // 定位初始化
    private void initLocationClient() {
        mLocClient = new LocationClient(this);
        initLocationOption();
    }

    private void initLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setServiceName("com.baidu.location.service_v4.5");

        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
/*
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        option.setWifiCacheTimeOut(5*60*1000);
        //可选，7.2版本新增能力，如果您设置了这个接口，首次启动定位时，会先判断当前WiFi是否超出有效期，超出有效期的话，会先重新扫描WiFi，然后再定位
*/
        mLocClient.setLocOption(option);
    }

    @Override
    public void onClick(View v) {
        int pos = (int) GRAPH_POSITION.get(v.getId());
        FullScreenFragment fragment = new FullScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", pos);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.main_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initGRAPH_POSITION() {
        GRAPH_POSITION.put(R.id.show_graph_tot, 0);
        GRAPH_POSITION.put(R.id.show_graph_x, 1);
        GRAPH_POSITION.put(R.id.show_graph_z, 2);
    }
}
