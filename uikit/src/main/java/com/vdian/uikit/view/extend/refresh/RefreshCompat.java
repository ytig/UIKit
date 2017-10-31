package com.vdian.uikit.view.extend.refresh;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.vdian.uikit.view.helper.ViewGroupMonitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.WeakHashMap;

/**
 * Created by zhangliang on 16/12/8.
 */
public class RefreshCompat {
    /**
     * 数据变化通知(自动更新视图)
     */
    public static class AdapterNotify {
        public interface Event {
            void notifyData();
        }

        public static boolean notifyData(View view) {
            if (view instanceof Event) return notifyData((Event) view);
            if (view instanceof RecyclerView) return notifyData((RecyclerView) view);
            if (view instanceof AdapterView) return notifyData((AdapterView) view);
            return false;
        }

        private static boolean notifyData(Event event) {
            event.notifyData();
            return true;
        }

        private static boolean notifyData(RecyclerView view) {
            RecyclerView.Adapter adapter = view.getAdapter();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                return true;
            }
            return false;
        }

        private static boolean notifyData(AdapterView view) {
            Adapter adapter = view.getAdapter();
            if (adapter instanceof BaseAdapter) {
                ((BaseAdapter) adapter).notifyDataSetChanged();
                return true;
            }
            return false;
        }
    }

    /**
     * 接近底部检测(触发自动加载)
     */
    public static class ViewBottom {
        public interface Event {
            boolean isBottom(int advance);
        }

        public static boolean isBottom(int advance, View view) {
            if (view instanceof Event) return isBottom(advance, (Event) view);
            if (view instanceof RecyclerView) return isBottom(advance, (RecyclerView) view);
            if (view instanceof AdapterView) return isBottom(advance, (AdapterView) view);
            return false;
        }

        private static boolean isBottom(int advance, Event event) {
            return event.isBottom(advance);
        }

        private static boolean isBottom(int advance, RecyclerView view) {
            RecyclerView.Adapter adapter = view.getAdapter();
            if (adapter != null) {
                int limit = adapter.getItemCount() - 1 - advance;
                if (-1 >= limit) return true;
                for (int i = view.getChildCount() - 1; i >= 0; i--) {
                    View child = view.getChildAt(i);
                    if (child == null) continue;
                    if (view.getChildAdapterPosition(child) >= limit) return true;
                }
            }
            return false;
        }

        private static boolean isBottom(int advance, AdapterView view) {
            Adapter adapter = view.getAdapter();
            if (adapter != null) {
                int limit = adapter.getCount() - 1 - advance;
                if (-1 >= limit) return true;
                for (int i = view.getChildCount() - 1; i >= 0; i--) {
                    View child = view.getChildAt(i);
                    if (child == null) continue;
                    if (view.getPositionForView(child) >= limit) return true;
                }
            }
            return false;
        }
    }

    /**
     * 控件触控代理(越界触控转移)
     */
    public static class ViewDelegate {
        public interface Event {
            boolean delegateTouch(MotionEvent event);
        }

        public static boolean delegateTouch(MotionEvent event, View view) {
            if (view instanceof Event) return delegateTouch(event, (Event) view);
            if (view instanceof RecyclerView) return delegateTouch(event, (RecyclerView) view);
            if (view instanceof AdapterView) return delegateTouch(event, (AdapterView) view);
            return false;
        }

        private static boolean delegateTouch(MotionEvent ev, Event event) {
            return event.delegateTouch(ev);
        }

        private static boolean delegateTouch(MotionEvent event, RecyclerView view) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                event.offsetLocation(((View) parent).getScrollX() - view.getLeft() - view.getTranslationX(), ((View) parent).getScrollY() - view.getTop() - view.getTranslationY());
                return view.dispatchTouchEvent(event);
            }
            return false;
        }

        private static boolean delegateTouch(MotionEvent event, AdapterView view) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                event.offsetLocation(((View) parent).getScrollX() - view.getLeft() - view.getTranslationX(), ((View) parent).getScrollY() - view.getTop() - view.getTranslationY());
                return view.dispatchTouchEvent(event);
            }
            return false;
        }
    }

    /**
     * 滚动边缘检测(回弹初始判断)
     */
    public static class ViewEdge {
        public interface Event {
            boolean isEdge(boolean isTop);
        }

        public static boolean isEdge(boolean isTop, View view) {
            if (view instanceof Event) return isEdge(isTop, (Event) view);
            if (view instanceof RecyclerView) return isEdge(isTop, (RecyclerView) view);
            if (view instanceof AdapterView) return isEdge(isTop, (AdapterView) view);
            return false;
        }

        private static boolean isEdge(boolean isTop, Event event) {
            return event.isEdge(isTop);
        }

        private static boolean isEdge(boolean isTop, RecyclerView view) {
            boolean isHorizontal = ViewOrientation.isHorizontal((View) view);
            return isHorizontal ? !view.canScrollHorizontally(isTop ? -1 : 1) : !view.canScrollVertically(isTop ? -1 : 1);
        }

        private static boolean isEdge(boolean isTop, AdapterView view) {
            boolean isHorizontal = ViewOrientation.isHorizontal((View) view);
            return isHorizontal ? !view.canScrollHorizontally(isTop ? -1 : 1) : !view.canScrollVertically(isTop ? -1 : 1);
        }
    }

    /**
     * 控件滚动监控(边缘滑动转移)
     */
    public static class ViewMonitor {
        public interface Event {
            boolean registerMonitor(OnScrollListener listener);
        }

        public interface OnScrollListener {
            void onStartScroll();

            void onStopScroll(long timePass, float velocityX, float velocityY);
        }

        public static boolean registerMonitor(OnScrollListener listener, View view) {
            if (view instanceof Event) return registerMonitor(listener, (Event) view);
            if (view instanceof RecyclerView) return registerMonitor(listener, (RecyclerView) view);
            if (view instanceof AdapterView) return registerMonitor(listener, (AdapterView) view);
            return view != null;
        }

        private static boolean registerMonitor(OnScrollListener listener, Event event) {
            return event.registerMonitor(listener);
        }

        private static boolean registerMonitor(OnScrollListener listener, RecyclerView view) {
            new ScrollMonitor(view, listener);
            return true;
        }

        private static boolean registerMonitor(OnScrollListener listener, AdapterView view) {
            new ScrollMonitor(view, listener);
            return true;
        }

        private static class ScrollMonitor implements ViewGroupMonitor.ChildListener {
            private boolean running; //正在变换中
            private Statistics statistics; //数据统计
            private ViewGroup parent; //监控视图
            private OnScrollListener listener; //监控回调

            public ScrollMonitor(ViewGroup parent, OnScrollListener listener) {
                this.running = false;
                this.statistics = new Statistics(parent.getContext());
                this.parent = parent;
                this.listener = listener;
                ViewGroupMonitor.localMonitor(this.parent, this);
            }

            @Override
            public List<ViewGroupMonitor.Child> handle(List<ViewGroupMonitor.Child> children) {
                if (statistics.exchange(AnimationUtils.currentAnimationTimeMillis(), children)) {
                    boolean changed = statistics.changed();
                    if (!running) {
                        if (changed) {
                            running = true;
                            listener.onStartScroll();
                        }
                    }
                    if (running) {
                        boolean edge = ViewEdge.isEdge(true, parent) || ViewEdge.isEdge(false, parent);
                        if (edge || !changed) {
                            long passTime = 0;
                            float velocityX = 0, velocityY = 0;
                            if (edge) {
                                Statistics.Result result = statistics.calculate();
                                passTime = result.passTime;
                                velocityX = result.velocityX;
                                velocityY = result.velocityY;
                                result.recycle();
                            }
                            statistics.clear();
                            running = false;
                            listener.onStopScroll(passTime, velocityX, velocityY);
                        }
                    }
                }
                if (running) parent.postInvalidate();
                return children;
            }

            private static class Statistics {
                private float ray; //极限速度
                private History news; //最新历史记录
                private List<ViewGroupMonitor.Child> trashes; //回收站

                public Statistics(Context context) {
                    ray = 10 * context.getResources().getDisplayMetrics().density;
                    news = new History();
                    History tmp = news;
                    for (int i = 0; i < 2; i++) {
                        History history = new History();
                        history.post = tmp;
                        tmp.pre = history;
                        tmp = history;
                    }
                    news.post = tmp;
                    tmp.pre = news;
                    trashes = new ArrayList<>();
                }

                public boolean exchange(long time, List<ViewGroupMonitor.Child> children) {
                    if (time > news.time) {
                        news = news.post;
                        news.time = time;
                        trashes.addAll(news.children);
                        news.children.clear();
                        news.children.addAll(children);
                        children.clear();
                        children.addAll(trashes);
                        trashes.clear();
                        return true;
                    }
                    return false;
                }

                public boolean changed() {
                    return news.changed(news.pre);
                }

                public Result calculate() {
                    Result result = Result.obtain();
                    result.passTime = 0;
                    result.velocityX = 0;
                    result.velocityY = 0;
                    if (news.time > 0 && news.pre.time > 0 && news.pre.pre.time > 0) {
                        Result r1 = news.calculate(news.pre);
                        Result r2 = news.pre.calculate(news.pre.pre);
                        if (r1.velocityX * r2.velocityX > 0 && r2.velocityX / r1.velocityX >= Math.min(Math.abs(r1.velocityX / ray), 1f)) {
                            result.velocityX = (r1.velocityX + r2.velocityX) / 2;
                            result.passTime = (long) ((news.time - news.pre.time) * (1 - (r1.velocityX / result.velocityX)));
                        }
                        if (r1.velocityY * r2.velocityY > 0 && r2.velocityY / r1.velocityY >= Math.min(Math.abs(r1.velocityY / ray), 1f)) {
                            result.velocityY = (r1.velocityY + r2.velocityY) / 2;
                            result.passTime = (long) ((news.time - news.pre.time) * (1 - (r1.velocityY / result.velocityY)));
                        }
                        r1.recycle();
                        r2.recycle();
                    }
                    return result;
                }

                public void clear() {
                    History cursor = news;
                    do {
                        cursor.time = 0;
                        cursor = cursor.pre;
                    } while (cursor != news);
                }

                private static class History {
                    public long time = 0; //统计时间
                    public List<ViewGroupMonitor.Child> children = new ArrayList<>(); //位置数据
                    public History pre; //更早的历史
                    public History post; //更新的历史

                    public boolean changed(History history) {
                        if (children.size() != history.children.size()) return true;
                        for (int i = 0; i < children.size(); i++) {
                            ViewGroupMonitor.Child child1 = children.get(i);
                            ViewGroupMonitor.Child child2 = history.children.get(i);
                            if (child1 != null || child2 != null) {
                                if (child1 == null || child2 == null) return true;
                                else {
                                    if (!child1.equals(child2)) return true;
                                }
                            }
                        }
                        return false;
                    }

                    public Result calculate(History history) {
                        Result result = Result.obtain();
                        result.passTime = 0;
                        result.velocityX = 0;
                        result.velocityY = 0;
                        int coin = 0;
                        long dt = time - history.time;
                        for (ViewGroupMonitor.Child child1 : children) {
                            if (child1 == null) continue;
                            if (child1.rect == null) continue;
                            if (child1.position < 0) continue;
                            for (ViewGroupMonitor.Child child2 : history.children) {
                                if (child2 == null) continue;
                                if (child2.rect == null) continue;
                                if (child1.position == child2.position) {
                                    float vx = ((float) (child1.rect.left - child2.rect.left)) / dt;
                                    float vy = ((float) (child1.rect.top - child2.rect.top)) / dt;
                                    coin++;
                                    result.velocityX = (result.velocityX * (coin - 1) / coin) + (vx / coin);
                                    result.velocityY = (result.velocityY * (coin - 1) / coin) + (vy / coin);
                                }
                            }
                        }
                        return result;
                    }
                }

                public static class Result {
                    private static Queue<Result> cache = new LinkedList<>(); //数据缓存

                    public long passTime; //停止历时
                    public float velocityX; //横向速度
                    public float velocityY; //纵向速度

                    public static Result obtain() {
                        return cache.size() > 0 ? cache.remove() : new Result();
                    }

                    public void recycle() {
                        cache.add(this);
                    }
                }
            }
        }
    }

    /**
     * 滚动方向检测(自动识别类型)
     */
    public static class ViewOrientation {
        public interface Event {
            boolean isHorizontal();
        }

        public static boolean isHorizontal(View view) {
            if (view instanceof Event) isHorizontal((Event) view);
            if (view instanceof RecyclerView) return isHorizontal((RecyclerView) view);
            if (view instanceof AdapterView) return isHorizontal((AdapterView) view);
            return false;
        }

        private static boolean isHorizontal(Event event) {
            return event.isHorizontal();
        }

        private static boolean isHorizontal(RecyclerView view) {
            RecyclerView.LayoutManager manager = view.getLayoutManager();
            if (manager instanceof LinearLayoutManager)
                return ((LinearLayoutManager) manager).getOrientation() == OrientationHelper.HORIZONTAL;
            else {
                if (manager instanceof StaggeredGridLayoutManager)
                    return ((StaggeredGridLayoutManager) manager).getOrientation() == OrientationHelper.HORIZONTAL;
            }
            return false;
        }

        private static boolean isHorizontal(AdapterView view) {
            return false;
        }
    }

    /**
     * 控件位置恢复(触控突变修复)
     */
    public static class ViewReform {
        private static Rect rect = new Rect(); //临时位置
        private static WeakHashMap<Object, Object> map = new WeakHashMap<>(); //临时存储区

        public interface Event {
            Object saveLocation(float delta);

            void loadLocation(Object location);
        }

        public static boolean saveLocation(float delta, View view) {
            if (view instanceof Event) return saveLocation(delta, (Event) view);
            if (view instanceof RecyclerView) return saveLocation(delta, (RecyclerView) view);
            if (view instanceof AdapterView) return saveLocation(delta, (AdapterView) view);
            return false;
        }

        private static boolean saveLocation(float delta, Event event) {
            Object location = event.saveLocation(delta);
            if (location != null) {
                map.put(event, location);
                return true;
            } else {
                map.remove(event);
                return false;
            }
        }

        private static boolean saveLocation(float delta, RecyclerView view) {
            View child = view.getChildAt(0);
            if (child != null) {
                view.getDecoratedBoundsWithMargins(child, rect);
                boolean isHorizontal = ViewOrientation.isHorizontal((View) view);
                Location location = Location.obtain();
                location.position = view.getChildAdapterPosition(child);
                location.offset = isHorizontal ? (rect.left - view.getPaddingLeft() - (int) delta) : (rect.top - view.getPaddingTop() - (int) delta);
                map.put(view, location);
                return true;
            } else {
                map.remove(view);
                return false;
            }
        }

        private static boolean saveLocation(float delta, AdapterView view) {
            View child = view.getChildAt(0);
            if (child != null) {
                boolean isHorizontal = ViewOrientation.isHorizontal((View) view);
                Location location = Location.obtain();
                location.position = view.getPositionForView(child);
                location.offset = isHorizontal ? (child.getLeft() - view.getPaddingLeft() - (int) delta) : (child.getTop() - view.getPaddingTop() - (int) delta);
                map.put(view, location);
                return true;
            } else {
                map.remove(view);
                return false;
            }
        }

        public static boolean loadLocation(View view) {
            if (view instanceof Event) return loadLocation((Event) view);
            if (view instanceof RecyclerView) return loadLocation((RecyclerView) view);
            if (view instanceof AdapterView) return loadLocation((AdapterView) view);
            return false;
        }

        private static boolean loadLocation(Event event) {
            Object location = map.remove(event);
            if (location != null) {
                event.loadLocation(location);
                return true;
            }
            return false;
        }

        private static boolean loadLocation(RecyclerView view) {
            Object location = map.remove(view);
            if (location instanceof Location) {
                int position = ((Location) location).position;
                int offset = ((Location) location).offset;
                ((Location) location).recycle();
                if (position != RecyclerView.NO_POSITION) {
                    RecyclerView.LayoutManager manager = view.getLayoutManager();
                    if (manager instanceof LinearLayoutManager)
                        ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, offset);
                    else {
                        if (manager instanceof StaggeredGridLayoutManager)
                            ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position, offset);
                    }
                }
                return true;
            }
            return false;
        }

        private static boolean loadLocation(AdapterView view) {
            Object location = map.remove(view);
            if (location instanceof Location) {
                int position = ((Location) location).position;
                int offset = ((Location) location).offset;
                ((Location) location).recycle();
                if (position != AdapterView.INVALID_POSITION) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        if (view instanceof AbsListView)
                            ((AbsListView) view).setSelectionFromTop(position, offset);
                    } else {
                        if (view instanceof ListView)
                            ((ListView) view).setSelectionFromTop(position, offset);
                    }
                }
                return true;
            }
            return false;
        }

        private static class Location {
            private static Queue<Location> cache = new LinkedList<>(); //数据缓存

            public int position; //滚动位置
            public int offset; //滚动偏移

            public static Location obtain() {
                return cache.size() > 0 ? cache.remove() : new Location();
            }

            public void recycle() {
                cache.add(this);
            }
        }
    }

    /**
     * 控件内容测量(底部控件定位)
     */
    public static class ViewVisibility {
        private static Rect rect = new Rect(); //临时位置

        public interface Event {
            int measureVisibility();
        }

        public static int measureVisibility(View view) {
            if (view instanceof Event) return measureVisibility((Event) view);
            if (view instanceof RecyclerView) return measureVisibility((RecyclerView) view);
            if (view instanceof AdapterView) return measureVisibility((AdapterView) view);
            return 0;
        }

        private static int measureVisibility(Event event) {
            return event.measureVisibility();
        }

        private static int measureVisibility(RecyclerView view) {
            boolean isHorizontal = ViewOrientation.isHorizontal((View) view);
            int mHeight = isHorizontal ? (view.getWidth() - view.getPaddingLeft() - view.getPaddingRight()) : (view.getHeight() - view.getPaddingTop() - view.getPaddingBottom());
            int cBottom = 0;
            for (int i = view.getChildCount() - 1; i >= 0; i--) {
                View child = view.getChildAt(i);
                if (child == null) continue;
                view.getDecoratedBoundsWithMargins(child, rect);
                int bottom = isHorizontal ? (rect.right - view.getPaddingLeft()) : (rect.bottom - view.getPaddingTop());
                cBottom = Math.max(cBottom, bottom);
            }
            return Math.min(mHeight, cBottom);
        }

        private static int measureVisibility(AdapterView view) {
            boolean isHorizontal = ViewOrientation.isHorizontal((View) view);
            int mHeight = isHorizontal ? (view.getWidth() - view.getPaddingLeft() - view.getPaddingRight()) : (view.getHeight() - view.getPaddingTop() - view.getPaddingBottom());
            int cBottom = 0;
            for (int i = view.getChildCount() - 1; i >= 0; i--) {
                View child = view.getChildAt(i);
                if (child == null) continue;
                int bottom = isHorizontal ? (child.getRight() - view.getPaddingLeft()) : (child.getBottom() - view.getPaddingTop());
                cBottom = Math.max(cBottom, bottom);
            }
            return Math.min(mHeight, cBottom);
        }
    }
}
