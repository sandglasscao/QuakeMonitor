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

public class QuakeView extends SurfaceView implements SurfaceHolder.Callback {
    //private static final int UPDATE_INTERVAL_TIME = 200;
    //private static final int SCALE = 3; // y coordinate scale
    //private static final double SMOOTHNESS = 0.16;
    private static final int MARGIN = 50;
    private static int scrWidth, scrHeight;
    private static float coordinate_x, coordinate_y, scale_x, scale_y;
    private int mInterval_time;
    private static float lnWidth;
    private float[] mthreshold;
    private static double[] maxApl = {0, 0, 0};
    private static String[] titleApl = {"最大强度：%f", "晃动最大强度：%f", "颠簸最大强度：%f"};
    private List<Record> mRecords;
    //private List<Float> mPointList;
    private MyThread myThread;
    private Paint mPaint;
    //private Paint pointPaint;
    private TextPaint textPaint;
    private TextPaint scalePaint;
    private Paint axesPaint;
    /*private Path mPath, dst;
    private PathMeasure mPathMeasure;
    private float drawScale = 1f;
    //private Path dst;*/

    public QuakeView(QuakeActivity activity, int intervalTime, float[] threshold) {
        super(activity);
        mRecords = activity.mRecords;
        mInterval_time = intervalTime;
        setThreshold(threshold);
        requestFocus();
        getHolder().addCallback(this);
        init();
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
        coordinate_x = scrWidth - MARGIN;
        coordinate_y = scrHeight / 3 - MARGIN;
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

    private void init() {
        //mPointList = new ArrayList<Float>();
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(lnWidth);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);

        /*pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(3);
        pointPaint.setStyle(Paint.Style.FILL);*/

        textPaint = new TextPaint();
        textPaint.setColor(Color.BLUE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(50);
        //textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        axesPaint = new Paint();
        axesPaint.setColor(0xff082e54);//Color.argb(1, 25, 25, 112));
        axesPaint.setStrokeWidth(5);

        scalePaint = new TextPaint();
        scalePaint.setColor(Color.DKGRAY);
        scalePaint.setStyle(Paint.Style.FILL);
        scalePaint.setTextSize(30);

        /*mPath = new Path();
        mPathMeasure = new PathMeasure();
        dst = new Path();*/
    }

    private void drawWave(Canvas canvas) {
        //measurePath();
        drawHistogram(canvas);
       /* mPathMeasure.setPath(mPath, false);
        dst.reset();
        dst.rLineTo(MARGIN, coordinate_y);
        float distance = mPathMeasure.getLength() * drawScale;
        if (mPathMeasure.getSegment(MARGIN, distance, dst, true)) {
            //绘制线
            canvas.drawPath(dst, mPaint);
            float[] pos = new float[2];
            mPathMeasure.getPosTan(distance, pos, null);
        }*/
        //canvas.drawPath(mPath, mPaint);
        //drawPoint(canvas);
    }
/*
    private void drawPoint(Canvas canvas) {
        for (int i = 0; i < mPointList.size(); i = i + 2) {
            canvas.drawCircle(mPointList.get(i), mPointList.get(i + 1), 10, pointPaint);
        }
    }*/

    private void drawAxes(Canvas canvas) {
        scalePaint.setTextAlign(Paint.Align.CENTER);

        for (int j = 1; j <= 3; j++) {
            float yPos = coordinate_y * j;
            canvas.drawLine(MARGIN, yPos, scrWidth, yPos, axesPaint); // x axis
            canvas.drawLine(MARGIN, yPos, MARGIN, yPos - coordinate_y + MARGIN, axesPaint); // y axis

            String content = "(s)";
            canvas.drawText(content, coordinate_x - content.length(), yPos + MARGIN, scalePaint);
            /*content = "(m/s^2)";
            canvas.drawText(content, 0, coordinate_y * (j-1) - 5, scalePaint);*/
            String title = String.format(titleApl[j - 1], maxApl[j - 1]);
            canvas.drawText(title, scrWidth / 3, coordinate_y * (j - 1) + MARGIN * 3, textPaint);

            scalePaint.setTextAlign(Paint.Align.LEFT);
            for (int i = 1; i < 4; i++) {
                //float xPosMark = MARGIN + i * coordinate_x / 10;
                float xPosMark = MARGIN + i * coordinate_x / 4;
                double mark = i / 2.0;
                int markR = (int) Math.rint(mark);
                String markStr = (markR==mark) ? String.valueOf(markR) : String.valueOf(mark);
                canvas.drawText(markStr, xPosMark, yPos + MARGIN, scalePaint);
                canvas.drawLine(xPosMark, yPos, xPosMark, yPos - 10, axesPaint);
            }

            for (int i = 1; i <= 3; i++) {
                float yPosMark = yPos - i * scale_y * 5 + axesPaint.getStrokeWidth()/2;
                canvas.drawText(String.valueOf(i * 5), 0, yPosMark, scalePaint);
                canvas.drawLine(MARGIN, yPosMark, MARGIN + 10, yPosMark, axesPaint);
            }
        }
    }

    // draw histogram with the records
    private void drawHistogram(Canvas canvas) {
        float currentPos;
        double currentDistTot, currentDistX, currentDistZ;
        // calculate the y axis because the coordinate origin is placed on the left-bottom corner
        double maxTot, maxX, maxZ;
        RectF rectTot = new RectF();
        RectF rectX = new RectF();
        RectF rectZ = new RectF();

        maxTot = mRecords.get(0).getDistance();
        maxX = Math.abs(mRecords.get(0).getX());
        maxZ = Math.abs(mRecords.get(0).getZ());
        //currentDistTot = coordinate_y - maxTot * scale_y;
        //currentDistX = coordinate_y * 2 - maxX * scale_y;
        //currentDistZ = coordinate_y * 3 - maxZ * scale_y;
        //mPath.reset();
        //mPath.moveTo(currentPos, (float) currentDist);
        final int lineSize = mRecords.size();
        for (int valueIndex = 0; valueIndex < lineSize; valueIndex++) {
            currentPos = MARGIN + valueIndex * scale_x;
            double intensityTot = mRecords.get(valueIndex).getDistance();
            double intensityX = Math.abs(mRecords.get(valueIndex).getX());
            double intensityZ = Math.abs(mRecords.get(valueIndex).getZ());

            currentDistTot = (float) (coordinate_y - intensityTot * scale_y);
            currentDistX = (float) (coordinate_y * 2 - intensityX * scale_y);
            currentDistZ = (float) (coordinate_y * 3 - intensityZ * scale_y);
            if (valueIndex == 0) {
                //mPath.rMoveTo(MARGIN, coordinate_y-MARGIN);
                //mPath.lineTo(MARGIN, (float) currentDist);
                rectTot.set(MARGIN, (float) currentDistTot, MARGIN + lnWidth / 2, coordinate_y);
                rectX.set(MARGIN, (float) currentDistX, MARGIN + lnWidth / 2, coordinate_y * 2);
                rectTot.set(MARGIN, (float) currentDistZ, MARGIN + lnWidth / 2, coordinate_y * 3);
            } else {
                //mPath.rMoveTo(currentPos, coordinate_y-MARGIN);
                //mPath.lineTo(currentPos, (float) currentDist);
                rectTot.set(currentPos - lnWidth / 2, (float) currentDistTot, currentPos + lnWidth / 2, coordinate_y);
                rectX.set(currentPos - lnWidth / 2, (float) currentDistX, currentPos + lnWidth / 2, coordinate_y * 2);
                rectZ.set(currentPos - lnWidth / 2, (float) currentDistZ, currentPos + lnWidth / 2, coordinate_y * 3);
            }

            drawRect(canvas, intensityTot, rectTot);
            drawRect(canvas, intensityX, rectX);
            drawRect(canvas, intensityZ, rectZ);

            maxTot = (maxTot < intensityTot) ? intensityTot : maxTot;
            maxX = (maxX < intensityX) ? intensityX : maxX;
            maxZ = (maxZ < intensityZ) ? intensityZ : maxZ;
        }
        //mPathMeasure = new PathMeasure(mPath, false);
        maxApl[0] = maxTot;
        maxApl[1] = maxX;
        maxApl[2] = maxZ;
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

    /*
    // draw line with the records
    private void drawHistogram(Canvas canvas) {
        float currentPos;
        double currentDist;
        // calculate the y axis because the coordinate origin is placed on the left-bottom corner
        double maxAmpTmp;

        //currentPos = MARGIN;
        currentDist = coordinate_y - mRecords.get(0).getDistance() * scale_x;
        mPath.reset();
        //mPath.moveTo(currentPos, (float) currentDist);
        //canvas.drawLine( startx - lnWidth  ,starty + lnSpace ,initX,endy + lnSpace, PaintText);
        maxAmpTmp = currentDist;
        final int lineSize = mRecords.size();
        for (int valueIndex = 0; valueIndex < lineSize; valueIndex++) {
            currentPos = MARGIN + valueIndex * scale_x;
            currentDist = (float) (coordinate_y - mRecords.get(valueIndex).getDistance() * scale_y);
            if (valueIndex == 0)
                mPath.moveTo(MARGIN, (float) currentDist);
            else
                mPath.lineTo(currentPos, (float) currentDist);

            maxAmpTmp = (maxAmpTmp > currentDist) ? currentDist : maxAmpTmp;
            // maximum amplitude will be gotten when minimum(height - the maximum of amplitude)
        }
        //mPathMeasure = new PathMeasure(mPath, false);
        maxAmplitude = (coordinate_y - maxAmpTmp) / scale_y;
    }
    */

}