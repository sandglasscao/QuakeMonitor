package com.fcao.quakemonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Frank on 10/1/2017.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_QUAKE = "create table quake ("
            + "x real, "
            + "y real, "
            + "z real, "
            + "distance real, "
            + "time integer, "
            + "longitude real, "
            + "latitude real)";

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_QUAKE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
