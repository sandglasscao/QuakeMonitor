package com.fcao.quakemonitor;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


/**
 * Created by Frank on 10/9/2017.
 */

public class FullScreenFragment extends Fragment implements OnClickListener{
    private QuakeActivity mParent;
    private int mPos;

    @Override
    public void onClick(View v) {
        getActivity().onBackPressed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mParent = (QuakeActivity) getActivity();
        Bundle bundle = getArguments();
        mPos = bundle.getInt("pos", 0);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        View view = inflater.inflate(R.layout.full_screen, container, false);
        float thresholdlevel1, thresholdlevel2;
        thresholdlevel1 = mParent.isOnTrack ? mParent.mThreshold[2] : mParent.mThreshold[0];
        thresholdlevel2 = mParent.isOnTrack ? mParent.mThreshold[3] : mParent.mThreshold[1];
        float[] threshold = {thresholdlevel1, thresholdlevel2};
        QuakeSurfaceView surfaceView = new QuakeSurfaceView(getActivity(), threshold, mPos);
        RelativeLayout layout = view.findViewById(R.id.show_full_src);
        layout.addView(surfaceView);
        layout.setOnClickListener(this);
        return view;
    }
}
