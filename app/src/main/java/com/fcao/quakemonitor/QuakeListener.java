package com.fcao.quakemonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.utils.DistanceUtil;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Frank on 7/12/2017.
 */

public class QuakeListener implements SensorEventListener {
    private static final int maxLength = 2; //for overtop records to save into SQLite
    private static final DecimalFormat df2 = new DecimalFormat("#.00");
    private float[] mthreshold;
    private int recordsSize_max;
    private SensorManager mSensorManager;
    private Sensor aSensor; //for accelerometer
    private Sensor mSensor; //for magnetic field
    private static boolean aHasInit = false;
    private static boolean mHasInit = false;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    private QuakeActivity mParent;
    private double[] lastP = {0, 0, 0, 0};
    private long lastTime;
    private float mLastSpeed;

    public QuakeListener(QuakeActivity activity, float[] threshold) {
        mParent = activity;
        recordsSize_max = 2 * 1000 / mParent.intervalTime;
        setThreshold(threshold);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: {
                System.arraycopy(sensorEvent.values, 0, mAccelerometerReading,
                        0, mAccelerometerReading.length);
                aHasInit = true;
                break;
            }
            case Sensor.TYPE_MAGNETIC_FIELD: {
                System.arraycopy(sensorEvent.values, 0, mMagnetometerReading,
                        0, mMagnetometerReading.length);
                mHasInit = true;
                break;
            }
        }
        updateOrientationAngles();
        if (aHasInit & mHasInit)
            calculateAmplitude();
    }

    public void setThreshold(float[] threshold) {
        mthreshold = threshold;
        mParent.mRecords.clear();
    }

    // start monitor the sensor accelerometer
    public void start() {
        mSensorManager = (SensorManager) mParent.getSystemService(Context.SENSOR_SERVICE);
        if (null != mSensorManager) {
            aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        if (null != mSensor) {
            mSensorManager.registerListener(this, aSensor,
                    SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);
        }
        mParent.mLocClient.start();
    }

    // stop monitor the sensor accelerometer
    public void stop() {
        mParent.mLocClient.stop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void finalize() throws Throwable {
        flush();
        super.finalize();
    }

    private void calculateAmplitude() {
        long curTime = System.currentTimeMillis();
        double gravity = SensorManager.STANDARD_GRAVITY;

        double rollA = gravity * Math.abs(Math.sin(mOrientationAngles[2]) * Math.cos(mOrientationAngles[1]));
        double pitchA = gravity * Math.abs(Math.sin(mOrientationAngles[1]));
        double amplitudeA = Math.sqrt(gravity * gravity - rollA * rollA - pitchA * pitchA);
        double x = mAccelerometerReading[0] - rollA;
        double y = mAccelerometerReading[1] - pitchA;
        double z = mAccelerometerReading[2] - amplitudeA;
        //double a = Math.sqrt(x * x + y * y + z * z);
        x = x * 2;
        z = z * 2;

        if ((curTime - lastTime) > mParent.intervalTime) {
            //long diffTime = curTime - lastTime;
            lastTime = curTime;

            double ax = lastP[0] - x;
            double ay = lastP[1] - y;
            double az = lastP[2] - z;
            //double a = Math.sqrt(ax * ax + ay * ay + az * az);
            double dist = Math.sqrt(ax * ax + az * az);
            Record record = new Record(Math.abs(ax), Math.abs(ay), Math.abs(az), dist, curTime, 0, 0, 0);
            saveLocation(record);
            updateRecords(record);

            if (dist >= mthreshold[1]) {
                saveRecords(record);
            }

            lastP[0] = x;
            lastP[1] = y;
            lastP[2] = z;
        }
    }

    private void flush() {
        boolean isLocked = false;
        SQLiteDatabase mDataBase = mParent.mDBHelper.getReadableDatabase();
        mDataBase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Record record : mParent.mOverTopRecords) {
                values.put("x", record.getX());
                values.put("y", record.getY());
                values.put("z", record.getZ());
                values.put("distance", record.getDistance());
                values.put("time", record.getTime());
                values.put("longitude", record.getLongitude());
                values.put("latitude", record.getLatitude());
                values.put("speed", record.getSpeed());
                mDataBase.insert("quake", null, values);
                values.clear();
            }
            // 设置事务标志为成功，当结束事务时就会提交事务
            mDataBase.setTransactionSuccessful();
            mParent.mOverTopRecords.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDataBase.endTransaction();// 结束事务
            mDataBase.close();
            //mDatabaseHelper.close();
        }
    }

    //save records
    private void saveRecords(Record record) {
        if (maxLength <= mParent.mOverTopRecords.size())
            flush();
        saveLocation(record);
        mParent.mOverTopRecords.add(record);
    }

    // get current longitude and latitude
    private void saveLocation(Record record) {
        try {
            //mLocationClient.requestLocation();
            BDLocation location = mParent.mLocClient.getLastKnownLocation();
            record.setLongitude(location.getLongitude());
            record.setLatitude(location.getLatitude());
            //record.setSpeed(location.getSpeed());
            float speed = Float.valueOf(df2.format(location.getSpeed()));
            speed = (0 == speed && Math.abs(speed - mLastSpeed) > 10) ? mLastSpeed : speed;
            // if current speed is zero and the difference between current speed and the last speed
            // retried from BaiduLocation is large, then it means that the current speed is wrong
            // and the vehicle should be inside a building or channel
            //DistanceUtil.getDistance();
            record.setSpeed(speed);

            mParent.mAddrEv.setText(location.getAddrStr());
            mParent.mSpeedEv.setText(String.valueOf(speed));
            mParent.mCurrLong = location.getLongitude();
            mParent.mCurrLatd = location.getLatitude();
            mParent.mCurrSpeed = speed;
            mLastSpeed = speed;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        // "mRotationMatrix" now has up-to-date information.

        mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        // "mOrientationAngles" now has up-to-date information.
    }

    //update sensor records
    private void updateRecords(Record record) {
        if (recordsSize_max <= mParent.mRecords.size()) {
            mParent.mRecords.remove(0);
        }
        mParent.mRecords.add(record);
    }
}
