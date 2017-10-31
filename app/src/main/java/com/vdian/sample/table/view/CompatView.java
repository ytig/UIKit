package com.vdian.sample.table.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.vdian.uikit.view.extend.refresh.RefreshView;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhangliang on 17/1/13.
 */
public class CompatView extends RefreshView {
    public CompatView(Context context) {
        super(context);
        init();
    }

    public CompatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                CompatView.super.computeScroll();
                return true;
            }
        });
    }

    @Override
    public void computeScroll() {
    }

    public static void setOnClickListener(View view, OnClickListener l) {
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) setClickable(v, true);
                return false;
            }
        });
        view.setOnClickListener(l);
        setClickable(view, false);
    }

    protected static void cancelOnClick(View view) {
        Queue<View> queue = new LinkedList<>();
        queue.add(view);
        while (queue.size() > 0) {
            View target = queue.remove();
            if (target instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) target).getChildCount(); i++) {
                    View child = ((ViewGroup) target).getChildAt(i);
                    if (child != null) queue.add(child);
                }
            }
            setClickable(target, false);
        }
    }

    private static void setClickable(View view, boolean clickable) {
        if (view.hasOnClickListeners() && view.isClickable() != clickable)
            view.setClickable(clickable);
    }
}
