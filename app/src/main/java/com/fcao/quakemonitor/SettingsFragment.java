package com.fcao.quakemonitor;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Frank on 10/9/2017.
 */

public class SettingsFragment extends Fragment implements View.OnClickListener{
    private float[] threshold = {0,0,0,0};
    private EditText platform_level1, platform_level2, track_level1,track_level2;
    private Button ok_btn, cancel_btn, del_history;
    private MyDatabaseHelper mDBHelper;
    private QuakeActivity mParent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mParent = (QuakeActivity) getActivity();
        mDBHelper = mParent.mDBHelper;
        threshold = mParent.mThreshold;

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        View view = inflater.inflate(R.layout.settings, container, false);

        platform_level1 = view.findViewById(R.id.platform_level1);
        platform_level2 = view.findViewById(R.id.platform_level2);
        track_level1 = view.findViewById(R.id.track_level1);
        track_level2 = view.findViewById(R.id.track_level2);
        ok_btn = view.findViewById(R.id.ok_btn);
        cancel_btn = view.findViewById(R.id.cancel_btn);
        del_history = view.findViewById(R.id.del_history);
        ok_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
        del_history.setOnClickListener(this);
        platform_level1.setText(Float.toString(threshold[0]));
        platform_level2.setText(Float.toString(threshold[1]));
        track_level1.setText(Float.toString(threshold[2]));
        track_level2.setText(Float.toString(threshold[3]));
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok_btn: {
                threshold[0] = Float.parseFloat(platform_level1.getText().toString());
                threshold[1] = Float.parseFloat(platform_level2.getText().toString());
                threshold[2] = Float.parseFloat(track_level1.getText().toString());
                threshold[3] = Float.parseFloat(track_level2.getText().toString());
                getActivity().onBackPressed();
                break;
            }
            case R.id.cancel_btn: {
                getActivity().onBackPressed();
                break;
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
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDataBase.close();
            mDBHelper.close();
        }
    }
}
