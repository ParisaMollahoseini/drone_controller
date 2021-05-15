package com.dji.drone_rajawali;

/*
 * Copyright (c) 2014 Ville Saarinen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.MainThread;


public class OnScreenJoystick extends SurfaceView implements
        SurfaceHolder.Callback, View.OnTouchListener {

    private Bitmap mJoystick;
    private SurfaceHolder mHolder;
    private Rect mKnobBounds;
    private long mLoopInterval = 50;
    private JoystickThread mThread;

    private float mKnobX, mKnobY,mKnobX1, mKnobY1;
    private float mKnobSize;
    private float mBackgroundSize,mBackgroundwidth;
    private float mRadius,baseradius;

    private OnScreenJoystickListener mJoystickListener;

    private boolean mAutoCentering = true;

    public OnScreenJoystick(Context context)
    {
        super(context);
        init();

    }
    public OnScreenJoystick(Context context, AttributeSet attrs) {
        super(context, attrs);

        initGraphics(attrs);
        init();

    }

    private void initGraphics(AttributeSet attrs) {
        Resources res = getContext().getResources();


    }

    private void initBounds(final Canvas pCanvas) {
        mBackgroundSize = pCanvas.getHeight();
        mBackgroundwidth = pCanvas.getWidth();
        mKnobSize = (mBackgroundSize * 0.6f);

        mKnobBounds = new Rect();

        mRadius = mBackgroundSize * 0.15f;
        baseradius = mKnobSize / 2;
        mKnobX = mBackgroundwidth/2;
        mKnobY = (mBackgroundSize/2);

        mKnobX1 = (mBackgroundwidth/2) ;//Math.round((mBackgroundSize - mKnobSize) * 0.5f);
        mKnobY1 = (mBackgroundSize/2);//Math.round((mBackgroundSize - mKnobSize) * 0.5f);
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        mThread = new JoystickThread();

        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setOnTouchListener(this);
        setEnabled(true);
        setAutoCentering(true);

    }

    public void setAutoCentering(final boolean pAutoCentering) {
        mAutoCentering = pAutoCentering;
    }

    public boolean isAutoCentering() {
        return mAutoCentering;
    }

    public void setJoystickListener(
            final OnScreenJoystickListener pJoystickListener) {
        mJoystickListener = pJoystickListener;
    }


    @Override
    public void surfaceChanged(final SurfaceHolder arg0, final int arg1,
                               final int arg2, final int arg3) {
        Toast.makeText(getContext(),"here is joy",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void surfaceCreated(final SurfaceHolder arg0) {
        if (mThread.getState()==Thread.State.TERMINATED) {
            mThread = new JoystickThread();
        }
        mThread.setRunning(true);
        mThread.start();

    }

    @Override
    public void surfaceDestroyed(final SurfaceHolder arg0) {
        boolean retry = true;
        mThread.setRunning(false);

        while (retry) {
            try {
                // code to kill Thread
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }

    }

    public void doDraw(final Canvas pCanvas) {
        if (mKnobBounds == null) {
            initBounds(pCanvas);
        }
        //big circle
        Paint colors = new Paint();
        colors.setARGB(255,50,50,50);
        pCanvas.drawCircle(mKnobX1 ,mKnobY1,baseradius,colors);

        //small circle
        colors.setARGB(255,255,0,0);
        pCanvas.drawCircle(mKnobX ,mKnobY,mRadius,colors);
    }



    @Override
    public boolean onTouch(final View arg0, final MotionEvent pEvent) {
        final float x = pEvent.getX();
        final float y = pEvent.getY();

        if (pEvent.getAction() == MotionEvent.ACTION_UP) {//when joystick is clicked
            if (isAutoCentering()) {//position of two circles center
                mKnobX = mBackgroundwidth * 0.5f;
                mKnobY = mBackgroundSize * 0.5f;
            }
        } else {
            float displacement = (float) Math.sqrt(Math.pow(x - mKnobX1, 2) + Math.pow(y - mKnobY1, 2));
            if (displacement < baseradius) {
                mKnobX = x;
                mKnobY = y;
            } else {

                float ratio = baseradius / displacement;
                mKnobX = (mKnobX1 + (x - mKnobX1) * ratio);
                mKnobY = (mKnobY1 + (y - mKnobY1) * ratio);
            }
        }

        if (mJoystickListener != null) {
            mJoystickListener.onTouch(pEvent,this,
                    (0.5f - (mKnobX / (mRadius * 2 - mKnobSize))) * -2,
                    (0.5f - (mKnobY / (mRadius * 2 - mKnobSize))) * 2);

        }

        return true;
    }
    /**
     * Process the angle following the 360° counter-clock protractor rules.
     * @return the angle of the button
     */
    public int getAngle() {
        int angle = (int) Math.toDegrees(Math.atan2(mKnobY1 - mKnobY, mKnobX - mKnobX1));
        return angle < 0 ? angle + 360 : angle; // make it as a regular counter-clock protractor
    }


    /**
     * Process the strength as a percentage of the distance between the center and the border.
     * @return the strength of the button
     */
    public double getStrength() {
        return  ( Math.sqrt((mKnobX - mKnobX1)
                * (mKnobX - mKnobX1) + (mKnobY - mKnobY1)
                * (mKnobY - mKnobY1)) / baseradius);
    }

    public float distance_base(float x)
    {
        return x - mKnobX1 ;
    }


    private class JoystickThread extends Thread {

        private boolean running = false;

        @Override
        public synchronized void start() {
            running = true;
            super.start();
        }

        public void setRunning(final boolean pRunning) {
            running = pRunning;
        }

        @Override
        public void run() {

            while (running) {
                // draw everything to the canvas
                Canvas canvas = null;
                try {
                    canvas = mHolder.lockCanvas(null);
                    synchronized (mHolder) {
                        // reset canvas
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        doDraw(canvas);
                    }
                }
                catch(Exception e){}
                finally {
                    if (canvas != null) {
                        mHolder.unlockCanvasAndPost(canvas);
                    }
                }

            }
        }
    }

}
