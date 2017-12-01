package com.vdian.sample.jelly.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.vdian.uikit.view.TouchController;

/**
 * Created by zhangliang on 17/12/1.
 */
public class JellyView extends RelativeLayout implements TouchController.TouchListener {
    private static final float T = 0.00016f; //劲度系数
    private static final float F = 0.004f; //摩擦系数
    private static final float D = 2f; //弹性除数
    private static final float P = 0.346f; //弹性次方

    private int mFillColor = Color.TRANSPARENT; //填充颜色
    private int mStrokeColor = Color.TRANSPARENT; //边框颜色
    private long mTime = 0; //当前时间戳
    private DampingCore xCore = new DampingCore(T, F); //横向阻尼内核
    private DampingCore yCore = new DampingCore(T, F); //纵向阻尼内核
    private TouchController mDelegate = new TouchController(this, this, true, true); //触控处理类
    private Path mPath = new Path(); //路径
    private Matrix mMatrix = new Matrix(); //矩阵
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); //画笔
    private JellyListener mListener; //形变监听器

    public JellyView(Context context) {
        super(context);
        init();
    }

    public JellyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JellyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setWillNotDraw(false);
        setClipChildren(false);
    }

    /**
     * 设置背景颜色
     *
     * @param fill
     */
    public void setJellyColor(int fill) {
        setJellyColor(fill, Color.TRANSPARENT);
    }

    /**
     * 设置背景颜色
     *
     * @param fill
     * @param stroke
     */
    public void setJellyColor(int fill, int stroke) {
        mFillColor = fill;
        mStrokeColor = stroke;
        invalidate();
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
        return true;
    }

    @Override
    public boolean move(float moveX, float moveY) {
        float range = ((getContext().getResources().getDisplayMetrics().widthPixels + getContext().getResources().getDisplayMetrics().heightPixels) / 2) / D;
        float touch = P;
        xCore.x = spring(range, touch, xCore.x, -moveX);
        yCore.x = spring(range, touch, yCore.x, -moveY);
        invalidate();
        return true;
    }

    @Override
    public void up(float velocityX, float velocityY) {
        xCore.v = velocityX / D;
        yCore.v = velocityY / D;
        invalidate();
    }

    @Override
    public void cancel() {
        up(0, 0);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        long dt = AnimationUtils.currentAnimationTimeMillis() - mTime;
        mTime += dt;
        if (!mDelegate.isTouch()) {
            if (dt > 0 && dt < 500) {
                while (dt > 0) {
                    xCore.time(1);
                    yCore.time(1);
                    dt--;
                }
            }
            if (!xCore.is(1, 0.001f) || !yCore.is(1, 0.001f)) postInvalidate();
            else {
                xCore.x = 0;
                xCore.v = 0;
                yCore.x = 0;
                yCore.v = 0;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        double angle = angle(xCore.x, yCore.x);
        double scalar = Math.sqrt(xCore.x * xCore.x + yCore.x * yCore.x);
        if (mListener != null) mListener.jelly(angle, scalar);
        super.onDraw(canvas);
        int width = getWidth(), height = getHeight(), unit = Math.min(width, height) / 2;
        float x3 = (float) scalar;
        float x2 = 0.618f * x3;
        float x1 = 0.618f * x2;
        x1 -= unit;
        x3 += unit;
        float px1, py1, px2, py2, px3, py3, px4, py4, px5, py5, px6, py6, px7, py7;
        px1 = x1;
        py1 = 0;
        px2 = x1;
        py2 = 0.552f * unit;
        px3 = x2 - 0.552f * (x2 - x1);
        py3 = unit;
        px4 = x2;
        py4 = unit;
        px5 = x2 + 0.552f * (x3 - x2);
        py5 = unit;
        px6 = x3;
        py6 = 0.552f * unit;
        px7 = x3;
        py7 = 0;
        mPath.reset();
        mPath.moveTo(px1, py1);
        mPath.cubicTo(px2, py2, px3, py3, px4, py4);
        mPath.cubicTo(px5, py5, px6, py6, px7, py7);
        mPath.cubicTo(px6, -py6, px5, -py5, px4, -py4);
        mPath.cubicTo(px3, -py3, px2, -py2, px1, -py1);
        mPath.close();
        mMatrix.reset();
        mMatrix.setRotate((float) Math.toDegrees(angle));
        mMatrix.postTranslate(width / 2, height / 2);
        mPath.transform(mMatrix);
        if (mFillColor != Color.TRANSPARENT) {
            mPaint.setColor(mFillColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, mPaint);
        }
        if (mStrokeColor != Color.TRANSPARENT) {
            mPaint.setColor(mStrokeColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(unit / 11);
            canvas.drawPath(mPath, mPaint);
        }
    }

    /**
     * 设置形变监听器
     *
     * @param listener
     */
    public void setJellyListener(JellyListener listener) {
        mListener = listener;
        if (mListener != null) invalidate();
    }

    public interface JellyListener {
        void jelly(double angle, double scalar);
    }

    private static float spring(float range, float touch, float x, float dx) {
        boolean negative = x < 0 || (x == 0 && dx > 0);
        if (negative) {
            dx *= -1;
            x *= -1;
        }
        double tmp = range * touch * Math.pow((range / (range - x)), 1 / touch) - range * touch;
        tmp = (tmp * (tmp - dx) < 0) ? 0 : tmp - dx;
        x = (float) (range - range * Math.pow(1 + tmp / (range * touch), -touch));
        if (negative) x *= -1;
        return x;
    }

    private static double angle(double x, double y) {
        if (x == 0) {
            if (y == 0) return 0;
            return (y > 0 ? 1 : -1) * Math.PI / 2;
        } else {
            double a = Math.atan(y / x);
            if (x < 0) a += (a > 0 ? -1 : 1) * Math.PI;
            return a;
        }
    }

    private static class DampingCore {
        private float t; //劲度系数
        private float f; //摩擦系数
        public float x; //位置
        public float v; //速度

        public DampingCore(float t, float f) {
            this.t = t;
            this.f = f;
            this.x = 0;
            this.v = 0;
        }

        public void time(long t) {
            float x = this.x;
            float v = this.v;
            this.x += v * t;
            this.v += (-this.t * x - this.f * v) * t;
        }

        public boolean is(float x, float v) {
            return Math.abs(this.x) <= Math.abs(x) && Math.abs(this.v) <= Math.abs(v);
        }
    }
}
