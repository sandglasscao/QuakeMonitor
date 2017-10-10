package com.fcao.quakemonitor;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 10/9/2017.
 */

public class HistoryFragment extends Fragment implements OnItemClickListener {
    private QuakeActivity mParent;
    private ListView mListView;
    private MyDatabaseHelper mDBHelper;
    private List<Record> recordList = new ArrayList<Record>();    ;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (-1 == id) { // 点击的是headerView或者<strong>footerView</strong>
            return;
        }
        Intent intent = new Intent("com.fcao.quakemonitor.SHOW_MAP");
        Record record = (Record) parent.getItemAtPosition(position);
        intent.putExtra("records", record);
        startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mParent = (QuakeActivity) getActivity();
        mDBHelper = mParent.mDBHelper;
        initRecords();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        View view = inflater.inflate(R.layout.history, container, false);

        mListView = view.findViewById(R.id.show_list);
        mListView.setOnItemClickListener(this);
        View headerView = inflater.inflate(R.layout.list_header, mListView, false);
        mListView.addHeaderView(headerView);
        QuakeAdapter adapter = new QuakeAdapter(getContext(), recordList);
        mListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void initRecords() {
        SQLiteDatabase mDataBase = mDBHelper.getReadableDatabase();

        Cursor cursor = mDataBase.query("quake", null, null, null, null, null, null);//查询并获得游标
        if (cursor.moveToFirst()) {//判断游标是否为空
            while (cursor.moveToNext()) {
                double x = cursor.getDouble(cursor.getColumnIndex("x"));
                double y = cursor.getDouble(cursor.getColumnIndex("y"));
                double z = cursor.getDouble(cursor.getColumnIndex("z"));
                double distance = cursor.getDouble(cursor.getColumnIndex("distance"));
                long time = cursor.getLong(cursor.getColumnIndex("time"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                float speed = cursor.getFloat(cursor.getColumnIndex("speed"));
                Record record = new Record(x, y, z, distance, time, longitude, latitude, speed);
                recordList.add(record);
            }
        }

        cursor.close();
        mDataBase.close();
    }

    @Override
    public void onStop() {
        super.onStop();
        mDBHelper.close();
    }
}
