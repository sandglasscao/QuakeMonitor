package com.fcao.quakemonitor;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Frank on 10/1/2017.
 */

public class SettingsActivity extends Activity implements View.OnClickListener{
    private float[] threshold = {0,0,0,0};
    private EditText platform_level1, platform_level2, track_level1,track_level2;
    private Button ok_btn, cancel_btn, del_history;
    private MyDatabaseHelper mDBHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String dbName = intent.getStringExtra("dbname");
        mDBHelper = new MyDatabaseHelper(this, dbName, null, 1);
        threshold = intent.getFloatArrayExtra("threshold");
        setContentView(R.layout.settings);

        platform_level1 = findViewById(R.id.platform_level1);
        platform_level2 = findViewById(R.id.platform_level2);
        track_level1 = findViewById(R.id.track_level1);
        track_level2 = findViewById(R.id.track_level2);
        ok_btn = findViewById(R.id.ok_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        del_history = findViewById(R.id.del_history);
        ok_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
        del_history.setOnClickListener(this);
        platform_level1.setText(Float.toString(threshold[0]));
        platform_level2.setText(Float.toString(threshold[1]));
        track_level1.setText(Float.toString(threshold[2]));
        track_level2.setText(Float.toString(threshold[3]));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok_btn: {
                threshold[0] = Float.parseFloat(platform_level1.getText().toString());
                threshold[1] = Float.parseFloat(platform_level2.getText().toString());
                threshold[2] = Float.parseFloat(track_level1.getText().toString());
                threshold[3] = Float.parseFloat(track_level2.getText().toString());
                Intent intent = new Intent();
                intent.putExtra("threshold", threshold);
                setResult(RESULT_OK, intent);
                finish();
                break;
            }
            case R.id.cancel_btn: {
                Intent intent = new Intent();
                intent.putExtra("threshold", threshold);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
            case R.id.del_history: {
                del_history();
                break;
            }
            default:
        }
    }

    private void del_history() {
        String msg = "记录已清空";
        SQLiteDatabase mDataBase = mDBHelper.getReadableDatabase();
        try {
            mDBHelper.truncate(mDataBase);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDataBase.close();
            mDBHelper.close();
        }
    }
}
