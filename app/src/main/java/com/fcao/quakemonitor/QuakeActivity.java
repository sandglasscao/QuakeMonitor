package com.fcao.quakemonitor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;


public class QuakeActivity extends Activity {
    //private static String[] pickTimes = {"10","20","30","40","50","60","70","80","90","100","200"};
    public static float[] threshold = {5, 10, 5, 10};
    //0: threshold level 1 of in station, default 5
    //1: threshold level 2 of in station, default 10
    //2: threshold level 1 of on track, default 5
    //3: threshold level 2 of on track, default 10
    private static String DB_NAME = "Quake.db";
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean isAllGranted = false;
    public List<Record> mRecords = new ArrayList<>();
    public List<Record> mOverTopRecords = new ArrayList<>();
    public int intervalTime; // default interval time for listener

    private MyDatabaseHelper mDBHelper;
    //private SQLiteDatabase mDataBase;
    private boolean isRunning, isOnTrack;
    private QuakeView mQuakeView;
    private QuakeListener mListener;
    private Switch mStart, mOntrack;

    @Override
    protected void onDestroy() {
        mDBHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1003: {
                threshold = (resultCode == RESULT_OK) ? data.getFloatArrayExtra("threshold") : threshold;
                resetThreshold();
                break;
            }
            default:
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar2.setNavigationIcon(R.mipmap.ic_launcher);//the navigation icon, now it's app log
        //toolbar2.setLogo(R.mipmap.ic_launcher);//app logo

        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.toolbar);//top-right menu
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search:
                        break;
                    case R.id.action_analysis: {
                        Intent intent = new Intent("com.fcao.quakemonitor.SHOW_QUAKES");
                        intent.putExtra("dbname", getMyDatabaseName());
                        startActivityForResult(intent, 1002); //1002 for show history
                        break;
                    }
                    case R.id.action_settings: {
                        /*SettingsFragment fragment = new SettingsFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.addToBackStack(null);
                        transaction.commit();*/
                        //Intent intent = new Intent(QuakeActivity.this, SettingsActivity.class);
                        Intent intent = new Intent("com.fcao.quakemonitor.SET_APL");
                        intent.putExtra("threshold", threshold);
                        startActivityForResult(intent, 1003); //1001 for settings
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

        isAllGranted = checkPermissionAllGranted(PERMISSIONS_STORAGE);
        if (!isAllGranted)
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        initDatabaseHelper();

        intervalTime = 20; //default interval time 20ms
        float thresholdlevel1, thresholdlevel2;
        thresholdlevel1 = isOnTrack ? threshold[2] : threshold[0];
        thresholdlevel2 = isOnTrack ? threshold[3] : threshold[1];
        float[] threshold = {thresholdlevel1, thresholdlevel2};
        mQuakeView = new QuakeView(this, intervalTime, threshold);
        mListener = new QuakeListener(this, mRecords, mOverTopRecords, intervalTime,
                threshold, mDBHelper);
        RelativeLayout rootView = findViewById(R.id.show_layout);
        rootView.addView(mQuakeView);

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
                isOnTrack = isChecked;
                resetThreshold();
            }
        });
        isOnTrack = mOntrack.isChecked();

    }

    private void resetThreshold() {
        float thresholdlevel1, thresholdlevel2;
        thresholdlevel1 = isOnTrack ? threshold[2] : threshold[0];
        thresholdlevel2 = isOnTrack ? threshold[3] : threshold[1];
        float[] threshold = {thresholdlevel1, thresholdlevel2};
        mQuakeView.setThreshold(threshold);
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
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
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
        builder.setMessage("需要访问内部存储器，请到 “应用信息 -> 权限” 中授予！");
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
}
