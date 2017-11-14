package com.vdian.wrapper.recycler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vdian.uikit.view.TouchController;
import com.vdian.uikit.view.TransitionController;
import com.vdian.uikit.view.ViewMonitor;

/**
 * Created by zhangliang on 16/11/2.
 */
public abstract class SwipeWrapper extends ReplaceWrapper {
    public SwipeWrapper(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (recyclerView != null)
            recyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
    }

    @Override
    protected ViewGroup onReplaceViewGroup(ViewGroup parent, int viewType) {
        SwipeView swipe = new SwipeView(parent.getContext());
        swipe.addMenu(onCreateMenu(swipe, viewType));
        return swipe;
    }

    protected abstract View onCreateMenu(ViewGroup parent, int viewType);

    protected static class SwipeView extends ViewGroup implements ReplaceEvent, TouchController.TouchListener, TransitionController.TransitionListener, View.OnTouchListener, ViewMonitor.RectListener {
        protected ViewGroup mLeft; //左侧容器
        protected ViewGroup mRight; //右侧容器
        protected boolean mCancel; //中断事件
        protected boolean mConsume; //遮挡事件
        protected float mLimit; //速度检测门限
        protected TouchController mDelegate; //触控代理类
        protected TransitionController mAnimation; //动画处理类
        protected Rect mRect; //位置信息

        public SwipeView(Context context) {
            super(context);
            init();
        }

        protected void init() {
            setClickable(true); //接受onTouchEvent后续事件
            mRight = new RelativeLayout(getContext());
            addView(mRight);
            mLeft = new RelativeLayout(getContext());
            mLeft.setBackgroundColor(Color.TRANSPARENT); //消除默认背景色
            mLeft.setOnTouchListener(this); //注册遮挡事件
            addView(mLeft);
            mCancel = true;
            mConsume = false;
            mLimit = 0.4f * getContext().getResources().getDisplayMetrics().density;
            mDelegate = new TouchController(this, this, true, false);
            mAnimation = new TransitionController(this, this);
            mRect = new Rect();
            ViewMonitor.globalMonitor(this, this);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            mLeft.measure(widthMeasureSpec, heightMeasureSpec);
            mRight.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(mLeft.getMeasuredHeight(), MeasureSpec.EXACTLY));
            setMeasuredDimension(mLeft.getMeasuredWidth(), mLeft.getMeasuredHeight());
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            mLeft.layout(0, 0, mLeft.getMeasuredWidth(), mLeft.getMeasuredHeight());
            mRight.layout(getWidth() - mRight.getMeasuredWidth(), 0, getWidth(), mRight.getMeasuredHeight());
        }

        protected void reset(boolean smooth) {
            mCancel = true; //中断事件
            if (smooth) mAnimation.setTarget(0); //滚动收拢
            else mAnimation.setValue(0); //强制收拢
        }

        public void addMenu(View view) {
            if (view != null) mRight.addView(view); //添加右侧控件
        }

        @Override
        public void create(View view) {
            if (view != null) mLeft.addView(view); //添加左侧控件
        }

        @Override
        public void bind() {
            reset(false);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            Boolean b = mDelegate.dispatchTouchEvent(event);
            if (b != null) return b;
            return super.dispatchTouchEvent(event);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Boolean b = mDelegate.onTouchEvent(event);
            if (b != null) return b;
            return super.onTouchEvent(event);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            Boolean b = mDelegate.onInterceptTouchEvent(event);
            if (b != null) return b;
            return super.onInterceptTouchEvent(event);
        }

        @Override
        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            mDelegate.requestDisallowInterceptTouchEvent(disallowIntercept);
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }

        @Override
        public boolean down(float downX, float downY) {
            mCancel = false;
            mAnimation.setValue(mAnimation.getValue()); //停止动画
            return true;
        }

        @Override
        public boolean move(float moveX, float moveY) {
            if (mCancel) return false;
            float value = toValue(toCoordinate(mAnimation.getValue()) - moveX);
            if (value < 0) value = 0;
            if (value > 1) value = 1;
            mAnimation.setValue(value);
            return true;
        }

        @Override
        public void up(float velocityX, float velocityY) {
            if (mCancel) return;
            if (mAnimation.getValue() < 0.5f) {
                if (velocityX < -mLimit) mAnimation.setTarget(1);
                else mAnimation.setTarget(0);
            } else {
                if (velocityX > mLimit) mAnimation.setTarget(0);
                else mAnimation.setTarget(1);
            }
        }

        @Override
        public void cancel() {
            up(0, 0);
        }

        @Override
        public void computeScroll() {
            super.computeScroll();
            mAnimation.computeScroll();
        }

        @Override
        public float speed(float value, float target) {
            return 1 / 250f;
        }

        @Override
        public void display(View self, float value) {
            mLeft.scrollTo((int) toCoordinate(value), 0);
        }

        protected float toCoordinate(float value) {
            return value * mRight.getWidth();
        }

        protected float toValue(float coordinate) {
            return coordinate / mRight.getWidth();
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                mConsume = (mAnimation.getValue() != 1);
            return mConsume;
        }

        @Override
        public Rect handle(Rect rect) {
            if (!mRect.equals(rect)) reset(true);
            mRect.set(rect);
            return rect;
        }
    }
}
