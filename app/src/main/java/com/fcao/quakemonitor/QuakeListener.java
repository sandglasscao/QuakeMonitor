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

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;

import java.util.List;

/**
 * Created by Frank on 7/12/2017.
 */

public class QuakeListener implements SensorEventListener {
    //private static final int SHAKE_THRESHOLD = 5;
    //private static final int SHAKE_THRESHOLD_MIN = 2;
    private static final int maxLength = 2; //for overtop records to save into SQLite
    private static int mInterval_time;
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

    private Context mContext;
    private List<Record> mRecords, mOverTopRecords;
    private SQLiteDatabase mDatabase;
    private MyDatabaseHelper mDatabaseHelper;

    private LocationClient mLocationClient;

    private double[] lastP = {0, 0, 0, 0};
    private long lastTime;
    //private long lastTimeP0, lastTimeP1, lastTimeP2, lastTime;
    //private double dist = 0;
    //DecimalFormat df = new DecimalFormat("#.00");

    public QuakeListener(Context context, List<Record> records, List<Record> overTopRecords,
                         int intervalTime, float[] threshold, MyDatabaseHelper mMysql,
                         LocationClient locationClient) {
        mContext = context;
        mRecords = records;
        mOverTopRecords = overTopRecords;
        mDatabaseHelper = mMysql;
        mLocationClient = locationClient;
        //QuakeListener.setInterval_time(intervalTime);
        mInterval_time = intervalTime;
        recordsSize_max = 2 * 1000 / mInterval_time;
        setThreshold(threshold);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void setThreshold(float[] threshold) {
        mthreshold = threshold;
        mRecords.clear();
    }
    /*public static void setInterval_time(int interval_time) {
        QuakeListener.interval_time = interval_time;
        QuakeListener.recordsSize_max = 10 * 1000 / QuakeListener.interval_time;
        //10s has the 1000/intervalTime records
        mRecords.clear();
    }*/

    // start monitor the sensor accelerometer
    public void start() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
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
        mLocationClient.start();
    }

    // stop monitor the sensor accelerometer
    public void stop() {
        mLocationClient.stop();
        mSensorManager.unregisterListener(this);
    }

    private void calculateAmplitude() {
        long curTime = System.currentTimeMillis();
        double gravity = SensorManager.STANDARD_GRAVITY;

        double rollA = gravity * Math.abs(Math.sin(mOrientationAngles[2]) * Math.cos(mOrientationAngles[1]));
        double pitchA = gravity * Math.abs(Math.sin(mOrientationAngles[1]));
        double amplitudeA = Math.sqrt(gravity * gravity - rollA * rollA - pitchA * pitchA);
        double x = Math.abs(mAccelerometerReading[0]) - rollA;
        double y = Math.abs(mAccelerometerReading[1]) - pitchA;
        double z = Math.abs(mAccelerometerReading[2]) - amplitudeA;
        //double a = Math.sqrt(x * x + y * y + z * z);

        if ((curTime - lastTime) > mInterval_time) {
            //long diffTime = curTime - lastTime;
            lastTime = curTime;

            double ax = lastP[0] - x;
            double ay = lastP[1] - y;
            double az = lastP[2] - z;
            //double a = Math.sqrt(ax * ax + ay * ay + az * az);
            double dist = Math.sqrt(ax * ax + az * az);
            Record record = new Record(Math.abs(ax), Math.abs(ay), Math.abs(az), dist, curTime, 0, 0, 0);
            updateRecords(record);

            if (dist >= mthreshold[1]) {
                saveRecords(record);
            }

            lastP[0] = x;
            lastP[1] = y;
            lastP[2] = z;
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
        if (recordsSize_max <= mRecords.size()) {
            mRecords.remove(0);
        }
        mRecords.add(record);
    }

    //save records
    private void saveRecords(Record record) {
        if (maxLength <= mOverTopRecords.size())
            flush();
        saveLocation(record);
        mOverTopRecords.add(record);
    }

    // get current longitude and latitude
    private void saveLocation(Record record) {
        try {
            //mLocationClient.requestLocation();
            BDLocation location = mLocationClient.getLastKnownLocation();
            record.setLongitude(location.getLongitude());
            record.setLatitude(location.getLatitude());
            record.setSpeed(location.getSpeed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void flush() {
        boolean isLocked = false;
        mDatabase = mDatabaseHelper.getReadableDatabase();
        mDatabase.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Record record : mOverTopRecords) {
                values.put("x", record.getX());
                values.put("y", record.getY());
                values.put("z", record.getZ());
                values.put("distance", record.getDistance());
                values.put("time", record.getTime());
                values.put("longitude", record.getLongitude());
                values.put("latitude", record.getLatitude());
                values.put("speed", record.getSpeed());
                mDatabase.insert("quake", null, values);
                values.clear();
            }
            // 设置事务标志为成功，当结束事务时就会提交事务
            mDatabase.setTransactionSuccessful();
            mOverTopRecords.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDatabase.endTransaction();// 结束事务
            mDatabase.close();
            //mDatabaseHelper.close();
        }
    }
}
