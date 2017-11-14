package com.vdian.uikit.view;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewParent;

/**
 * 触控控制器
 *
 * @author YangTao
 */
public class TouchController {
    private boolean isTouch = false; //有无触控点
    private int[] touchPointId = new int[5]; //触控点采集索引
    private float[] touchPointX = new float[5]; //触控点采集横坐标
    private float[] touchPointY = new float[5]; //触控点采集纵坐标
    private float downX = 0; //落点横坐标
    private float downY = 0; //落点纵坐标
    private boolean doCancel = false; //中断事件
    private float moveX = 0; //横向偏移
    private float moveY = 0; //纵向偏移
    private float absX = 0; //横向总路程
    private float absY = 0; //纵向总路程
    private float absLimit = 0; //拦截触发门限
    private int judgeMode = 0; //拦截判断模式
    private int judgeState = 0; //拦截判断结果
    private boolean canParent = false; //可拦截父事件
    private boolean canChild = false; //可拦截子事件
    private boolean doIntercept = false; //是否拦截子事件
    private float velocityX = 0; //横向速度
    private float velocityY = 0; //纵向速度
    private VelocityTracker mVelocityTracker; //速度跟踪器
    private View mView; //触控控件
    private TouchListener mTouchListener; //触控处理实现器

    public TouchController(TouchListener listener) {
        this(listener, null, false, false);
    }

    public TouchController(TouchListener listener, View view, boolean x, boolean y) {
        for (int i = 0; i < touchPointId.length; i++) touchPointId[i] = -1;
        mTouchListener = listener;
        if (view != null) {
            mView = view;
            absLimit = 4 * mView.getContext().getResources().getDisplayMetrics().density;
            if (y) {
                if (x) judgeMode = 3;
                else judgeMode = 2;
            } else {
                if (x) judgeMode = 1;
                else judgeMode = 0;
            }
        }
    }

    /**
     * 是否有触控
     *
     * @return
     */
    public boolean isTouch() {
        return isTouch;
    }

    /**
     * view同名调用，覆写
     *
     * @param event
     * @return 返回null调用原方法
     */
    public Boolean dispatchTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1);
                velocityX = mVelocityTracker.getXVelocity();
                velocityY = mVelocityTracker.getYVelocity();
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
        }
        int action = event.getAction();
        int masked = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            if (isTouch) {
                for (int i = 0; i < touchPointId.length; i++) touchPointId[i] = -1;
                isTouch = false;
            }
            downX = event.getRawX();
            downY = event.getRawY();
            absX = 0;
            absY = 0;
            judgeState = 0;
            canParent = true;
            canChild = true;
            doIntercept = false;
        }
        moveX = 0;
        moveY = 0;
        if (judgeState > 0 && canChild) {
            canChild = false;
            doIntercept = true;
        }
        if (action == MotionEvent.ACTION_DOWN || masked == MotionEvent.ACTION_POINTER_DOWN) {
            int id = event.getPointerId(event.getActionIndex());
            for (int i = 0; i < touchPointId.length; i++) {
                if (touchPointId[i] == -1) {
                    touchPointId[i] = id;
                    touchPointX[i] = getRawX(event, event.getActionIndex());
                    touchPointY[i] = getRawY(event, event.getActionIndex());
                    isTouch = true;
                    break;
                }
            }
        }
        if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < touchPointId.length; i++) {
                int index = -1;
                if (touchPointId[i] != -1) index = event.findPointerIndex(touchPointId[i]);
                if (index != -1) {
                    float dX = getRawX(event, index) - touchPointX[i];
                    touchPointX[i] += dX;
                    moveX += dX;
                    float dY = getRawY(event, index) - touchPointY[i];
                    touchPointY[i] += dY;
                    moveY += dY;
                }
            }
            absX += Math.abs(moveX);
            absY += Math.abs(moveY);
            if (judgeState == 0) {
                if (absX > absY) {
                    if (absX > absLimit) judgeState = (judgeMode % 2 == 1) ? 1 : -1;
                } else {
                    if (absY > absLimit) judgeState = (judgeMode / 2 == 1) ? 1 : -1;
                }
            }
        }
        if (action == MotionEvent.ACTION_UP || masked == MotionEvent.ACTION_POINTER_UP) {
            boolean b = false;
            int id = event.getPointerId(event.getActionIndex());
            for (int i = 0; i < touchPointId.length; i++) {
                if (touchPointId[i] == id) touchPointId[i] = -1;
                if (touchPointId[i] != -1) b = true;
            }
            isTouch = b;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            if (isTouch) {
                for (int i = 0; i < touchPointId.length; i++) touchPointId[i] = -1;
                isTouch = false;
            }
        }
        if (judgeState > 0 && canParent) {
            canParent = false;
            if (mView != null) {
                ViewParent parent = mView.getParent();
                if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                doCancel = !mTouchListener.down(downX, downY);
                break;
            case MotionEvent.ACTION_UP:
                if (!doCancel) mTouchListener.up(velocityX, velocityY);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!doCancel) mTouchListener.cancel();
                break;
        }
        if (doCancel) return Boolean.FALSE;
        return null;
    }

    /**
     * view同名调用，覆写
     *
     * @param event
     * @return 返回null调用原方法
     */
    public Boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mTouchListener.move(moveX, moveY)) return Boolean.TRUE;
                break;
        }
        return null;
    }

    /**
     * view同名调用，覆写
     *
     * @param event
     * @return 返回null调用原方法
     */
    public Boolean onInterceptTouchEvent(MotionEvent event) {
        if (doIntercept) return Boolean.TRUE;
        return null;
    }

    /**
     * view同名调用，添加
     *
     * @param disallowIntercept
     */
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) canChild = false;
    }

    private float getRawX(MotionEvent event, int index) {
        return event.getRawX() - event.getX() + event.getX(index);
    }

    private float getRawY(MotionEvent event, int index) {
        return event.getRawY() - event.getY() + event.getY(index);
    }

    public interface TouchListener {
        boolean down(float downX, float downY); //返回false会屏蔽后续事件

        boolean move(float moveX, float moveY); //返回true会屏蔽原处理事件

        void up(float velocityX, float velocityY);

        void cancel();
    }
}
