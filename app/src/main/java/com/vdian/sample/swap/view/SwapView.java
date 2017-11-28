package com.vdian.sample.swap.view;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;

import com.vdian.uikit.util.RectUtil;
import com.vdian.uikit.view.AnimatorManager;
import com.vdian.uikit.view.TouchController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangliang on 16/12/21.
 */
public class SwapView extends UnRecyclerView implements TouchController.TouchListener {
    private boolean isSwapping = false;
    private boolean doCancel = false;
    private boolean isScaling = false;
    private Rect mTmp;
    private SwapManager mManager;
    private ScrollHelper mHelper;
    private TouchController mDelegate;
    private AnimatorManager yManager;
    private AnimatorManager scaleManager;
    private SwapListener mListener;

    public SwapView(Context context) {
        super(context);
        init();
    }

    public SwapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        RectUtil.vary();
        mTmp = new Rect();
        mManager = new SwapManager();
        mHelper = new ScrollHelper();
        mDelegate = new TouchController(this);
        yManager = new YManager();
        scaleManager = new ScaleManager();
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (isSwapping && !doCancel) {
                    Float line = mManager.line();
                    if (line != null) {
                        int delta = mHelper.scroll(line);
                        if (delta != 0) {
                            scrollBy(0, delta);
                            mManager.swap(delta);
                        }
                    }
                }
                boolean wasScaling = isScaling;
                isScaling = false;
                RecyclerView.Adapter adapter = getAdapter();
                if (adapter != null) {
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if (scaleManager.getValue(getChildAtPosition(i)) != 1) isScaling = true;
                    }
                }
                if (wasScaling || isScaling) invalidate();
                return true;
            }
        });
    }

    @Override
    protected void layoutChild() {
        super.layoutChild();
        doCancel = true;
    }

    public void startSwap(View view) {
        if (isSwapping) return;
        isSwapping = true;
        mManager.start(view);
        mHelper.reset();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mDelegate.dispatchTouchEvent(event);
        mDelegate.onTouchEvent(event);
        if (isSwapping) return true;
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean down(float downX, float downY) {
        isSwapping = false;
        doCancel = false;
        return true;
    }

    @Override
    public boolean move(float moveX, float moveY) {
        if (isSwapping && !doCancel) mManager.swap(moveY);
        return false;
    }

    @Override
    public void up(float velocityX, float velocityY) {
        if (isSwapping && !doCancel) {
            isSwapping = false;
            mManager.end();
        }
    }

    @Override
    public void cancel() {
        up(0, 0);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                View child = getChildAtPosition(i);
                float scale = scaleManager.getValue(child);
                float value = (scale - 1) / 0.25f;
                if (value > 0) {
                    int top, bottom;
                    RectUtil.measure(child, mTmp);
                    top = mTmp.top;
                    bottom = mTmp.bottom;
                    RectUtil.measure(this, mTmp);
                    top -= mTmp.top;
                    bottom -= mTmp.top;
                    top = (int) (top + getScrollY() + child.getPaddingTop() * child.getScaleY());
                    bottom = (int) (bottom + getScrollY() - child.getPaddingBottom() * child.getScaleY());
                    canvas.save();
                    canvas.clipRect(0, top, getWidth(), bottom, Region.Op.DIFFERENCE);
                    ShadowPainter.Shadow shadow = ShadowPainter.Shadow.obtain();
                    shadow.color = Color.argb(51, 136, 136, 136);
                    shadow.width = 1;
                    shadow.height = 1;
                    shadow.top = (int) (2 * getContext().getResources().getDisplayMetrics().density);
                    shadow.bottom = (int) (16 * getContext().getResources().getDisplayMetrics().density);
                    shadow.interpolator = 2;
                    ShadowPainter.drawNP(canvas, shadow, 0, top + (1 - value) * +shadow.top, getWidth(), bottom - top - (1 - value) * (shadow.top + shadow.bottom));
                    shadow.recycle();
                    canvas.restore();
                }
            }
        }
    }

    private class SwapManager {
        private int index = -1;
        private float offset = 0;
        private List<Item> items = new ArrayList<>();
        private int from = -1;
        private int to = -1;

        public void start(View view) {
            int size = getAdapter().getItemCount();
            int position = getChildAdapterPosition(view);
            int type = getAdapter().getItemViewType(position);
            int start = position;
            int end = position;
            while (true) {
                if (start <= 0) break;
                if (getAdapter().getItemViewType(start - 1) == type) start--;
            }
            while (true) {
                if (end >= size - 1) break;
                if (getAdapter().getItemViewType(end + 1) == type) end++;
            }
            index = -1;
            offset = 0;
            items.clear();
            if (position != -1) {
                for (int i = start; i <= end; i++) {
                    View child = getChildAtPosition(i);
                    items.add(new Item(i, (child.getTop() + child.getBottom()) / 2));
                    if (i == position) {
                        index = items.size() - 1;
                        bringChildToFront(child);
                        scaleManager.setTarget(child, 1.25f);
                        offset = yManager.getValue(child);
                    }
                }
            }
            swap(0);
        }

        public Float line() {
            if (index != -1) return items.get(index).center + offset;
            return null;
        }

        public void swap(float delta) {
            if (index != -1) {
                offset += delta;
                Item target = items.get(index);
                from = target.position;
                float center = items.get(index).center + offset;
                if (offset < 0) {
                    to = from;
                    for (int i = index - 1; i >= 0; i--) {
                        Item item = items.get(i);
                        if (center > item.center) break;
                        to = item.position;
                    }
                } else {
                    to = from;
                    for (int i = index + 1; i < items.size(); i++) {
                        Item item = items.get(i);
                        if (center < item.center) break;
                        to = item.position;
                    }
                }
                View view = getChildAtPosition(from);
                yManager.setValue(view, yManager.getValue(view) + delta);
                for (int i = 0; i < items.size(); i++) {
                    int position = items.get(i).position;
                    if (position < from) {
                        if (position < to)
                            yManager.setTarget(getChildAtPosition(position), 0);
                        else
                            yManager.setTarget(getChildAtPosition(position), view.getHeight());
                    }
                    if (position > from) {
                        if (position > to)
                            yManager.setTarget(getChildAtPosition(position), 0);
                        else
                            yManager.setTarget(getChildAtPosition(position), -view.getHeight());
                    }
                }
            } else {
                from = -1;
                to = -1;
            }
        }

        public void end() {
            if (from != -1 && to != -1) {
                View view = getChildAtPosition(from);
                scaleManager.setTarget(view, 1f);
                yManager.setTarget(view, from < to ? getChildAtPosition(to).getBottom() - getChildAtPosition(from).getBottom() : getChildAtPosition(to).getTop() - getChildAtPosition(from).getTop());
                if (from != to) {
                    HashMap<Integer, Float> vY = new HashMap<>();
                    HashMap<Integer, Float> tY = new HashMap<>();
                    HashMap<Integer, Float> vScale = new HashMap<>();
                    HashMap<Integer, Float> tScale = new HashMap<>();
                    for (int i = 0; i < getAdapter().getItemCount(); i++) {
                        int index = i;
                        float swap = 0;
                        if (from < to) {
                            if (index >= from && index <= to) {
                                if (index == from) {
                                    index = to;
                                    swap = getChildAtPosition(from).getBottom() - getChildAtPosition(to).getBottom();
                                } else {
                                    index--;
                                    swap = getChildAtPosition(from).getHeight();
                                }
                            }
                        } else {
                            if (index >= to && index <= from) {
                                if (index == from) {
                                    index = to;
                                    swap = getChildAtPosition(from).getTop() - getChildAtPosition(to).getTop();
                                } else {
                                    index++;
                                    swap = -getChildAtPosition(from).getHeight();
                                }
                            }
                        }
                        View child = getChildAtPosition(i);
                        vY.put(index, yManager.getValue(child) + swap);
                        tY.put(index, yManager.getTarget(child) + swap);
                        vScale.put(index, scaleManager.getValue(child));
                        tScale.put(index, scaleManager.getTarget(child));
                    }
                    if (mListener != null) mListener.swap(from, to);
                    for (int i = 0; i < getAdapter().getItemCount(); i++) {
                        View child = getChildAtPosition(i);
                        if (i == to) bringChildToFront(child);
                        yManager.setValue(child, vY.get(i));
                        yManager.setTarget(child, tY.get(i));
                        scaleManager.setValue(child, vScale.get(i));
                        scaleManager.setTarget(child, tScale.get(i));
                    }
                }
            }
        }

        private class Item {
            private int position;
            private float center;

            private Item(int position, float center) {
                this.position = position;
                this.center = center;
            }
        }
    }

    private class ScrollHelper {
        private int direction = 0;
        private long time = 0;
        private float velocity = 0;

        public void reset() {
            direction = 0;
        }

        public int scroll(float line) {
            int delta = 0;
            float top = getScrollY() + 60 * getResources().getDisplayMetrics().density;
            float bottom = getScrollY() + getHeight() - 60 * getResources().getDisplayMetrics().density;
            if (top > bottom) {
                top = getScrollY() + getHeight() / 2;
                bottom = top;
            }
            long dt = AnimationUtils.currentAnimationTimeMillis() - time;
            time += dt;
            if (line < top) {
                if (direction == -1) {
                    delta = (int) -(getResources().getDisplayMetrics().heightPixels * velocity * (dt / 1000f));
                    if (delta < -getScrollY()) delta = -getScrollY();
                    if (delta >= 0) delta = -1;
                    velocity += (1 / 3f) * (dt / 666f);
                    if (velocity > 1f) velocity = 1f;
                } else {
                    direction = -1;
                    velocity = 1 / 3f;
                }
            } else if (line > bottom) {
                if (direction == 1) {
                    delta = (int) (getResources().getDisplayMetrics().heightPixels * velocity * (dt / 1000f));
                    if (delta > getChildAt(0).getHeight() - getScrollY() - getHeight())
                        delta = getChildAt(0).getHeight() - getScrollY() - getHeight();
                    if (delta <= 0) delta = 1;
                    velocity += (1 / 3f) * (dt / 666f);
                    if (velocity > 1f) velocity = 1f;
                } else {
                    direction = 1;
                    velocity = 1 / 3f;
                }
            } else direction = 0;
            return delta;
        }
    }

    public void setSwapListener(SwapListener listener) {
        mListener = listener;
    }

    public interface SwapListener {
        void swap(int from, int to);
    }

    private static class YManager extends AnimatorManager.YManager {
        @Override
        protected long duration(View view, float from, float to) {
            return 6 * super.duration(view, from, to);
        }
    }

    private static class ScaleManager extends AnimatorManager.ScaleManager {
        @Override
        protected long duration(View view, float from, float to) {
            return 2 * super.duration(view, from, to);
        }

        @Override
        protected TimeInterpolator interpolator(View view, float from, float to) {
            return new AccelerateDecelerateInterpolator();
        }
    }
}
