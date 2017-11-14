package com.vdian.uikit.view;

import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;

import com.vdian.uikit.util.RectUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by zhangliang on 16/11/10.
 */
public class ViewGroupMonitor implements View.OnAttachStateChangeListener, ViewTreeObserver.OnPreDrawListener {
    private static boolean doDraw = true; //是否进行绘制

    public static void cancelDraw() {
        doDraw = false;
    }

    public static void globalMonitor(ViewGroup parent, ChildListener listener) {
        if (parent == null || listener == null) return;
        parent.addOnAttachStateChangeListener(new ViewGroupMonitor(true, parent, listener));
    }

    public static void localMonitor(ViewGroup parent, ChildListener listener) {
        if (parent == null || listener == null) return;
        parent.addOnAttachStateChangeListener(new ViewGroupMonitor(false, parent, listener));
    }

    private boolean isAttached; //是否正在监控
    private boolean isRaw; //是否基于屏幕原点
    private ViewGroup mParent; //监控视图
    private ChildListener mListener; //监控数据处理器

    private ViewGroupMonitor(boolean raw, ViewGroup parent, ChildListener listener) {
        isAttached = false;
        isRaw = raw;
        mParent = parent;
        mListener = listener;
        if (ViewCompat.isAttachedToWindow(mParent)) onViewAttachedToWindow(null); //主动添加监控
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        if (isAttached) return;
        isAttached = true;
        mParent.getViewTreeObserver().addOnPreDrawListener(this);
        if (mListener instanceof ChildListeners) ((ChildListeners) mListener).monitor(isAttached);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (!isAttached) return;
        isAttached = false;
        mParent.getViewTreeObserver().removeOnPreDrawListener(this);
        if (mListener instanceof ChildListeners) ((ChildListeners) mListener).monitor(isAttached);
    }

    @Override
    public boolean onPreDraw() {
        doDraw = true;
        Child.release(mListener.handle(Child.request(isRaw, mParent)));
        return doDraw;
    }

    public static class Child implements Comparable<Child> {
        private static Queue<List<Child>> list = new LinkedList<>(); //列表缓存
        private static Queue<Child> cell = new LinkedList<>(); //单元格缓存
        private static Rect origin = new Rect(); //父视图内矩阵

        protected static List<Child> request(boolean raw, ViewGroup parent) {
            if (raw) {
                List<Child> children = list.size() > 0 ? list.remove() : new ArrayList<Child>();
                for (int i = 0; i < parent.getChildCount(); i++) {
                    Child child = cell.size() > 0 ? cell.remove() : new Child();
                    child.index = i;
                    child.position = position(parent, i);
                    RectUtil.measure(parent.getChildAt(i), child.rect);
                    children.add(child);
                }
                return children;
            } else {
                RectUtil.measure(parent, origin);
                List<Child> children = list.size() > 0 ? list.remove() : new ArrayList<Child>();
                for (int i = 0; i < parent.getChildCount(); i++) {
                    Child child = cell.size() > 0 ? cell.remove() : new Child();
                    child.index = i;
                    child.position = position(parent, i);
                    RectUtil.measure(parent.getChildAt(i), child.rect);
                    child.rect.set(child.rect.left - origin.left, child.rect.top - origin.top, child.rect.right - origin.left, child.rect.bottom - origin.top);
                    children.add(child);
                }
                return children;
            }
        }

        protected static void release(List<Child> children) {
            if (children != null) {
                cell.addAll(children);
                children.clear();
                list.add(children);
            }
        }

        private static int position(ViewGroup parent, int index) {
            if (parent instanceof AdapterView)
                return ((AdapterView) parent).getPositionForView(parent.getChildAt(index));
            if (parent instanceof RecyclerView)
                return ((RecyclerView) parent).getChildAdapterPosition(parent.getChildAt(index));
            return index;
        }

        public int index = -1; //控件布局序号
        public int position = -1; //控件数据序号
        public Rect rect = new Rect(); //控件位置信息

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Child) {
                if (index != ((Child) obj).index) return false;
                if (position != ((Child) obj).position) return false;
                if (rect == null || ((Child) obj).rect == null) {
                    if (rect != null || ((Child) obj).rect != null) return false;
                } else {
                    if (!rect.equals(((Child) obj).rect)) return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(Child o) {
            if (position != o.position) return position - o.position;
            return index - o.index;
        }
    }

    public interface ChildListener {
        List<Child> handle(List<Child> children);
    }

    public interface ChildListeners extends ChildListener {
        void monitor(boolean attached);
    }
}
