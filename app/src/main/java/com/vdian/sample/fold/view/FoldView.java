package com.vdian.sample.fold.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.vdian.uikit.view.helper.TransitionController;

/**
 * Created by zhangliang on 17/7/7.
 */
public class FoldView extends RelativeLayout implements RootView.RootEvent, TransitionController.TransitionListener {
    public static final int VERTICAL = 1; //纵向折叠
    public static final int HORIZONTAL = 2; //横向折叠

    private boolean mDidLayout; //布局完成
    private int mRootCount; //落点计数
    private Judge mJudge; //触控判定者
    private TransitionController mAnimation; //动画处理类
    private int mFoldOrientation; //折叠方向
    private float mFoldRange; //折叠范围
    private FoldListener mListener; //折叠实现器

    public FoldView(Context context) {
        super(context);
        init();
    }

    public FoldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FoldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDidLayout = false;
        mRootCount = 0;
        mJudge = new Judge();
        mAnimation = new TransitionController(this, this, 1);
    }

    /**
     * 设置折叠方向
     *
     * @param orientation
     */
    public void setFoldOrientation(int orientation) {
        mFoldOrientation = orientation;
        mAnimation.initDisplay();
    }

    /**
     * 获取折叠方向
     *
     * @return
     */
    public int getFoldOrientation() {
        return mFoldOrientation;
    }

    /**
     * 设置折叠范围
     *
     * @param range
     */
    public void setFoldRange(float range) {
        mFoldRange = range;
        mAnimation.initDisplay();
    }

    /**
     * 获取折叠范围
     *
     * @return
     */
    public float getFoldRange() {
        return mFoldRange;
    }

    /**
     * 展开
     *
     * @param anim
     */
    public void doExpand(boolean anim) {
        if (mRootCount == 0) {
            if (anim) mAnimation.setTarget(1);
            else mAnimation.setValue(1);
        }
    }

    /**
     * 判断是否展开
     *
     * @return
     */
    public boolean isExpand() {
        return mAnimation.getValue() == 1;
    }

    /**
     * 收拢
     *
     * @param anim
     */
    public void doCollapse(boolean anim) {
        if (mRootCount == 0) {
            if (anim) mAnimation.setTarget(0);
            else mAnimation.setValue(0);
        }
    }

    /**
     * 判断是否收拢
     *
     * @return
     */
    public boolean isCollapse() {
        return mAnimation.getValue() == 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mDidLayout = true;
        mAnimation.initDisplay();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mJudge.dispatch(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void down(RootView view) {
        RootView.RootEvent event = RootView.RootEvent.Tools.getRootEvent(this);
        if (event != null) event.down(view);
        if (mRootCount == 0) {
            mJudge.reset();
            if (mListener != null && mListener.down(view))
                mAnimation.setValue(mAnimation.getValue());
        }
        mRootCount++;
    }

    @Override
    public Boolean move(RootView view, float[] xy) {
        Boolean B = null, b = null;
        float value = mAnimation.getValue();
        boolean owe = value == 0;
        if (owe) {
            RootView.RootEvent event = RootView.RootEvent.Tools.getRootEvent(this);
            if (event != null) B = event.move(view, xy);
        }
        if (mListener != null && mListener.move(view, xy)) {
            if (mFoldOrientation > 0 && mFoldRange > 0) {
                float delta = xy[mFoldOrientation != HORIZONTAL ? 1 : 0];
                if ((value != 0 || delta < 0) && (value != 1 || delta > 0)) {
                    delta = mJudge.intercept() ? 0 : delta;
                    float target = value - delta / mFoldRange;
                    if (target < 0) {
                        target = 0;
                        delta -= value * mFoldRange;
                        if (delta < 0) delta = 0;
                    } else if (target > 1) {
                        target = 1;
                        delta += (1 - value) * mFoldRange;
                        if (delta > 0) delta = 0;
                    } else delta = 0;
                    mAnimation.setValue(target);
                    b = Boolean.TRUE;
                }
                xy[mFoldOrientation != HORIZONTAL ? 1 : 0] = delta;
            }
        }
        if (!owe) {
            RootView.RootEvent event = RootView.RootEvent.Tools.getRootEvent(this);
            if (event != null) B = event.move(view, xy);
        }
        return b == null ? B : b;
    }

    @Override
    public void up(RootView view) {
        mRootCount--;
        if (mRootCount == 0) {
            if (mListener != null && mListener.up(view)) {
                float value = mAnimation.getValue();
                if (value != 0 && value != 1) {
                    if (value < 0.5f) {
                        if (mJudge.fling(true)) mAnimation.setTarget(1);
                        else mAnimation.setTarget(0);
                    } else {
                        if (mJudge.fling(false)) mAnimation.setTarget(0);
                        else mAnimation.setTarget(1);
                    }
                }
            }
        }
        RootView.RootEvent event = RootView.RootEvent.Tools.getRootEvent(this);
        if (event != null) event.up(view);
    }

    @Override
    public Boolean fling(RootView view) {
        if (mListener != null && !mListener.fling(view)) return Boolean.FALSE;
        RootView.RootEvent event = RootView.RootEvent.Tools.getRootEvent(this);
        return event == null ? null : event.fling(view);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mAnimation.computeScroll();
    }

    @Override
    public float speed(float value, float target) {
        return mListener != null ? mListener.speed(value, target) : 0;
    }

    @Override
    public void display(View self, float value) {
        mJudge.update();
        if (mDidLayout && mFoldOrientation > 0 && mFoldRange > 0) {
            if (mListener != null) mListener.display(FoldView.this, value);
        }
    }

    private class Judge {
        private boolean doIntercept; //拦截事件
        private float downCoordinate; //落下位置
        private float distanceLimit; //距离门限
        private long moveTime; //移动时间
        private float moveValue; //移动位置
        private float velocityLimit; //速度门限

        public Judge() {
            doIntercept = false;
            downCoordinate = 0;
            distanceLimit = 4 * getContext().getResources().getDisplayMetrics().density;
            moveTime = Long.MAX_VALUE;
            moveValue = 0;
            velocityLimit = 0.2f * getContext().getResources().getDisplayMetrics().density;
        }

        public void dispatch(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float coordinate = mFoldOrientation != HORIZONTAL ? event.getRawY() : event.getRawX();
                doIntercept = true;
                downCoordinate = coordinate;
            } else {
                if (doIntercept) {
                    float coordinate = mFoldOrientation != HORIZONTAL ? event.getRawY() : event.getRawX();
                    doIntercept = Math.abs(coordinate - downCoordinate) < distanceLimit;
                }
            }
        }

        public boolean intercept() {
            return doIntercept;
        }

        public void reset() {
            float value = mAnimation.getValue();
            moveTime = Long.MAX_VALUE;
            moveValue = value;
        }

        public void update() {
            float value = mAnimation.getValue();
            if (value == 0 || value == 1) {
                moveTime = Long.MAX_VALUE;
                moveValue = value;
            } else {
                if (moveTime == Long.MAX_VALUE) {
                    if (value != moveValue) moveTime = AnimationUtils.currentAnimationTimeMillis();
                }
            }
        }

        public boolean fling(boolean positive) {
            float value = mAnimation.getValue();
            long time = AnimationUtils.currentAnimationTimeMillis() - moveTime;
            float velocity = time <= 0 ? 0 : ((value - moveValue) * mFoldRange / time);
            return positive ? (velocity > velocityLimit) : (velocity < -velocityLimit);
        }
    }

    /**
     * 设置折叠实现器
     *
     * @param listener
     */
    public void setFoldListener(FoldListener listener) {
        mListener = listener;
        mAnimation.initDisplay();
    }

    public interface FoldListener {
        class Tools {
            public abstract static class BaseSetBatch implements OnAttachStateChangeListener, ViewTreeObserver.OnGlobalLayoutListener, FoldListener {
                protected FoldView mFold;
                private boolean isAttached;
                private boolean mTopExpand;
                private boolean mAutoFold;
                private boolean mStopFling;

                public BaseSetBatch(FoldView fold) {
                    mFold = fold;
                    isAttached = false;
                    mTopExpand = true;
                    mAutoFold = true;
                    mStopFling = true;
                    mFold.setFoldListener(this);
                    mFold.addOnAttachStateChangeListener(this);
                    if (ViewCompat.isAttachedToWindow(mFold)) onViewAttachedToWindow(mFold);
                }

                public BaseSetBatch topExpand(boolean b) {
                    mTopExpand = b;
                    return this;
                }

                public BaseSetBatch autoFold(boolean b) {
                    mAutoFold = b;
                    return this;
                }

                public BaseSetBatch stopFling(boolean b) {
                    mStopFling = b;
                    return this;
                }

                @Override
                public void onViewAttachedToWindow(View v) {
                    if (isAttached) return;
                    isAttached = true;
                    mFold.getViewTreeObserver().addOnGlobalLayoutListener(this);
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    if (!isAttached) return;
                    isAttached = false;
                    mFold.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                @Override
                public void onGlobalLayout() {
                    int orientation = getFoldOrientation();
                    if (orientation > 0 && orientation != mFold.getFoldOrientation())
                        mFold.setFoldOrientation(orientation);
                    float range = getFoldRange();
                    if (range > 0 && range != mFold.getFoldRange()) mFold.setFoldRange(range);
                }

                @Override
                public boolean down(RootView view) {
                    return true;
                }

                @Override
                public boolean move(RootView view, float[] xy) {
                    return (!view.horizontal() == (mFold.getFoldOrientation() != HORIZONTAL)) && (!view.overflow()) && (!mTopExpand || !mFold.isCollapse() || view.edge(true));
                }

                @Override
                public boolean up(RootView view) {
                    return mAutoFold;
                }

                @Override
                public boolean fling(RootView view) {
                    return !mStopFling || mFold.isExpand() || mFold.isCollapse();
                }

                protected abstract int getFoldOrientation();

                protected abstract float getFoldRange();
            }

            public static class VerticalSetBatch extends BaseSetBatch {
                private View mHeader;
                private View mContent;
                private View mFooter;

                public VerticalSetBatch(FoldView fold, View header, View content) {
                    this(fold, header, content, null);
                }

                public VerticalSetBatch(FoldView fold, View header, View content, View footer) {
                    super(fold);
                    mHeader = header;
                    mContent = content;
                    mFooter = footer;
                }

                @Override
                protected int getFoldOrientation() {
                    return VERTICAL;
                }

                @Override
                protected float getFoldRange() {
                    return mHeader.getHeight();
                }

                @Override
                public float speed(float value, float target) {
                    float d = (float) (2 - 2 * (1 - Math.sqrt(Math.abs(value - target))));
                    if (d < 0.01f) d = 0.01f;
                    return d / 300;
                }

                @Override
                public void display(FoldView view, float value) {
                    int range = (int) view.getFoldRange();
                    int offset = (int) (range * (value - 1));
                    mHeader.setTranslationY(offset);
                    mContent.setTranslationY(offset + range);
                    if (mFooter != null) mFooter.setTranslationY(mFooter.getHeight() * (1 - value));
                    view.invalidate();
                }
            }

            public static class HorizontalSetBatch extends BaseSetBatch {
                private View mHeader;
                private View mContent;
                private View mFooter;

                public HorizontalSetBatch(FoldView fold, View header, View content) {
                    this(fold, header, content, null);
                }

                public HorizontalSetBatch(FoldView fold, View header, View content, View footer) {
                    super(fold);
                    mHeader = header;
                    mContent = content;
                    mFooter = footer;
                }

                @Override
                protected int getFoldOrientation() {
                    return HORIZONTAL;
                }

                @Override
                protected float getFoldRange() {
                    return mHeader.getWidth();
                }

                @Override
                public float speed(float value, float target) {
                    float d = (float) (2 - 2 * (1 - Math.sqrt(Math.abs(value - target))));
                    if (d < 0.01f) d = 0.01f;
                    return d / 300;
                }

                @Override
                public void display(FoldView view, float value) {
                    int range = (int) view.getFoldRange();
                    int offset = (int) (range * (value - 1));
                    mHeader.setTranslationX(offset);
                    mContent.setTranslationX(offset + range);
                    if (mFooter != null) mFooter.setTranslationX(mFooter.getWidth() * (1 - value));
                    view.invalidate();
                }
            }
        }

        boolean down(RootView view);

        boolean move(RootView view, float[] xy);

        boolean up(RootView view);

        boolean fling(RootView view);

        float speed(float value, float target);

        void display(FoldView view, float value);
    }
}
