package com.fcao.quakemonitor;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 10/1/2017.
 */

public class HistoryActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private MyDatabaseHelper mDBHelper;
    private List<Record> recordList = new ArrayList<Record>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.history);

        Intent intent = getIntent();
        //recordList = (List<Record>) intent.getSerializableExtra("records");
        String dbName = intent.getStringExtra("dbname");
        mDBHelper = new MyDatabaseHelper(this, dbName, null, 1);

        initRecords();
        /*QuakeAdapter adapter = new QuakeAdapter(this,
                R.layout.history,
                recordList);
        setListAdapter(adapter);*/
        mListView = getListView();
        mListView.setOnItemClickListener(this);
        QuakeAdapter adapter = new QuakeAdapter(this, recordList);
        mListView.setAdapter(adapter);
    }

    private void initRecords() {
        SQLiteDatabase mDataBase = mDBHelper.getReadableDatabase();

        Cursor cursor = mDataBase.query("quake",null,null,null,null,null,null);//查询并获得游标
        if(cursor.moveToFirst()) {//判断游标是否为空
            while(cursor.moveToNext()) {
                //       c.move(i);//移动到指定记录,数据过多会有问题
                double x = cursor.getDouble(cursor.getColumnIndex("x"));
                double y = cursor.getDouble(cursor.getColumnIndex("y"));
                double z = cursor.getDouble(cursor.getColumnIndex("z"));
                double distance = cursor.getDouble(cursor.getColumnIndex("distance"));
                long time = cursor.getLong(cursor.getColumnIndex("time"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double speed = cursor.getDouble(cursor.getColumnIndex("speed"));
                Record record = new Record(x, y, z, distance, time, longitude, latitude, speed);
                recordList.add(record);
            }
        }

        cursor.close();
        mDataBase.close();
        mDBHelper.close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(-1 == id) { // 点击的是headerView或者<strong>footerView</strong>
            return;
        }
        Intent intent = new Intent("com.fcao.quakemonitor.SHOW_MAP");
        Record record = (Record) parent.getItemAtPosition(position);
        //intent.putExtra("longitude", record.getLongitude());
        //intent.putExtra("latitude", record.getLatitude());
        intent.putExtra("records", record);
        startActivity(intent);
    }
}
