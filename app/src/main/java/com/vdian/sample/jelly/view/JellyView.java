package com.vdian.sample.jelly.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.vdian.uikit.view.TouchController;

/**
 * Created by zhangliang on 17/12/1.
 */
public class JellyView extends View implements TouchController.TouchListener {
    private static final float T = 0.00016f;
    private static final float F = 0.004f;
    private static final float D = 2f;
    private static final float P = 0.346f;

    private long t;
    private DampingCore x;
    private DampingCore y;
    private TouchController delegate;
    private Path path;
    private Paint paint;

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
        t = 0;
        x = new DampingCore(T, F);
        y = new DampingCore(T, F);
        delegate = new TouchController(this);
        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Boolean b = delegate.dispatchTouchEvent(event);
        if (b != null) return b;
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Boolean b = delegate.onTouchEvent(event);
        if (b != null) return b;
        return super.onTouchEvent(event);
    }

    @Override
    public boolean down(float downX, float downY) {
        return true;
    }

    @Override
    public boolean move(float moveX, float moveY) {
        float range = ((getContext().getResources().getDisplayMetrics().widthPixels + getContext().getResources().getDisplayMetrics().heightPixels) / 2) / D;
        float touch = P;
        x.x = overflow(range, touch, x.x, -moveX);
        y.x = overflow(range, touch, y.x, -moveY);
        invalidate();
        return true;
    }

    @Override
    public void up(float velocityX, float velocityY) {
        x.v = velocityX / D;
        y.v = velocityY / D;
        invalidate();
    }

    @Override
    public void cancel() {
        up(0, 0);
    }

    private static float overflow(float range, float touch, float x, float dx) {
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

    @Override
    public void computeScroll() {
        super.computeScroll();
        long dt = AnimationUtils.currentAnimationTimeMillis() - t;
        t += dt;
        if (!delegate.isTouch()) {
            if (dt > 0 && dt < 500) {
                while (dt > 0) {
                    x.time(1);
                    y.time(1);
                    dt--;
                }
            }
            if (!x.is(1, 0.001f) || !y.is(1, 0.001f)) postInvalidate();
            else {
                x.x = 0;
                x.v = 0;
                y.x = 0;
                y.v = 0;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float K = 0.552f;
        super.onDraw(canvas);
        int width = getWidth(), height = getHeight();
        int unit = Math.min(width, height) / 2;
        double angle = angle(x.x, y.x);
        double scalar = Math.sqrt(x.x * x.x + y.x * y.x);
        canvas.translate(width / 2, height / 2);
        canvas.rotate((float) Math.toDegrees(angle));
        float x3 = (float) scalar;
        float x2 = 0.618f * x3;
        float x1 = 0.618f * x2;
        x1 -= unit;
        x3 += unit;
        float px1, py1, px2, py2, px3, py3, px4, py4, px5, py5, px6, py6, px7, py7;
        px1 = x1;
        py1 = 0;
        px2 = x1;
        py2 = K * unit;
        px3 = x2 - K * (x2 - x1);
        py3 = unit;
        px4 = x2;
        py4 = unit;
        px5 = x2 + K * (x3 - x2);
        py5 = unit;
        px6 = x3;
        py6 = K * unit;
        px7 = x3;
        py7 = 0;
        path.reset();
        path.moveTo(px1, py1);
        path.cubicTo(px2, py2, px3, py3, px4, py4);
        path.cubicTo(px5, py5, px6, py6, px7, py7);
        path.cubicTo(px6, -py6, px5, -py5, px4, -py4);
        path.cubicTo(px3, -py3, px2, -py2, px1, -py1);
        path.close();
        canvas.drawPath(path, paint);
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
        private float t;
        private float f;
        public float x;
        public float v;

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
