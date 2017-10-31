package com.vdian.uikit.view.extend.refresh;

import android.content.Context;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by zhangliang on 16/12/9.
 */
public abstract class NestedParent extends RelativeLayout implements NestedScrollingParent, NestedCompat.NestedParentController.NestedParentListener {
    private NestedScrollingParentHelper mHelper;
    private NestedCompat.NestedParentController mDelegate;

    public NestedParent(Context context) {
        super(context);
        init();
    }

    public NestedParent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NestedParent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        mHelper = new NestedScrollingParentHelper(this);
        mDelegate = new NestedCompat.NestedParentController(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mDelegate.beforeDispatchTouchEvent(event);
        boolean b = super.dispatchTouchEvent(event);
        mDelegate.afterDispatchTouchEvent(event);
        return b;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return child == target;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mHelper.onStopNestedScroll(target);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        mDelegate.onNestedPreScroll(target, dx, dy, consumed);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return !fling();
    }

    @Override
    public int getNestedScrollAxes() {
        return mHelper.getNestedScrollAxes();
    }

    public boolean touching() {
        return mDelegate.isTouch();
    }

    public abstract boolean fling();
}
