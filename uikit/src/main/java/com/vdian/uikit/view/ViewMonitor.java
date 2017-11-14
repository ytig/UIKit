package com.vdian.uikit.view;

import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import com.vdian.uikit.util.RectUtil;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhangliang on 16/11/11.
 */
public class ViewMonitor implements View.OnAttachStateChangeListener, ViewTreeObserver.OnPreDrawListener {
    private static boolean doDraw = true; //是否进行绘制

    public static void cancelDraw() {
        doDraw = false;
    }

    public static void globalMonitor(View view, RectListener listener) {
        if (view == null || listener == null) return;
        view.addOnAttachStateChangeListener(new ViewMonitor(true, view, listener));
    }

    public static void localMonitor(View view, RectListener listener) {
        if (view == null || listener == null) return;
        view.addOnAttachStateChangeListener(new ViewMonitor(false, view, listener));
    }

    private boolean isAttached; //是否正在监控
    private boolean isRaw; //是否基于屏幕原点
    private View mView; //监控视图
    private RectListener mListener; //监控数据处理器

    private ViewMonitor(boolean raw, View view, RectListener listener) {
        isAttached = false;
        isRaw = raw;
        mView = view;
        mListener = listener;
        if (ViewCompat.isAttachedToWindow(mView)) onViewAttachedToWindow(null); //主动添加监控
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (isAttached) return;
        isAttached = true;
        mView.getViewTreeObserver().addOnPreDrawListener(this);
        if (mListener instanceof RectListeners) ((RectListeners) mListener).monitor(isAttached);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (!isAttached) return;
        isAttached = false;
        mView.getViewTreeObserver().removeOnPreDrawListener(this);
        if (mListener instanceof RectListeners) ((RectListeners) mListener).monitor(isAttached);
    }

    @Override
    public boolean onPreDraw() {
        doDraw = true;
        RectManager.release(mListener.handle(RectManager.request(isRaw, mView)));
        return doDraw;
    }

    private static class RectManager {
        private static Queue<Rect> cache = new LinkedList<>(); //数据缓存
        private static Rect origin = new Rect(); //父视图内矩阵

        protected static Rect request(boolean raw, View view) {
            View parent = null;
            if (!raw) {
                ViewParent viewParent = view.getParent();
                if (viewParent instanceof View) parent = (View) viewParent;
            }
            if (parent == null) {
                Rect rect = cache.size() > 0 ? cache.remove() : new Rect();
                RectUtil.measure(view, rect);
                return rect;
            } else {
                RectUtil.measure(parent, origin);
                Rect rect = cache.size() > 0 ? cache.remove() : new Rect();
                RectUtil.measure(view, rect);
                rect.set(rect.left - origin.left, rect.top - origin.top, rect.right - origin.left, rect.bottom - origin.top);
                return rect;
            }
        }

        protected static void release(Rect rect) {
            if (rect != null) cache.add(rect);
        }
    }

    public interface RectListener {
        Rect handle(Rect rect);
    }

    public interface RectListeners extends RectListener {
        void monitor(boolean attached);
    }
}
