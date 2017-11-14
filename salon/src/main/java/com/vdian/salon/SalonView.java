package com.vdian.salon;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.vdian.uikit.view.TransitionController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 17/2/13.
 */
public abstract class SalonView extends RelativeLayout {
    protected static final int CHILD_SIZE = 3; //复用总量
    protected static final int CHILD_MARGIN = 20; //横向间距
    protected static final float ZOOM_RANGE = 3f; //缩放上限

    protected float mOffset = 0; //翻页偏移
    protected ViewGroup mContainer; //展览品容器
    protected int mChildIndex = 0; //当前展览品序号
    protected View[] mExhibits = new View[CHILD_SIZE]; //展览品
    protected float[] mDxs = new float[CHILD_SIZE]; //展览品横向偏移
    protected float[] mDys = new float[CHILD_SIZE]; //展览品纵向偏移
    protected float[] mScales = new float[CHILD_SIZE]; //展览品缩放比例
    protected int mUrlIndex = 0; //当前链接序号
    protected List<String> mUrls; //链接列表
    protected boolean didLayout = false; //是否已布局
    protected boolean doConsume = false; //消除模式
    protected boolean doSpring = false; //弹簧模式
    protected boolean doZoom = false; //缩放模式
    protected float zoomX = 0; //缩放中心横坐标
    protected float zoomY = 0; //缩放中心纵坐标
    protected SalonDelegate mDelegate; //手势处理类
    protected SalonAnimation mAnimator; //动画处理类

    public SalonView(Context context) {
        super(context);
        init();
    }

    public SalonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SalonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        mContainer = new RelativeLayout(getContext());
        mContainer.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mContainer);
        for (int i = 0; i < CHILD_SIZE; i++) {
            mExhibits[i] = onCreateExhibit(getContext()); //创建展览品
            mExhibits[i].setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mContainer.addView(mExhibits[i]);
        }
        mDelegate = new SalonDelegate();
    }

    public void display(int index, List<String> urls) {
        if (mAnimator != null) {
            mAnimator.cancel(); //停止当前动画
            mAnimator = null;
        }
        mOffset = 0;
        mChildIndex = 0;
        mUrlIndex = index;
        mUrls = urls;
        for (int d = 0; d <= CHILD_SIZE / 2; d++) {
            int l = (CHILD_SIZE + mChildIndex - d) % CHILD_SIZE;
            mDxs[l] = 0;
            mDys[l] = 0;
            mScales[l] = 1;
            ((SalonExhibit) mExhibits[l]).setMatrix(mDxs[l], mDys[l], mScales[l]); //重置矩阵
            String lUrl = null;
            if (mUrlIndex - d >= 0 && mUrlIndex - d < mUrls.size()) lUrl = mUrls.get(mUrlIndex - d);
            ((SalonExhibit) mExhibits[l]).loadUrl(lUrl); //加载对应链接
            if (d == 0) continue;
            int r = (CHILD_SIZE + mChildIndex + d) % CHILD_SIZE;
            mDxs[r] = 0;
            mDys[r] = 0;
            mScales[r] = 1;
            ((SalonExhibit) mExhibits[r]).setMatrix(mDxs[r], mDys[r], mScales[r]); //重置矩阵
            String rUrl = null;
            if (mUrlIndex + d >= 0 && mUrlIndex + d < mUrls.size()) rUrl = mUrls.get(mUrlIndex + d);
            ((SalonExhibit) mExhibits[r]).loadUrl(rUrl); //加载对应链接
        }
        update(); //更新翻页偏移
    }

    protected boolean update() {
        boolean reset = false; //是否发生复用移位
        if (didLayout) {
            while (Math.abs(mOffset) >= getWidth() + CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density) {
                int direction = mOffset > 0 ? 1 : -1;
                if ((direction < 0 && mUrlIndex <= 0) || (direction > 0 && mUrlIndex >= mUrls.size() - 1))
                    break;
                mDxs[mChildIndex] = 0;
                mDys[mChildIndex] = 0;
                mScales[mChildIndex] = 1;
                ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]); //重置矩阵
                String url = null;
                int d = (direction > 0 ? 1 : -1) * (CHILD_SIZE / 2 + 1);
                if (mUrlIndex + d >= 0 && mUrlIndex + d < mUrls.size())
                    url = mUrls.get(mUrlIndex + d);
                ((SalonExhibit) mExhibits[(CHILD_SIZE + mChildIndex - (direction > 0 ? 1 : -1) * CHILD_SIZE / 2) % CHILD_SIZE]).loadUrl(url); //加载对应链接
                mOffset -= direction * (getWidth() + CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density); //更新翻页偏移
                mChildIndex = (CHILD_SIZE + mChildIndex + direction) % CHILD_SIZE; //更新当前展览品序号
                mUrlIndex += direction; //更新当前链接序号
                reset = true;
            }
            for (int d = 0; d <= CHILD_SIZE / 2; d++) {
                mExhibits[(CHILD_SIZE + mChildIndex - d) % CHILD_SIZE].setTranslationX(-d * (getWidth() + CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density));
                if (d == 0) continue;
                mExhibits[(CHILD_SIZE + mChildIndex + d) % CHILD_SIZE].setTranslationX(d * (getWidth() + CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density));
            }
            mContainer.scrollTo((int) mOffset, 0);
            onUpdate(mUrlIndex + mOffset / (getWidth() + CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density), mUrls != null ? mUrls.size() : 0); //更新页号信息
        }
        return reset;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        didLayout = true; //完成布局
        update();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mDelegate.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDelegate.onInterceptTouchEvent(event);
    }

    protected void permitTouchEvent(boolean permit) {
        mDelegate.permitTouchEvent(permit); //触控许可
    }

    protected void down() {
        doConsume = false;
        doSpring = false;
        doZoom = false;
        if (mAnimator != null) {
            mAnimator.cancel(); //停止当前动画
            mAnimator = null;
        }
    }

    protected void premove(boolean x, boolean y) {
        float ratio = ((SalonExhibit) mExhibits[mChildIndex]).getRatio();
        if (ratio > 0) {
            if (mScales[mChildIndex] < 1 || mScales[mChildIndex] > ZOOM_RANGE || mDxs[mChildIndex] != reform(true, ratio, mScales[mChildIndex], mDxs[mChildIndex]) || mDys[mChildIndex] != reform(false, ratio, mScales[mChildIndex], mDys[mChildIndex]))
                doSpring = true; //越界缩放、越界滚动
            else {
                if (mOffset == 0 && y && overflow(false, ratio, mScales[mChildIndex]) > 0 && overflow(true, ratio, mScales[mChildIndex]) <= 0)
                    doConsume = true; //未翻页、纵向滚动、高度充足、宽度不足
            }
        }
    }

    protected void move(float dx, float dy, float px, float py, float sk) {
        float ratio = ((SalonExhibit) mExhibits[mChildIndex]).getRatio();
        if (mOffset != 0 || ratio <= 0) {
            if ((mUrlIndex <= 0 && mOffset < 0) || (mUrlIndex >= mUrls.size() - 1 && mOffset > 0)) {
                float k = (getWidth() - Math.abs(mOffset)) / getWidth();
                if (k <= 0) dx = 0;
                else if (k < 1) dx *= (float) Math.pow(k, 4.3);
            }
            float tmp = mOffset;
            mOffset -= dx;
            if (mOffset * tmp < 0) mOffset = 0;
            update(); //更新翻页偏移
        } else {
            float kx = (getWidth() - Math.abs(mDxs[mChildIndex] - reform(true, ratio, mScales[mChildIndex], mDxs[mChildIndex]))) / getWidth();
            if (kx <= 0) dx = 0;
            else if (kx < 1) dx *= (float) Math.pow(kx, 4.3);
            float ky = (getHeight() - Math.abs(mDys[mChildIndex] - reform(false, ratio, mScales[mChildIndex], mDys[mChildIndex]))) / getHeight();
            if (ky <= 0) dy = 0;
            else if (ky < 1) dy *= (float) Math.pow(ky, 4.3);
            float ks = mScales[mChildIndex] < 1 ? mScales[mChildIndex] : (mScales[mChildIndex] > ZOOM_RANGE ? (2 - mScales[mChildIndex] / ZOOM_RANGE) : 1);
            if (ks <= 0) sk = 1;
            else if (ks < 1)
                sk = (float) (1 + (sk - 1) * Math.pow(ks, 4.3 * (mScales[mChildIndex] < 1 ? 1 : 0.5)));
            dy += (mDys[mChildIndex] + getHeight() / 2 - py) * (sk - 1) + mDys[mChildIndex];
            dx += (mDxs[mChildIndex] + getWidth() / 2 - px) * (sk - 1) + mDxs[mChildIndex];
            sk *= mScales[mChildIndex];
            if (sk != mScales[mChildIndex]) {
                doConsume = false; //缩放关闭消除模式
                doSpring = true; //缩放开启弹簧模式
            } else {
                if (doConsume) dx = mDxs[mChildIndex];
                if (!doSpring) {
                    if (overflow(false, ratio, mScales[mChildIndex]) <= 0) dy = mDys[mChildIndex];
                    float rx = reform(true, ratio, mScales[mChildIndex], dx);
                    float ry = reform(false, ratio, mScales[mChildIndex], dy);
                    if (dx != rx) {
                        mOffset -= (dx - rx);
                        dx = rx;
                        dy = ry;
                        update(); //更新翻页偏移
                    } else if (dy != ry) doSpring = true; //纵向越界开启弹簧模式
                }
            }
            mScales[mChildIndex] = sk;
            mDxs[mChildIndex] = dx;
            mDys[mChildIndex] = dy;
            ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]); //更新矩阵
        }
    }

    protected void up(final float vx, final float vy) {
        final float ratio = ((SalonExhibit) mExhibits[mChildIndex]).getRatio();
        if (mOffset != 0) { //翻页动画
            int direction = 0;
            if (Math.abs(vx) > 0.4f * getContext().getResources().getDisplayMetrics().density) {
                if (vx < 0) {
                    if (mOffset > 0) direction = 1;
                } else {
                    if (mOffset < 0) direction = -1;
                }
            } else if (Math.abs(mOffset) > (getWidth() + CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density) / 2) {
                if (mOffset > 0) direction = 1;
                else direction = -1;
            }
            if ((direction < 0 && mUrlIndex <= 0) || (direction > 0 && mUrlIndex >= mUrls.size() - 1))
                direction = 0;
            final float sOffset = mOffset;
            final float eOffset = (direction == 0) ? 0 : ((direction > 0) ? (getWidth() + CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density) : (-getWidth() - CHILD_MARGIN * getContext().getResources().getDisplayMetrics().density));
            mAnimator = new SalonAnimation((long) (300 + 100 * Math.abs(eOffset - sOffset) / getWidth()), new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    if (input > 0 && input < 1) {
                        input = (1.0f - (1.0f - input) * (1.0f - input));
                        input = (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
                    }
                    return input;
                }
            }) {
                @Override
                protected void display(SalonAnimation animation, float value, long time) {
                    mOffset = sOffset * (1 - value) + eOffset * value;
                    if (update()) animation.cancel();
                }
            }.start();
        } else if (ratio > 0) {
            if (doZoom) { //缩放动画
                final float sScale = mScales[mChildIndex], sDx = mDxs[mChildIndex], sDy = mDys[mChildIndex];
                final float eScale, eDx, eDy;
                if (sScale <= 1) {
                    eScale = ZOOM_RANGE;
                    float tDx = (sDx + getWidth() / 2 - zoomX) * (eScale / sScale - 1);
                    eDx = reform(true, ratio, ZOOM_RANGE, tDx);
                    float tDy = (sDy + getHeight() / 2 - zoomY) * (eScale / sScale - 1);
                    eDy = reform(false, ratio, ZOOM_RANGE, tDy);
                } else {
                    eScale = 1;
                    eDx = 0;
                    eDy = 0;
                }
                mAnimator = new SalonAnimation((long) (150 + 150 * Math.abs(eScale - sScale) / (ZOOM_RANGE - 1)), new AccelerateDecelerateInterpolator()) {
                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        mScales[mChildIndex] = sScale * (1 - value) + eScale * value;
                        mDxs[mChildIndex] = sDx * (1 - value) + eDx * value;
                        mDys[mChildIndex] = sDy * (1 - value) + eDy * value;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                    }
                }.start();
            } else if (mScales[mChildIndex] < 1) { //缩放复位动画
                final float sScale = mScales[mChildIndex];
                final float eScale = 1;
                final float sDx = mDxs[mChildIndex];
                final float eDx = 0;
                final float sDy = mDys[mChildIndex];
                final float eDy = 0;
                mAnimator = new SalonAnimation((long) (200 + 100 * (eScale - sScale)), new DecelerateInterpolator()) {
                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        mScales[mChildIndex] = sScale * (1 - value) + eScale * value;
                        mDxs[mChildIndex] = sDx * (1 - value) + eDx * value;
                        mDys[mChildIndex] = sDy * (1 - value) + eDy * value;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                    }
                }.start();
            } else if (mScales[mChildIndex] > ZOOM_RANGE) { //缩放复位动画
                final float sScale = mScales[mChildIndex];
                final float eScale = ZOOM_RANGE;
                final float sDx = mDxs[mChildIndex];
                float tDx = sDx * eScale / sScale;
                final float eDx = reform(true, ratio, ZOOM_RANGE, tDx);
                final float sDy = mDys[mChildIndex];
                float tDy = sDy * eScale / sScale;
                final float eDy = reform(false, ratio, ZOOM_RANGE, tDy);
                mAnimator = new SalonAnimation((long) (200 + 100 * (sScale - eScale) / ZOOM_RANGE), new DecelerateInterpolator()) {
                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        mScales[mChildIndex] = sScale * (1 - value) + eScale * value;
                        mDxs[mChildIndex] = sDx * (1 - value) + eDx * value;
                        mDys[mChildIndex] = sDy * (1 - value) + eDy * value;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                    }
                }.start();
            } else { //抛掷动画
                mAnimator = new SalonAnimation(Long.MAX_VALUE, null) {
                    protected PhysicalCalculator xCalculator = new PhysicalCalculator(overflow(true, ratio, mScales[mChildIndex]), mDxs[mChildIndex], vx);
                    protected PhysicalCalculator yCalculator = new PhysicalCalculator(overflow(false, ratio, mScales[mChildIndex]), mDys[mChildIndex], vy);

                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        boolean xEnd = xCalculator.calculate(time);
                        boolean yEnd = yCalculator.calculate(time);
                        if (xEnd && yEnd) animation.cancel();
                        mDxs[mChildIndex] = xCalculator.load;
                        mDys[mChildIndex] = yCalculator.load;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                    }

                    class PhysicalCalculator {
                        protected MixedFriction friction = new MixedFriction(0.0008 * getContext().getResources().getDisplayMetrics().density, 0.125 * getContext().getResources().getDisplayMetrics().density);
                        protected UnderDamping damping = new UnderDamping(0.00025, 0.0266);
                        protected int type;
                        protected long time;
                        protected float overflow;
                        protected float save;
                        public float load;

                        public PhysicalCalculator(float overflow, float origin, float velocity) {
                            this.overflow = overflow;
                            load = origin;
                            save = load;
                            time = 0;
                            if (save >= -overflow / 2 && save <= overflow / 2) {
                                type = 0;
                                friction.set(velocity < 0 ? (save + overflow / 2) : -(save - overflow / 2), velocity);
                            } else {
                                type = 1;
                                float offset = save - (overflow <= 0 ? 0 : (save < 0 ? -overflow / 2 : overflow / 2));
                                damping.set(offset, offset != 0 ? velocity : 0); //未满屏未越界抵消速度
                            }
                        }

                        public boolean calculate(long t) {
                            long delta = t - time;
                            switch (type) {
                                case 0: //分段摩擦
                                    if (delta < friction.t1) {
                                        load = save + (float) friction.get(delta);
                                        return false;
                                    } else {
                                        if (friction.overflow) {
                                            load = overflow <= 0 ? 0 : (friction.v0 < 0 ? -overflow / 2 : overflow / 2);
                                            save = load;
                                            type = 1;
                                            time = t - (delta - friction.t1);
                                            double v = friction.xov1;
                                            if (overflow <= 0) { //纯欠阻尼能量损耗
                                                double k = 0.75;
                                                double b = 0.025 * getContext().getResources().getDisplayMetrics().density;
                                                if (v > 0) {
                                                    v = k * v - b;
                                                    if (v < 0) v = 0;
                                                } else {
                                                    v = k * v + b;
                                                    if (v > 0) v = 0;
                                                }
                                            }
                                            damping.set(0, v);
                                            return calculate(t);
                                        } else {
                                            if (overflow <= 0) load = 0;
                                            else {
                                                load = save + (float) friction.xov1;
                                                if (load < -overflow / 2) load = -overflow / 2;
                                                else if (load > overflow / 2) load = overflow / 2;
                                            }
                                            return true; //运动终止
                                        }
                                    }
                                case 1: //欠阻尼
                                    if (delta < damping.t1) {
                                        load = (overflow <= 0 ? 0 : (save < 0 ? -overflow / 2 : overflow / 2)) + (float) damping.get(delta);
                                        return false;
                                    } else {
                                        load = overflow <= 0 ? 0 : (save < 0 ? -overflow / 2 : overflow / 2);
                                        save = load;
                                        type = 0;
                                        time = t - (delta - damping.t1);
                                        friction.set(damping.v1 < 0 ? (save + overflow / 2) : -(save - overflow / 2), damping.v1);
                                        return calculate(t);
                                    }
                            }
                            return false;
                        }
                    }

                    class MixedFriction {
                        protected double coefficient; //摩擦系数
                        protected double vLimit; //速度临界点
                        public double v0; //初始速度
                        public boolean overflow; //是否溢出
                        public long t1; //停止或溢出时长
                        public double xov1; //停止位移或溢出速度

                        public MixedFriction(double coefficient, double vLimit) {
                            this.coefficient = coefficient;
                            this.vLimit = vLimit;
                        }

                        public MixedFriction set(double xLimit, double v0) {
                            this.v0 = v0;
                            if (v0 == 0) {
                                overflow = false;
                                t1 = 0;
                                xov1 = 0;
                            } else if (xLimit <= 0) {
                                overflow = true;
                                t1 = 0;
                                xov1 = v0;
                            } else {
                                double xMax = get(Long.MAX_VALUE);
                                if (Math.abs(xMax) <= xLimit) {
                                    overflow = false;
                                    if (Math.abs(v0) > vLimit)
                                        t1 = (long) (Math.log(Math.abs(v0) / vLimit) / (coefficient / vLimit) + vLimit / coefficient);
                                    else t1 = (long) (Math.abs(v0) / coefficient);
                                    xov1 = xMax;
                                } else {
                                    overflow = true;
                                    if (Math.abs(v0) > vLimit) {
                                        double xLeave = xLimit - (Math.abs(v0) - vLimit) / (coefficient / vLimit);
                                        if (xLeave <= 0) {
                                            t1 = (long) (-Math.log(1 - (coefficient / vLimit) * xLimit / Math.abs(v0)) / (coefficient / vLimit));
                                            xov1 = (v0 > 0 ? 1 : -1) * (Math.abs(v0) - (coefficient / vLimit) * xLimit);
                                        } else {
                                            t1 = (long) (Math.log(Math.abs(v0) / vLimit) / (coefficient / vLimit) + (vLimit - Math.sqrt(Math.max(0, vLimit * vLimit - 2 * coefficient * xLeave))) / coefficient);
                                            xov1 = (v0 > 0 ? 1 : -1) * Math.sqrt(Math.max(0, vLimit * vLimit - 2 * coefficient * xLeave));
                                        }
                                    } else {
                                        t1 = (long) ((Math.abs(v0) - Math.sqrt(Math.max(0, v0 * v0 - 2 * coefficient * xLimit))) / coefficient);
                                        xov1 = (v0 > 0 ? 1 : -1) * Math.sqrt(Math.max(0, v0 * v0 - 2 * coefficient * xLimit));
                                    }
                                }
                            }
                            return this;
                        }

                        public double get(long t) {
                            if ((t != Long.MAX_VALUE) && (Math.abs(v0) * Math.pow(Math.E, -(coefficient / vLimit) * t) >= vLimit))
                                return (v0 / (coefficient / vLimit)) * (1 - Math.pow(Math.E, -(coefficient / vLimit) * t));
                            else {
                                double dv = vLimit;
                                double dx = (Math.abs(v0) - vLimit) / (coefficient / vLimit);
                                double dt = t;
                                if (dx < 0) {
                                    dv = Math.abs(v0);
                                    dx = 0;
                                } else
                                    dt -= Math.log(Math.abs(v0) / vLimit) / (coefficient / vLimit);
                                dt = Math.max(dt, 0);
                                dt = Math.min(dt, dv / coefficient);
                                dx += (-(coefficient / 2) * dt * dt + dv * dt);
                                return v0 > 0 ? dx : -dx;
                            }
                        }
                    }

                    class UnderDamping {
                        protected double tension; //劲度系数
                        protected double friction; //摩擦系数
                        protected double x0; //初始位置
                        protected double v0; //初始速度
                        public long t1; //首次归零时长
                        public double v1; //首次归零速度

                        public UnderDamping(double tension, double friction) {
                            this.tension = tension;
                            this.friction = friction;
                        }

                        public UnderDamping set(double x0, double v0) {
                            this.x0 = x0;
                            this.v0 = v0;
                            if (x0 == 0 && v0 == 0) {
                                t1 = 0;
                                v1 = 0;
                            } else {
                                double angle;
                                if (x0 == 0) angle = Math.PI;
                                else {
                                    double denominator = v0 / x0 + friction / 2;
                                    angle = denominator == 0 ? (Math.PI / 2) : Math.atan(-Math.sqrt(tension - friction * friction / 4) / denominator);
                                    if (angle <= 0) angle += Math.PI;
                                }
                                t1 = (long) (angle / Math.sqrt(tension - friction * friction / 4));
                                v1 = (((-(friction / 2) * v0 - tension * x0) / Math.sqrt(tension - friction * friction / 4)) * Math.sin(Math.sqrt(tension - friction * friction / 4) * t1) + v0 * Math.cos(Math.sqrt(tension - friction * friction / 4) * t1)) * Math.pow(Math.E, (-friction / 2) * t1);
                            }
                            return this;
                        }

                        public double get(long t) {
                            return (((v0 + (friction / 2) * x0) / Math.sqrt(tension - friction * friction / 4)) * Math.sin(Math.sqrt(tension - friction * friction / 4) * t) + x0 * Math.cos(Math.sqrt(tension - friction * friction / 4) * t)) * Math.pow(Math.E, (-friction / 2) * t);
                        }
                    }
                }.start();
            }
        }
    }

    protected float overflow(boolean x, float ratio, float scale) { //溢出值计算
        return x ? ((ratio * getHeight() <= getWidth()) ? (scale * getHeight() * ratio - getWidth()) : ((scale - 1) * getWidth())) : ((ratio * getHeight() >= getWidth()) ? (scale * getWidth() / ratio - getHeight()) : ((scale - 1) * getHeight()));
    }

    protected float reform(boolean x, float ratio, float scale, float delta) { //越界值复位
        float overflow = overflow(x, ratio, scale);
        if (overflow <= 0) return 0;
        else {
            if (delta < -overflow / 2) return -overflow / 2;
            if (delta > overflow / 2) return overflow / 2;
            return delta;
        }
    }

    protected abstract void onUpdate(float index, int size);

    protected abstract void onClick(float x, float y);

    protected abstract void onDoubleClick(float x, float y);

    protected abstract void onLongClick(float x, float y);

    protected abstract View onCreateExhibit(Context context);

    public interface SalonExhibit {
        void loadUrl(String url);

        float getRatio();

        void setMatrix(float dx, float dy, float scale);
    }

    protected class SalonDelegate extends Handler {
        protected boolean mPermit = true; //触控许可
        protected int mConsume = 0; //触控消耗对象
        protected boolean doIntercept = false; //是否拦截事件
        protected List<Integer> touchPointId = new ArrayList<>(); //触控点采集索引
        protected List<Float> touchPointX = new ArrayList<>(); //触控点采集横坐标
        protected List<Float> touchPointY = new ArrayList<>(); //触控点采集纵坐标
        protected float downX = 0; //落点横坐标
        protected float downY = 0; //落点纵坐标
        protected float deltaX = 0; //横向偏移
        protected float deltaY = 0; //纵向偏移
        protected float pivotX = 0; //缩放中心横坐标
        protected float pivotY = 0; //缩放中心纵坐标
        protected float scaleK = 1; //缩放比值
        protected float absX = 0; //横向总路程
        protected float absY = 0; //纵向总路程
        protected float velocityX = 0; //横向速度
        protected float velocityY = 0;//纵向速度
        protected VelocityTracker mTracker; //速度跟踪器
        protected float scaleLimit; //缩放指距门限
        protected float absLimit; //移动触发门限
        protected float doubleLimit; //双击点距门限
        protected boolean doCancel = false; //中断事件
        protected boolean doMove = false; //移动事件
        protected float clickX = 0; //点击横坐标
        protected float clickY = 0; //点击纵坐标
        protected boolean singleClick = false; //单击延时中
        protected boolean doubleClick = false; //双击判定中
        protected boolean longClick = false; //长按判定中

        public SalonDelegate() {
            scaleLimit = 50 * SalonView.this.getContext().getResources().getDisplayMetrics().density;
            absLimit = 4 * SalonView.this.getContext().getResources().getDisplayMetrics().density;
            doubleLimit = 50 * SalonView.this.getContext().getResources().getDisplayMetrics().density;
            SalonView.this.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    removeMessages(0);
                    removeMessages(1);
                }
            });
        }

        public boolean dispatchTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!mPermit) mConsume = -1;
                else {
                    doIntercept = false;
                    if (SalonView.super.dispatchTouchEvent(event)) mConsume = 0;
                    else {
                        mConsume = 1;
                        delegateTouchEvent(event);
                    }
                }
            } else {
                switch (mConsume) {
                    case 0:
                        SalonView.super.dispatchTouchEvent(event);
                        break;
                    case 1:
                        delegateTouchEvent(event);
                        break;
                }
            }
            return true;
        }

        public boolean onInterceptTouchEvent(MotionEvent event) {
            boolean b = SalonView.super.onInterceptTouchEvent(event);
            return b || doIntercept;
        }

        public void permitTouchEvent(boolean permit) {
            if (mPermit == permit) return;
            mPermit = permit;
            if (!mPermit) {
                doIntercept = true;
                if (!doCancel) {
                    doCancel = true;
                    singleClick = false;
                    removeMessages(0);
                    removeMessages(1);
                    if (touchPointId.size() != 0) SalonView.this.up(0, 0);
                }
            }
        }

        protected void delegateTouchEvent(MotionEvent event) {
            if (mTracker == null) mTracker = VelocityTracker.obtain();
            mTracker.addMovement(event);
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    mTracker.computeCurrentVelocity(1);
                    velocityX = mTracker.getXVelocity();
                    velocityY = mTracker.getYVelocity();
                case MotionEvent.ACTION_CANCEL:
                    mTracker.recycle();
                    mTracker = null;
                    break;
            }
            int action = event.getAction();
            int masked = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || masked == MotionEvent.ACTION_POINTER_DOWN) {
                if (action == MotionEvent.ACTION_DOWN) {
                    touchPointId.clear();
                    touchPointX.clear();
                    touchPointY.clear();
                    downX = event.getX();
                    downY = event.getY();
                    absX = 0;
                    absY = 0;
                }
                touchPointId.add(event.getPointerId(event.getActionIndex()));
                touchPointX.add(event.getX(event.getActionIndex()));
                touchPointY.add(event.getY(event.getActionIndex()));
            }
            if (action == MotionEvent.ACTION_MOVE) {
                deltaX = 0;
                deltaY = 0;
                pivotX = 0;
                pivotY = 0;
                scaleK = 1;
                boolean b = false;
                float oldX = 0;
                float oldY = 0;
                float newX = 0;
                float newY = 0;
                for (int i = 0; i < touchPointId.size(); i++) {
                    int index = event.findPointerIndex(touchPointId.get(i));
                    if (index != -1) {
                        if (!b) {
                            b = true;
                            oldX = touchPointX.get(i);
                            oldY = touchPointY.get(i);
                            newX = event.getX(index);
                            newY = event.getY(index);
                            deltaX = newX - oldX;
                            deltaY = newY - oldY;
                            pivotX = oldX;
                            pivotY = oldY;
                            touchPointX.set(i, newX);
                            touchPointY.set(i, newY);
                        } else {
                            float oldDistance = (float) Math.sqrt((touchPointX.get(i) - oldX) * (touchPointX.get(i) - oldX) + (touchPointY.get(i) - oldY) * (touchPointY.get(i) - oldY));
                            float newDistance = (float) Math.sqrt((event.getX(index) - newX) * (event.getX(index) - newX) + (event.getY(index) - newY) * (event.getY(index) - newY));
                            oldDistance = oldDistance < scaleLimit ? scaleLimit : oldDistance;
                            newDistance = newDistance < scaleLimit ? scaleLimit : newDistance;
                            scaleK *= (newDistance / oldDistance);
                            touchPointX.set(i, event.getX(index));
                            touchPointY.set(i, event.getY(index));
                        }
                    }
                }
                absX += Math.abs(deltaX);
                absY += Math.abs(deltaY);
            }
            if (action == MotionEvent.ACTION_UP || masked == MotionEvent.ACTION_POINTER_UP) {
                int id = event.getPointerId(event.getActionIndex());
                for (int i = touchPointId.size() - 1; i >= 0; i--) {
                    if (touchPointId.get(i) == id) {
                        touchPointId.remove(i);
                        touchPointX.remove(i);
                        touchPointY.remove(i);
                    }
                }
            }
            if (action == MotionEvent.ACTION_CANCEL) {
                touchPointId.clear();
                touchPointX.clear();
                touchPointY.clear();
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                doCancel = false;
                doMove = false;
                doubleClick = singleClick;
                singleClick = false;
                longClick = false;
                removeMessages(0);
                sendEmptyMessageDelayed(1, 500);
                SalonView.this.down();
            } else {
                if (!doCancel) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            if (doMove) SalonView.this.move(deltaX, deltaY, pivotX, pivotY, scaleK);
                            else {
                                if (absX > absLimit || absY > absLimit) {
                                    doMove = true;
                                    removeMessages(1);
                                    SalonView.this.premove(absX > absY, absY > absX);
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (doMove) SalonView.this.up(velocityX, velocityY);
                            else {
                                if (!longClick) {
                                    if (doubleClick && Math.sqrt((clickX - event.getX()) * (clickX - event.getX()) + (clickY - event.getY()) * (clickY - event.getY())) < doubleLimit)
                                        SalonView.this.onDoubleClick(event.getX(), event.getY());
                                    else {
                                        singleClick = true;
                                        clickX = event.getX();
                                        clickY = event.getY();
                                        sendEmptyMessageDelayed(0, 250);
                                    }
                                }
                                SalonView.this.up(0, 0);
                            }
                            removeMessages(1);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            SalonView.this.up(0, 0);
                            removeMessages(1);
                            break;
                        default:
                            if (!doMove) {
                                doMove = true;
                                removeMessages(1);
                                SalonView.this.premove(absX > absY, absY > absX);
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    SalonView.this.onClick(clickX, clickY);
                    singleClick = false;
                    break;
                case 1:
                    SalonView.this.onLongClick(downX, downY);
                    longClick = true;
                    break;
            }
        }
    }

    protected abstract class SalonAnimation implements OnAttachStateChangeListener, ViewTreeObserver.OnPreDrawListener {
        protected boolean isAttached; //控件是否添加
        protected boolean hasInit; //有无初始化
        protected int mStage; //当前执行阶段
        protected long mTime; //当前播放时长
        protected long mDuration; //动画总时长
        protected TimeInterpolator mInterpolator; //动画插值器
        protected TransitionController mAnimation; //动画计算器

        public SalonAnimation(long duration, TimeInterpolator interpolator) {
            isAttached = false;
            hasInit = false;
            mStage = -1;
            mTime = 0;
            mDuration = duration;
            mInterpolator = interpolator;
        }

        public SalonAnimation start() {
            if (mStage == -1) {
                mStage = 0;
                mAnimation = new TransitionController(new TransitionController.TransitionListener() {
                    @Override
                    public float speed(float value, float target) {
                        return 1f / mDuration;
                    }

                    @Override
                    public void display(View self, float value) {
                        SalonAnimation.this.display(SalonAnimation.this, mInterpolator == null ? value : mInterpolator.getInterpolation(value), mTime);
                        if (mAnimation.getValue() == 1) {
                            if (mStage == 0) {
                                mStage = 1;
                                onEnd();
                            }
                        }
                    }
                }, SalonView.this) {
                    @Override
                    protected long reduce(long time) {
                        mTime += time;
                        return super.reduce(time);
                    }
                };
                mAnimation.setTarget(1);
                onStart();
            }
            return this;
        }

        public SalonAnimation cancel() {
            if (mStage == 0) {
                mStage = 1;
                onCancel();
                onEnd();
            }
            return this;
        }

        protected void onStart() {
            onViewAttachedToWindow(null);
            addOnAttachStateChangeListener(this);
        }

        protected void onCancel() {
        }

        protected void onEnd() {
            removeOnAttachStateChangeListener(this);
            onViewDetachedFromWindow(null);
        }

        @Override
        public void onViewAttachedToWindow(View v) {
            if (isAttached) return;
            isAttached = true;
            getViewTreeObserver().addOnPreDrawListener(this);
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            if (!isAttached) return;
            isAttached = false;
            getViewTreeObserver().removeOnPreDrawListener(this);
        }

        @Override
        public boolean onPreDraw() {
            if (!hasInit) {
                hasInit = true;
                mAnimation.initDisplay();
            }
            mAnimation.computeScroll();
            return true;
        }

        protected abstract void display(SalonAnimation animation, float value, long time);
    }
}
