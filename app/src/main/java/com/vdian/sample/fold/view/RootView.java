package com.vdian.sample.fold.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.vdian.uikit.view.extend.refresh.RefreshCompat;
import com.vdian.uikit.view.extend.refresh.RefreshView;
import com.vdian.uikit.view.helper.TouchController;

/**
 * Created by zhangliang on 17/7/7.
 */
public class RootView extends RefreshView {
    private float[] mXY; //偏移量数组
    private Faker mFaker; //滑动伪造者

    public RootView(Context context) {
        super(context);
        init();
    }

    public RootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mXY = new float[2];
    }

    /**
     * 伪造滑动事件（初始化用）
     *
     * @param horizontal
     */
    public void fake(boolean horizontal) {
        permit(false, false);
        mFaker = new Faker(horizontal);
    }

    /**
     * 是否处于越界状态
     *
     * @return
     */
    public boolean overflow() {
        return !super.fling();
    }

    /**
     * 滚动方向检测
     *
     * @return
     */
    public boolean horizontal() {
        return mFaker != null ? mFaker.horizontal() : RefreshCompat.ViewOrientation.isHorizontal(getNestedChild());
    }

    /**
     * 滚动边缘检测
     *
     * @param top
     * @return
     */
    public boolean edge(boolean top) {
        return mFaker != null ? mFaker.edge(top) : RefreshCompat.ViewEdge.isEdge(top, getNestedChild());
    }

    @Override
    public void down() {
        RootEvent event = RootEvent.Tools.getRootEvent(this);
        if (event != null) event.down(this);
        super.down();
    }

    @Override
    public boolean move(float dx, float dy) {
        mXY[0] = dx;
        mXY[1] = dy;
        RootEvent event = RootEvent.Tools.getRootEvent(this);
        Boolean B = (event == null ? null : event.move(this, mXY));
        boolean b = super.move(mXY[0], mXY[1]);
        return B == null ? b : B;
    }

    @Override
    public void up() {
        super.up();
        RootEvent event = RootEvent.Tools.getRootEvent(this);
        if (event != null) event.up(this);
    }

    @Override
    public boolean fling() {
        RootEvent event = RootEvent.Tools.getRootEvent(this);
        Boolean B = (event == null ? null : event.fling(this));
        boolean b = super.fling();
        return B == null ? b : B;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mFaker != null) {
            Boolean b = mFaker.dispatchTouchEvent(event);
            if (b != null) return b;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mFaker != null) {
            Boolean b = mFaker.onTouchEvent(event);
            if (b != null) return b;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mFaker != null) {
            Boolean b = mFaker.onInterceptTouchEvent(event);
            if (b != null) return b;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (mFaker != null) mFaker.requestDisallowInterceptTouchEvent(disallowIntercept);
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private class Faker extends TouchController {
        private boolean isHorizontal; //伪造滑动方向

        public Faker(boolean horizontal) {
            super(new TouchController.TouchListener() {
                @Override
                public boolean down(float downX, float downY) {
                    RootView.this.down();
                    return true;
                }

                @Override
                public boolean move(float moveX, float moveY) {
                    RootView.this.move(-moveX, -moveY);
                    return true;
                }

                @Override
                public void up(float velocityX, float velocityY) {
                    RootView.this.up();
                }

                @Override
                public void cancel() {
                    up(0, 0);
                }
            }, RootView.this, horizontal, !horizontal);
            isHorizontal = horizontal;
        }

        public boolean horizontal() {
            return isHorizontal;
        }

        public boolean edge(boolean top) {
            return true;
        }
    }

    public interface RootEvent {
        class Tools {
            public static RootEvent getRootEvent(View view) {
                ViewParent parent = (view != null ? view.getParent() : null);
                while (parent != null) {
                    if (parent instanceof RootEvent) return (RootEvent) parent;
                    parent = parent.getParent();
                }
                return null;
            }
        }

        void down(RootView view);

        Boolean move(RootView view, float[] xy);

        void up(RootView view);

        Boolean fling(RootView view);
    }
}
