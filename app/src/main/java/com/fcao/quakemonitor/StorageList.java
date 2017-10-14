package com.fcao.quakemonitor;

import android.app.Activity;
import android.os.storage.StorageManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Frank on 10/13/2017.
 */

public class StorageList {
    private Activity mActivity;
    private StorageManager mStorageManager;
    private Method mMethodGetPaths;

    public StorageList(Activity activity) {
        mActivity = activity;
        if (mActivity != null) {
            mStorageManager = (StorageManager) mActivity
                    .getSystemService(Activity.STORAGE_SERVICE);
            try {
                mMethodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getVolumePaths() {
        String[] paths = null;
        try {
            paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return paths;
    }
}
