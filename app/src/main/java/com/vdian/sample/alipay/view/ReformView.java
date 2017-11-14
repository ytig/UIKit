package com.vdian.sample.alipay.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;

import com.vdian.uikit.util.RectUtil;
import com.vdian.uikit.view.ViewGroupMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 16/10/24.
 */
public class ReformView extends RecyclerView implements ViewTreeObserver.OnGlobalLayoutListener, ViewGroupMonitor.ChildListener {
    private int correct = 0;
    private boolean touching = false;
    private boolean running = false;
    private long time = 0;
    private Comparator comparator = new Comparator();

    public ReformView(Context context) {
        super(context);
        init();
    }

    public ReformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        ViewGroupMonitor.globalMonitor(this, this);
    }

    protected int correct() {
        return 0;
    }

    protected void reform() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && getChildAdapterPosition(child) == 0) {
                int offset = RectUtil.measure(this).top - RectUtil.measure(child).top;
                int height = child.getHeight() - correct;
                if (offset > 0 && offset < height) {
                    if (offset > height / 2) offset -= height;
                    smoothScrollBy(0, -offset);
                }
                return;
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        correct = correct();
        if (!touching && !running) reform();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean b = super.dispatchTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touching = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touching = false;
                if (!running) reform();
                break;
        }
        return b;
    }

    @Override
    public List<ViewGroupMonitor.Child> handle(List<ViewGroupMonitor.Child> children) {
        long now = AnimationUtils.currentAnimationTimeMillis();
        if (time != now) {
            time = now;
            if (comparator.changed(children)) {
                running = true;
                postInvalidate();
            } else {
                if (running) {
                    if (!touching) reform();
                    running = false;
                }
            }
        } else postInvalidate();
        return children;
    }

    private static class Comparator {
        private List<ViewGroupMonitor.Child> mChildren = new ArrayList<>();
        private List<ViewGroupMonitor.Child> mTmp = new ArrayList<>();

        protected boolean changed(List<ViewGroupMonitor.Child> children) {
            mTmp.addAll(mChildren);
            mChildren.clear();
            mChildren.addAll(children);
            children.clear();
            children.addAll(mTmp);
            mTmp.clear();
            if (mChildren.size() != children.size()) return true;
            for (int i = 0; i < mChildren.size(); i++) {
                if (!equals(mChildren.get(i), children.get(i))) return true;
            }
            return false;
        }

        private boolean equals(ViewGroupMonitor.Child child1, ViewGroupMonitor.Child child2) {
            if (child1 != null || child2 != null) {
                if (child1 == null || child2 == null) return false;
                else {
                    if (child1.index != child2.index) return false;
                    if (child1.position != child2.position) return false;
                    if (!child1.rect.equals(child2.rect)) return false;
                }
            }
            return true;
        }
    }
}
