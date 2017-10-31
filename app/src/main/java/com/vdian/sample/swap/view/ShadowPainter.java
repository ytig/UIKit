package com.vdian.sample.swap.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.SparseArray;
import android.view.animation.AnimationUtils;

import com.vdian.uikit.util.task.Task;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by zhangliang on 17/6/2.
 */
public class ShadowPainter {
    private static List<Shadow> shadows = new ArrayList<>(); //位图缓存
    private static Paint paint = new Paint(); //画笔缓存
    private static RectF rectF = new RectF(); //位置缓存
    private static Filter filter = new Filter(255); //滤镜缓存

    /**
     * 绘制阴影（硬件加速!性能）
     *
     * @param canvas
     * @param shadow
     * @param x
     * @param y
     */
    public static void drawGPU(Canvas canvas, Shadow shadow, float x, float y) {
        canvas.save();
        canvas.translate(x - shadow.left, y - shadow.top);
        shadow.draw(canvas);
        canvas.restore();
    }

    /**
     * 绘制阴影（双缓冲!内存）
     *
     * @param canvas
     * @param shadow
     * @param x
     * @param y
     */
    public static void drawDB(Canvas canvas, Shadow shadow, float x, float y) {
        Bitmap bitmap = obtain(shadow).bitmap;
        paint.setColor(Color.rgb(Color.red(shadow.color), Color.green(shadow.color), Color.blue(shadow.color)));
        canvas.drawBitmap(bitmap, x - shadow.left, y - shadow.top, paint);
        recycle();
    }

    /**
     * 绘制阴影（双缓冲.9!效果）
     *
     * @param canvas
     * @param shadow
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public static void drawNP(Canvas canvas, Shadow shadow, float x, float y, float w, float h) {
        Shadow s = obtain(shadow);
        if (!s.patch()) return; //意外状况
        NinePatch patch = s.patch;
        canvas.save();
        canvas.translate(x - shadow.left, y - shadow.top);
        rectF.set(0, 0, shadow.left + w + shadow.right, shadow.top + h + shadow.bottom);
        patch.setPaint(filter.obtain(Color.rgb(Color.red(shadow.color), Color.green(shadow.color), Color.blue(shadow.color))));
        patch.draw(canvas, rectF);
        patch.setPaint(null);
        canvas.restore();
        recycle();
    }

    private static Shadow obtain(Shadow shadow) {
        Shadow s = null;
        for (Shadow tmp : shadows) {
            if (Color.alpha(shadow.color) != Color.alpha(tmp.color)) continue;
            if (shadow.width != tmp.width) continue;
            if (shadow.height != tmp.height) continue;
            if (shadow.left != tmp.left) continue;
            if (shadow.top != tmp.top) continue;
            if (shadow.right != tmp.right) continue;
            if (shadow.bottom != tmp.bottom) continue;
            if (shadow.radius != tmp.radius) continue;
            if (shadow.interpolator != tmp.interpolator) continue;
            s = tmp; //使用缓存
            break;
        }
        if (s == null) {
            s = Shadow.obtain(shadow);
            s.color = Color.argb(Color.alpha(s.color), 0, 0, 0);
            s.buffer();
            shadows.add(s);
        }
        return s;
    }

    private static void recycle() {
        for (int size = 0, i = shadows.size() - 1; i >= 0; i--) {
            if (size > 1024 * 1024L) shadows.remove(i).recycle(); //溢出回收
            else size += shadows.get(i).bitmap.getByteCount();
        }
    }

    public static class Shadow {
        private static Queue<Shadow> cache; //对象缓存
        private static RectF rectF; //位置缓存
        private static Paint paint; //画笔缓存

        static {
            cache = new LinkedList<>();
            rectF = new RectF();
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN)); //阴影混合模式
        }

        public int color; //内色
        public int width; //内宽
        public int height; //内高
        public int left; //左长
        public int top; //上长
        public int right; //右长
        public int bottom; //下长
        public float radius; //内弧
        public double interpolator; //收敛
        private Bitmap bitmap; //双缓冲
        private NinePatch patch; //九宫图

        /**
         * 创建对象
         *
         * @return
         */
        public static Shadow obtain() {
            return cache.size() > 0 ? cache.remove() : new Shadow();
        }

        /**
         * 复制对象
         *
         * @param shadow
         * @return
         */
        public static Shadow obtain(Shadow shadow) {
            Shadow s = obtain();
            s.color = shadow.color;
            s.width = shadow.width;
            s.height = shadow.height;
            s.left = shadow.left;
            s.top = shadow.top;
            s.right = shadow.right;
            s.bottom = shadow.bottom;
            s.radius = shadow.radius;
            s.interpolator = shadow.interpolator;
            return s;
        }

        /**
         * 回收对象
         */
        public void recycle() {
            color = 0;
            width = 0;
            height = 0;
            left = 0;
            top = 0;
            right = 0;
            bottom = 0;
            radius = 0;
            interpolator = 0;
            if (bitmap != null) {
                new Recycler(bitmap).publish();
                bitmap = null;
            }
            patch = null;
            cache.add(this);
        }

        private void draw(Canvas canvas) {
            int max = Math.max(Math.max(Math.max(left, top), right), bottom);
            double tmp = 0;
            for (int i = max; i >= 1 && tmp < 1; i--) {
                rectF.set(((float) left) * (max - i) / max, ((float) top) * (max - i) / max, width + left + ((float) right) * i / max, height + top + ((float) bottom) * i / max);
                double a = ((Math.pow((1 - (i - 1d) / max), interpolator)) - tmp) / (1 - tmp);
                tmp = a + tmp - tmp * a;
                paint.setColor(color);
                paint.setAlpha((int) (paint.getAlpha() * a));
                float r = radius * (rectF.width() / width + rectF.height() / height) / 2;
                canvas.drawRoundRect(rectF, r, r, paint);
            }
        }

        private void buffer() {
            patch = null;
            bitmap = Bitmap.createBitmap(width + left + right, height + top + bottom, Bitmap.Config.ALPHA_8);
            draw(new Canvas(bitmap));
        }

        private boolean patch() {
            if (bitmap == null || bitmap.isRecycled()) return false;
            if (patch == null) {
                int[] xDivs = new int[]{left + (int) radius, left + width - (int) radius};
                int[] yDivs = new int[]{top + (int) radius, top + height - (int) radius};
                int[] colors = new int[]{0x00000001, 0x00000001, 0x00000001, 0x00000001, 0x00000001, 0x00000001, 0x00000001, 0x00000001, 0x00000001};
                int capacity = 4 + (7 + xDivs.length + yDivs.length + colors.length) * 4;
                ByteBuffer byteBuffer = ByteBuffer.allocate(capacity).order(ByteOrder.nativeOrder());
                byteBuffer.put(Integer.valueOf(1).byteValue()); //head
                byteBuffer.put(Integer.valueOf(xDivs.length).byteValue()); //xDivs.length
                byteBuffer.put(Integer.valueOf(yDivs.length).byteValue()); //yDivs.length
                byteBuffer.put(Integer.valueOf(colors.length).byteValue()); //colors.length
                byteBuffer.putInt(0); //skip
                byteBuffer.putInt(0); //skip
                byteBuffer.putInt(0); //padding.left
                byteBuffer.putInt(0); //padding.right
                byteBuffer.putInt(0); //padding.top
                byteBuffer.putInt(0); //padding.bottom
                byteBuffer.putInt(0); //skip
                for (int div : xDivs) byteBuffer.putInt(div); //xDivs
                for (int div : yDivs) byteBuffer.putInt(div); //yDivs
                for (int color : colors) byteBuffer.putInt(color); //colors
                patch = new NinePatch(bitmap, byteBuffer.array(), null);
            }
            return true;
        }

        private static class Recycler extends Task {
            private Bitmap bitmap; //回收对象

            private Recycler(Bitmap bitmap) {
                this.bitmap = bitmap;
            }

            @Override
            protected void execute() {
                if (bitmap != null) {
                    if (!bitmap.isRecycled()) bitmap.recycle();
                    bitmap = null;
                }
            }
        }
    }

    private static class Filter extends IntCache<Paint> {
        private int limit; //缓存上限

        public Filter(int size) {
            limit = size;
        }

        @Override
        protected Paint newValue(int key) {
            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(new float[]{
                    0, 0, 0, 0, Color.red(key),
                    0, 0, 0, 0, Color.green(key),
                    0, 0, 0, 0, Color.blue(key),
                    0, 0, 0, Color.alpha(key) / 255f, 0,
            })));
            return paint;
        }

        @Override
        protected boolean removeValue(SparseArray<Paint> cache) {
            return cache.size() > limit;
        }
    }
}

abstract class IntCache<Value> {
    private SparseArray<Value> values = new SparseArray<>(); //对象缓存
    private SparseArray<Time> times = new SparseArray<>(); //时间缓存

    public Value obtain(int key) {
        Value value = values.get(key);
        Time time = times.get(key);
        if (value == null || time == null) {
            value = newValue(key);
            time = new Time();
            values.put(key, value);
            times.put(key, time);
            while (removeValue(values)) {
                int k = 0;
                long t = Long.MAX_VALUE;
                for (int i = times.size() - 1; i >= 0; i--) {
                    long dt = (times.valueAt(i).time - t);
                    if (dt <= 0) {
                        t += dt;
                        k = times.keyAt(i);
                    }
                }
                times.remove(k);
                values.remove(k);
                if (values.size() <= 0) break;
            }
        } else time.update();
        return value;
    }

    protected abstract Value newValue(int key); //新建对象

    protected abstract boolean removeValue(SparseArray<Value> cache); //溢出判断

    private static class Time {
        public long time; //最新获取时间戳

        public Time() {
            update();
        }

        public void update() {
            time = AnimationUtils.currentAnimationTimeMillis();
        }
    }
}
