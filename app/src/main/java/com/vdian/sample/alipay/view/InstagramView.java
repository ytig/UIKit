package com.vdian.sample.alipay.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;

import com.vdian.refresh.RefreshHintView;

/**
 * Created by zhangliang on 16/12/28.
 */
public class InstagramView extends RefreshHintView {
    private static RectF RectF = new RectF();

    private UnRefresh unrefresh;
    private Refresh refresh;

    public InstagramView(Context context) {
        super(context);
    }

    public InstagramView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InstagramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int type() {
        return VIEW_TYPE_TOP;
    }

    @Override
    protected long stay() {
        return 0;
    }

    @Override
    protected View build() {
        setWillNotDraw(false);
        unrefresh = new UnRefresh(getContext());
        unrefresh.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (90 * getResources().getDisplayMetrics().density)));
        addView(unrefresh);
        refresh = new Refresh(getContext());
        refresh.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (90 * getResources().getDisplayMetrics().density)));
        addView(refresh);
        return null;
    }

    @Override
    protected void status(int from, int to) {
        refresh.display(to == STATUS_REFRESH);
    }

    @Override
    protected void layout(int height) {
    }

    @Override
    protected void scroll(float offset) {
        setTranslationY(-(int) offset);
        unrefresh.offset(offset);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        int bottom = (int) mOffset;
        if (bottom < 0) bottom = 0;
        RectF.set(0, 0, getWidth(), bottom);
        canvas.clipRect(RectF);
        super.draw(canvas);
    }

    private static class UnRefresh extends View {
        private Paint ltPaint;
        private Paint dkPaint;
        private float mOffset;

        public UnRefresh(Context context) {
            super(context);
            ltPaint = new Paint();
            ltPaint.setAntiAlias(true);
            ltPaint.setColor(Color.LTGRAY);
            ltPaint.setStyle(Paint.Style.STROKE);
            ltPaint.setStrokeWidth(1.5f * getContext().getResources().getDisplayMetrics().density);
            dkPaint = new Paint();
            dkPaint.setAntiAlias(true);
            dkPaint.setColor(Color.DKGRAY);
            dkPaint.setStyle(Paint.Style.STROKE);
            dkPaint.setStrokeWidth(1.5f * getContext().getResources().getDisplayMetrics().density);
        }

        public void offset(float offset) {
            mOffset = offset;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int x = getWidth() / 2;
            int y = (int) (34 * getContext().getResources().getDisplayMetrics().density);
            int r = (int) (14 * getContext().getResources().getDisplayMetrics().density);
            int limit = (int) (30 * getContext().getResources().getDisplayMetrics().density);
            float angle = 360 * (mOffset - limit) / (getHeight() - limit);
            if (angle < 0) angle = 0;
            if (angle > 360) angle = 360;
            RectF.set(x - r, y - r, x + r, y + r);
            canvas.drawOval(RectF, ltPaint);
            canvas.drawArc(RectF, -90, angle, false, dkPaint);
        }
    }

    private static class Refresh extends View {
        private boolean isShow;
        private Paint mPaint;
        private Animator mAnimator;
        private Rotation mRotation;

        public Refresh(Context context) {
            super(context);
            setAlpha(0);
            isShow = false;
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.SQUARE);
            mPaint.setStrokeWidth(1.5f * getContext().getResources().getDisplayMetrics().density);
            mRotation = new Rotation();
        }

        public void display(boolean show) {
            if (isShow == show) return;
            isShow = show;
            if (isShow) {
                if (mAnimator != null && mAnimator.isRunning()) mAnimator.cancel();
                final float fromAlpha = getAlpha();
                final float fromScale = (getScaleX() + getScaleY()) / 2;
                ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                animator.setDuration(300);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Object value = animation.getAnimatedValue();
                        if (value instanceof Float) {
                            float mAlpha, mScale;
                            if ((Float) value < 0.1f) {
                                float valueAlpha = ((Float) value) / 0.1f;
                                mAlpha = fromAlpha * (1 - valueAlpha) + 1f * valueAlpha;
                            } else mAlpha = 1f;
                            if ((Float) value < 0.1f) mScale = fromScale;
                            else {
                                if ((Float) value < 0.55f) {
                                    float valueScale = ((Float) value - 0.1f) / 0.45f;
                                    mScale = fromScale * (1 - valueScale) + 1.2f * valueScale;
                                } else {
                                    float valueScale = ((Float) value - 0.55f) / 0.45f;
                                    mScale = 1.2f * (1 - valueScale) + 1f * valueScale;
                                }
                            }
                            setAlpha(mAlpha);
                            ViewParent parent = getParent();
                            if (parent instanceof ViewGroup) {
                                for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                                    View child = ((ViewGroup) parent).getChildAt(i);
                                    if (child != null && child != Refresh.this)
                                        child.setAlpha(1 - mAlpha);
                                }
                            }
                            setScaleX(mScale);
                            setScaleY(mScale);
                        }
                    }
                });
                mAnimator = animator;
                mAnimator.start();
            } else {
                if (mAnimator != null && mAnimator.isRunning()) mAnimator.cancel();
                final float fromAlpha = getAlpha();
                final float fromScale = (getScaleX() + getScaleY()) / 2;
                ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                animator.setDuration(300);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Object value = animation.getAnimatedValue();
                        if (value instanceof Float) {
                            float mAlpha, mScale;
                            if ((Float) value < 0.1f) {
                                float valueAlpha = ((Float) value) / 0.1f;
                                mAlpha = fromAlpha * (1 - valueAlpha) + 1f * valueAlpha;
                            } else {
                                if ((Float) value < 0.9f) mAlpha = 1f;
                                else {
                                    float valueAlpha = ((Float) value - 0.9f) / 0.1f;
                                    mAlpha = 1f * (1 - valueAlpha) + 0f * valueAlpha;
                                }
                            }
                            if ((Float) value < 0.1f) mScale = fromScale;
                            else {
                                if ((Float) value < 0.9f) {
                                    float valueScale = ((Float) value - 0.1f) / 0.8f;
                                    mScale = fromScale * (1 - valueScale) + 0.2f * valueScale;
                                } else {
                                    if ((Float) value < 1f) mScale = 0.2f;
                                    else mScale = 1f;
                                }
                            }
                            setAlpha(mAlpha);
                            ViewParent parent = getParent();
                            if (parent instanceof ViewGroup) {
                                for (int i = 0; i < ((ViewGroup) parent).getChildCount(); i++) {
                                    View child = ((ViewGroup) parent).getChildAt(i);
                                    if (child != null && child != Refresh.this)
                                        child.setAlpha(1 - mAlpha);
                                }
                            }
                            setScaleX(mScale);
                            setScaleY(mScale);
                        }
                    }
                });
                mAnimator = animator;
                mAnimator.start();
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            int x = getWidth() / 2;
            int y = (int) (34 * getContext().getResources().getDisplayMetrics().density);
            setPivotX(x);
            setPivotY(y);
            mPaint.setShader(new SweepGradient(x, y, new int[]{Color.LTGRAY, Color.DKGRAY}, null));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int x = getWidth() / 2;
            int y = (int) (34 * getContext().getResources().getDisplayMetrics().density);
            int r = (int) (14 * getContext().getResources().getDisplayMetrics().density);
            RectF.set(x - r, y - r, x + r, y + r);
            canvas.drawOval(RectF, mPaint);
        }

        @Override
        public void computeScroll() {
            mRotation.computeScroll();
        }

        @Override
        public void setAlpha(float alpha) {
            super.setAlpha(alpha);
            invalidate();
        }

        private class Rotation {
            private long time = 0;
            private boolean reset = true;
            private int rotation = 0;

            public void computeScroll() {
                long delta = AnimationUtils.currentAnimationTimeMillis() - time;
                time += delta;
                if (getAlpha() < 0.1f) reset = true;
                else {
                    if (reset) {
                        reset = false;
                        rotation = 270;
                    } else {
                        rotation += (360 * delta / 1000);
                        while (rotation < 0) rotation += 360;
                        while (rotation >= 360) rotation -= 360;
                    }
                    setRotation(rotation);
                    invalidate();
                }
            }
        }
    }
}
