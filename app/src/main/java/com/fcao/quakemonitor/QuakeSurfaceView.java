package com.fcao.quakemonitor;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by Frank on 7/12/2017.
 */

public class QuakeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int MARGIN = 50;
    private final String[] titleApl = {"综合强度：%f", "水平检测：%f", "高低检测：%f"};
    private int scrWidth, scrHeight;
    private float coordinate_x, coordinate_y, scale_x, scale_y;
    private int mInterval_time;
    private float lnWidth;
    private float[] mthreshold;
    private int mSeq;
    private  int mYmarks;
    private double maxApl;
    private List<Record> mRecords;

    private MyThread myThread;
    private Paint mPaint, axesPaint, dashPaint;
    private TextPaint textPaint, scalePaint;

    public QuakeSurfaceView(Activity activity, int intervalTime, float[] threshold, List<Record> records, int seq) {
        super(activity);
        mRecords = records;
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

    public void setThreshold(float[] threshold) {
        mthreshold = threshold;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        initCoordinate(width, height);
        mRecords.clear();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        myThread = new MyThread(this);
        myThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        myThread.isRun = false;
    }

    private void drawAxes(Canvas canvas) {
        float yPos = coordinate_y;

        scalePaint.setTextAlign(Paint.Align.LEFT);
        for (int i = 1; i < 4; i++) {  //x axis
            float xPosMark = MARGIN + i * coordinate_x / 4;
            double mark = i / 2.0;
            int markR = (int) Math.rint(mark);
            String markStr = (markR == mark) ? String.valueOf(markR) : String.valueOf(mark);
            canvas.drawText(markStr, xPosMark, yPos + MARGIN, scalePaint);
            canvas.drawLine(xPosMark, yPos, xPosMark, yPos - 10, axesPaint);
        }

        for (int i = 1; i <= mYmarks; i++) { // y axis
            float yPosMark = yPos - i * scale_y * 5 + axesPaint.getStrokeWidth() / 2;
            canvas.drawText(String.valueOf(i * 5), 0, yPosMark, scalePaint);
            canvas.drawLine(MARGIN, yPosMark, coordinate_x, yPosMark, dashPaint);
        }

        canvas.drawLine(MARGIN, yPos, scrWidth, yPos, axesPaint); // x axis
        canvas.drawLine(MARGIN, yPos, MARGIN, MARGIN, axesPaint); // y axis

        String content = "(s)";
        canvas.drawText(content, coordinate_x - MARGIN, yPos + MARGIN, scalePaint);
        String title = String.format(titleApl[mSeq], maxApl);
        canvas.drawText(title, scrWidth / 3, MARGIN, textPaint);
    }

    // draw histogram with the records
    private void drawHistogram(Canvas canvas) {
        float currentPos;
        double currentDist;
        RectF rect = new RectF();

        maxApl = getApl(0);
        final int lineSize = mRecords.size();
        double apl;
        float pos;
        for (int i = 0; i < lineSize; i++) {
            currentPos = MARGIN + i * scale_x;
            apl = getApl(i);
            currentDist = (float) (coordinate_y - apl * scale_y);
            pos = (0 == i) ? MARGIN : currentPos;
            rect.set(pos - lnWidth / 2, (float) currentDist, pos + lnWidth / 2, coordinate_y);

            drawRect(canvas, apl, rect);

            maxApl = (maxApl < apl) ? apl : maxApl;
        }
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

    private void drawWave(Canvas canvas) {
        drawHistogram(canvas);
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

    private void initCoordinate(int width, int height) {
        scrWidth = width;
        scrHeight = height;
        coordinate_x = scrWidth;
        coordinate_y = scrHeight - MARGIN;
        scale_x = coordinate_x / 2 * mInterval_time / 1000;
        scale_y = coordinate_y / 100;
        scale_y = scale_y * 5;//(1 == mSeq) ? scale_y * 5 : scale_y * 3; // scale the y axis
        mYmarks = 3;//(1 == mSeq) ? 3 : 5;
        lnWidth = scale_x / 2;
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
        axesPaint.setStyle(Paint.Style.STROKE);
        axesPaint.setStrokeWidth(5);

        dashPaint = new Paint();
        dashPaint.setColor(0xffb0e0e6);//Color.argb(1, 25, 25, 112));
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(5);

        scalePaint = new TextPaint();
        scalePaint.setColor(Color.DKGRAY);
        scalePaint.setStyle(Paint.Style.FILL);
        scalePaint.setTextSize(30);
        scalePaint.setTextAlign(Paint.Align.CENTER);
    }
}