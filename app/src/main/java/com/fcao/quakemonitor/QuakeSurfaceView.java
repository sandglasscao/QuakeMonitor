package com.fcao.quakemonitor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Frank on 7/12/2017.
 */

public class QuakeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    //private static final int UPDATE_INTERVAL_TIME = 200;
    //private static final int SCALE = 3; // y coordinate scale
    //private static final double SMOOTHNESS = 0.16;
    private static final int MARGIN = 50;
    private static int scrWidth, scrHeight;
    private static float coordinate_x, coordinate_y, scale_x, scale_y;
    private int mInterval_time;
    private static float lnWidth;
    private float[] mthreshold;
    private int mSeq;
    private static double maxApl;
    private static String[] titleApl = {"综合最大强度：%f", "晃动最大强度：%f", "颠簸最大强度：%f"};
    private List<Record> mRecords;

    private MyThread myThread;
    private Paint mPaint;
    private TextPaint textPaint;
    private TextPaint scalePaint;
    private Paint axesPaint;

    public QuakeSurfaceView(QuakeActivity activity, int intervalTime, float[] threshold, int seq) {
        super(activity);
        mRecords = activity.mRecords;
        mInterval_time = intervalTime;
        mSeq = seq;
        setThreshold(threshold);
        getHolder().addCallback(this);
        initPaints();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        if (mRecords.size() == 0)
            return;
        drawWave(canvas);
        drawAxes(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        scrWidth = this.getWidth();
        scrHeight = this.getHeight();
        coordinate_x = scrWidth;
        coordinate_y = scrHeight;
        scale_x = coordinate_x / 2 * mInterval_time / 1000;
        scale_y = coordinate_y / 100;
        scale_y = scale_y * 5; // scale the y axis
        lnWidth = scale_x / 2;

        myThread = new MyThread(this);
        myThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mRecords.clear();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        myThread.isRun = false;
    }

    public void setThreshold(float[] threshold) {
        mthreshold = threshold;
    }

    private void initPaints() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        textPaint = new TextPaint();
        textPaint.setColor(Color.BLUE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50);
        textPaint.setFakeBoldText(true);

        axesPaint = new Paint();
        axesPaint.setColor(0xff082e54);//Color.argb(1, 25, 25, 112));
        axesPaint.setStrokeWidth(5);

        scalePaint = new TextPaint();
        scalePaint.setColor(Color.DKGRAY);
        scalePaint.setStyle(Paint.Style.FILL);
        scalePaint.setTextSize(30);
    }

    private void drawWave(Canvas canvas) {
        drawHistogram(canvas);
    }

    private void drawAxes(Canvas canvas) {
        scalePaint.setTextAlign(Paint.Align.CENTER);

        float yPos = coordinate_y;
        canvas.drawLine(MARGIN, yPos, scrWidth, yPos, axesPaint); // x axis
        canvas.drawLine(MARGIN, yPos, MARGIN, yPos - coordinate_y + MARGIN, axesPaint); // y axis

        String content = "(s)";
        canvas.drawText(content, coordinate_x - content.length(), yPos + MARGIN, scalePaint);
        String title = String.format(titleApl[mSeq], maxApl);
        canvas.drawText(title, scrWidth / 3, yPos - coordinate_y + MARGIN * 3, textPaint);

        scalePaint.setTextAlign(Paint.Align.LEFT);
        for (int i = 1; i < 4; i++) {
            float xPosMark = MARGIN + i * coordinate_x / 4;
            double mark = i / 2.0;
            int markR = (int) Math.rint(mark);
            String markStr = (markR == mark) ? String.valueOf(markR) : String.valueOf(mark);
            canvas.drawText(markStr, xPosMark, yPos + MARGIN, scalePaint);
            canvas.drawLine(xPosMark, yPos, xPosMark, yPos - 10, axesPaint);
        }

        for (int i = 1; i <= 3; i++) {
            float yPosMark = yPos - i * scale_y * 5 + axesPaint.getStrokeWidth() / 2;
            canvas.drawText(String.valueOf(i * 5), 0, yPosMark, scalePaint);
            canvas.drawLine(MARGIN, yPosMark, MARGIN + 10, yPosMark, axesPaint);
        }
    }

    // draw histogram with the records
    private void drawHistogram(Canvas canvas) {
        float currentPos;
        double currentDist;
        // calculate the y axis because the coordinate origin is placed on the left-bottom corner
        //double maxTot, maxX, maxZ;
        RectF rect = new RectF();

        maxApl = getApl(0);
        final int lineSize = mRecords.size();
        for (int i = 0; i < lineSize; i++) {
            currentPos = MARGIN + i * scale_x;
            double apl = getApl(i);
            currentDist = (float) (coordinate_y - apl * scale_y);
            float pos = (0 == i) ? MARGIN : currentPos;
            rect.set(pos - lnWidth / 2, (float) currentDist, pos + lnWidth / 2, coordinate_y);

            drawRect(canvas, apl, rect);

            maxApl = (maxApl < apl) ? apl : maxApl;
        }
    }

    private double getApl(int pos) {
        double Apl = 0;
        switch (mSeq) {
            case 0: {
                Apl = mRecords.get(pos).getDistance();
                break;
            }
            case 1: {
                Apl = mRecords.get(pos).getX();
                break;
            }
            case 2: {
                Apl = mRecords.get(pos).getZ();
                break;
            }
            default:
        }
        return Apl;
    }

    private void drawRect(Canvas canvas, double intensity, RectF rectF) {
        if (intensity >= mthreshold[1])
            mPaint.setColor(0xffe3170d); //Red
        else if (intensity >= mthreshold[0])
            mPaint.setColor(0xffff9912); //Yellow
        else
            mPaint.setColor(0xff228b22); //Green
        canvas.drawRect(rectF, mPaint);
    }
}