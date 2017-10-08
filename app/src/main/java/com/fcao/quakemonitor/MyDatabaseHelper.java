package com.fcao.quakemonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Frank on 10/1/2017.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_QUAKE = "CREATE TABLE quake ("
            + "x REAL, "
            + "y REAL, "
            + "z REAL, "
            + "distance REAL, "
            + "time INTEGER, "
            + "longitude DOUBLE, "
            + "latitude DOUBLE, "
            + "speed REAL)";

    private static final String DROP_QUAKE = "DROP TABLE quake";

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

    public void truncate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DROP_QUAKE);
        sqLiteDatabase.execSQL(CREATE_QUAKE);
    }
}
