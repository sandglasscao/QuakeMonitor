package com.fcao.quakemonitor;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 10/1/2017.
 */

public class HistoryActivity extends ListActivity {
    private SQLiteOpenHelper mDBHelper;
    private List<Record> recordList = new ArrayList<Record>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Intent intent = getIntent();
        //recordList = (List<Record>) intent.getSerializableExtra("records");
        String dbName = intent.getStringExtra("dbname");
        mDBHelper = new MyDatabaseHelper(this, dbName, null, 1);

        initRecords();
        QuakeAdapter adapter = new QuakeAdapter(this,
                R.layout.list_view,
                recordList);
        setListAdapter(adapter);
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
                Record record = new Record(x, y, z, distance, time, longitude, latitude);
                recordList.add(record);
            }
        }

        cursor.close();
        mDataBase.close();
        mDBHelper.close();
    }
}

/*Cursor query (
        boolean distinct,
        String table,
        String[] columns,
        String selection,
        String[] selectionArgs,
        String groupBy,
        String having,
        String orderBy,
        String limit,
        CancellationSignal cancellationSignal
        1 distinct，boolean: true if you want each row to be unique, false otherwise.（设置为true,每一行的数据必须唯一。反之亦然。）
2. table，String: The table name to compile the query against.（query函数要操作的表名。）
3. columns，String[]: A list of which columns to return. Passing null will return all columns, which is discouraged to prevent reading data from storage that isn’t going to be used.（要返回的列的名字的数组。如果设置为null，返回所有列，如果不需要使用所有列，不建议这么做。）
4. selection，String: A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given table.（一个决定返回哪一行的过滤器，相当于SQL语句中的 WHERE 关键字。传递null则会返回给定表的所有行。）
5. selectionArgs，String: You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.（用于替换上一个参数中的 ？ ,顺序对应selection中？的顺序。格式限制为String格式。）
6. groupBy，String: A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY itself). Passing null will cause the rows to not be grouped.（用于设定返回行的分组方式，相当于SQL语句中的GROUP BY 关键字。传递null表示返回的行不会被分组。）
7. having，String: A filter declare which row groups to include in the cursor, if row grouping is being used, formatted as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all row groups to be included, and is required when row grouping is not being used.（决定哪一行被放到Cursor中的过滤器。如果使用了行分组，相当于SQL语句中的HAVING关键字。传递null会导致所有的行都包含在内，前提是groupBy属性也设置为null。）
8. orderBy，String: How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.（行的排列方式，相当于SQL语句中的“ORDER BY”关键字，传递null表示使用默认的排序方式，可能是无序排列。）
9. limit，String: Limits the number of rows returned by the query, formatted as LIMIT clause. Passing null denotes no LIMIT clause.（设置query语句返回行的数量，相当于SQL语句中的“LIMIT”关键字，传递null表示没有设置limit语句。注意格式为String,传递的时候需要传递数字字符串，例如“12”）
10. cancellationSignal，CancellationSignal: A signal to cancel the operation in progress, or null if none. If the operation is canceled, then OperationCanceledException will be thrown when the query is executed.（取消程序操作的信号，如果没有则设置为null。如果操作取消了，query语句运行时会抛出OperationCanceledException异常。）

含有7个参数的query函数不包含1，9，10，也就是distinct，limit，cancellationSignal。
含有8个参数的query函数不包含1，10，也就是distinct，cancellationSignal。
含有9个参数的query函数不包含10，也就是cancellationSignal。
        */
