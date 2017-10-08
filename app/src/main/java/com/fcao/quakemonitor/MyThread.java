package com.fcao.quakemonitor;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by Frank on 7/12/2017.
 */

public class MyThread extends Thread {
    private static final int SLEEP_SPAN = 205; // draw again after listener refreshing record list.
    private QuakeSurfaceView mView;
    private SurfaceHolder holder;
    public boolean isRun = true;

    public MyThread(QuakeSurfaceView myView) {
        this.mView = myView;
        this.holder = mView.getHolder();
    }

    @Override
    public void run() {
        Canvas c;
        while (isRun) {
            c = null;
            try {
                c = holder.lockCanvas(null);
                synchronized (holder) {
                    mView.draw(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }
            try {
                Thread.sleep(SLEEP_SPAN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
