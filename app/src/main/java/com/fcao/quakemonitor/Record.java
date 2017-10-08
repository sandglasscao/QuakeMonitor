package com.fcao.quakemonitor;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Frank on 7/12/2017.
 */

public class Record implements Serializable{
    private double x, y, z, distance;
    private long mTime;
    private double mLongitude, mLatitude;
    float mSpeed;

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public Record(double x, double y, double z, double distance, long time, double longitude, double latitude, float speed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.distance = distance;
        mTime = time;
        mLongitude = longitude;
        mLatitude = latitude;
        mSpeed = speed;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }
}
