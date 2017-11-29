package com.vdian.salon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vdian.uikit.util.RectUtil;
import com.vdian.uikit.view.TransitionController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by zhangliang on 17/2/13.
 */
public abstract class SalonMaster extends SalonView {
    private List<WeakReference<View>> mViews = new ArrayList<>(); //共享元素
    private boolean isShow = false; //是否已显示
    private float mLeft = 0; //裁剪左边
    private float mTop = 0; //裁剪顶边
    private float mRight = 0; //裁剪右边
    private float mBottom = 0; //裁剪底边
    private Rect mRect = new Rect(); //我的位置信息
    private Rect yRect = new Rect(); //你的位置信息
    private ColorDrawable mBg = new ColorDrawable(Color.rgb(0, 0, 0)); //背景图
    private SalonIndicator mIndicator; //页号控件
    private SalonPopup mPopup; //弹窗控件

    public SalonMaster(ViewGroup root) {
        super(root.getContext());
        setFocusable(true);
        setFocusableInTouchMode(true);
        setWillNotDraw(false);
        permitTouchEvent(false); //禁止触控
        setVisibility(View.INVISIBLE); //隐藏控件
        setBackgroundDrawable(mBg);
        mIndicator = new SalonIndicator(root.getContext());
        mIndicator.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mIndicator); //添加页号
        mPopup = new SalonPopup(root.getContext());
        mPopup.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mPopup); //添加弹窗
        setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        root.addView(this);
    }

    /**
     * 显示（渐变动画）
     *
     * @param index 当前序号
     * @param urls  链接列表
     */
    @Override
    public void display(int index, List<String> urls) {
        display(index, urls, null);
    }

    /**
     * 显示（共享动画）
     *
     * @param index 当前序号
     * @param urls  链接列表
     * @param views 共享元素
     */
    public void display(int index, List<String> urls, List<View> views) {
        if (didLayout && !isShow) {
            isShow = true;
            mViews.clear();
            if (views != null) {
                for (View view : views) mViews.add(new WeakReference<>(view));
            }
            super.display(index, urls);
            animation(true); //开启动画
            requestFocus(); //获取焦点
            ViewParent parent = getParent();
            if (parent != null) {
                parent.bringChildToFront(this); //置顶控件
                requestLayout();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean consume = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mPopup.isShow) {
                    consume = true;
                    if (!isTouch()) mPopup.onClick(mPopup);
                    break;
                }
                if (isShow) {
                    consume = true;
                    if (!isTouch() && permitTouchEvent() && mOffset == 0) animation(false); //关闭动画
                    break;
                }
                break;
        }
        return consume || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onUpdate(float index, int size) {
        mIndicator.display(index, size); //更新页号信息
    }

    @Override
    protected void onClick(float x, float y) {
        if (mOffset != 0) return;
        animation(false); //关闭动画
    }

    @Override
    protected void onDoubleClick(float x, float y) {
        doZoom = true; //缩放动画
        zoomX = x;
        zoomY = y;
    }

    @Override
    protected void onLongClick(float x, float y) {
        if (mOffset != 0) return;
        if (mPopup.display(mUrlIndex, mUrls.get(mUrlIndex), mExhibits[mChildIndex])) {
            permitTouchEvent(false); //弹窗取消触控
            permitTouchEvent(true);
        }
    }

    @Override
    protected View onCreateExhibit(Context context) {
        return new SalonFrame(context, onCreateImage(context)); //创建展览品
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipRect(mLeft, mTop, mRight, mBottom); //区域裁剪
        super.onDraw(canvas);
    }

    private void animation(boolean show) {
        if (mAnimator != null) {
            mAnimator.cancel(); //停止当前动画
            mAnimator = null;
        }
        final List<View> decorations = new ArrayList<>(); //遍历装饰品
        Queue<View> queue = new LinkedList<>();
        queue.add(SalonMaster.this);
        while (queue.size() > 0) {
            View target = queue.remove();
            if (target instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) target).getChildCount(); i++) {
                    View child = ((ViewGroup) target).getChildAt(i);
                    if (child != null) queue.add(child);
                }
            }
            if (target instanceof SalonDecoration) decorations.add(target);
        }
        float ratio = 0;
        Rect rect = null;
        if (mUrlIndex >= 0 && mUrlIndex < mViews.size()) {
            WeakReference<View> reference = mViews.get(mUrlIndex);
            if (reference != null) {
                View view = reference.get();
                if (view != null) rect = getRect(view); //获取共享元素位置
            }
        }
        if (rect != null) ratio = getRatio(mUrls.get(mUrlIndex)); //获取宽高比例
        if (ratio <= 0 || rect == null) {
            if (show) { //渐变开启动画
                final float sScale = 1f;
                final float eScale = 1f;
                final float sDx = 0;
                final float eDx = 0;
                final float sDy = 0;
                final float eDy = 0;
                mAnimator = new SalonAnimation(300L, null) {
                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        if (value < 0) value = 0;
                        if (value >= 1) value = 1;
                        else value = (float) (Math.cos((value + 1) * Math.PI) / 2.0f) + 0.5f;
                        setAlpha(value);
                        mBg.setAlpha(255);
                        mScales[mChildIndex] = sScale * (1 - value) + eScale * value;
                        mDxs[mChildIndex] = sDx * (1 - value) + eDx * value;
                        mDys[mChildIndex] = sDy * (1 - value) + eDy * value;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                        mLeft = 0;
                        mTop = 0;
                        mRight = getWidth();
                        mBottom = getHeight();
                        invalidate();
                        for (View view : decorations) view.setAlpha(1);
                    }

                    @Override
                    protected void onStart() {
                        setVisibility(View.VISIBLE);
                        super.onStart();
                    }

                    @Override
                    protected void onEnd() {
                        super.onEnd();
                        permitTouchEvent(true); //开启动画结束允许触控
                    }
                }.start();
            } else { //渐变关闭动画
                final float sScale = mScales[mChildIndex];
                final float eScale = 1f;
                final float sDx = mDxs[mChildIndex];
                final float eDx = 0;
                final float sDy = mDys[mChildIndex];
                final float eDy = 0;
                mAnimator = new SalonAnimation(300L, null) {
                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        if (value < 0) value = 0;
                        if (value >= 1) value = 1;
                        else value = (float) (Math.cos((value + 1) * Math.PI) / 2.0f) + 0.5f;
                        setAlpha(1 - value);
                        mBg.setAlpha(255);
                        mScales[mChildIndex] = sScale * (1 - value) + eScale * value;
                        mDxs[mChildIndex] = sDx * (1 - value) + eDx * value;
                        mDys[mChildIndex] = sDy * (1 - value) + eDy * value;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                        mLeft = 0;
                        mTop = 0;
                        mRight = getWidth();
                        mBottom = getHeight();
                        invalidate();
                        for (View view : decorations) view.setAlpha(1);
                    }

                    @Override
                    protected void onStart() {
                        permitTouchEvent(false); //关闭动画开始禁止触控
                        super.onStart();
                    }

                    @Override
                    protected void onEnd() {
                        super.onEnd();
                        isShow = false;
                        setVisibility(View.INVISIBLE);
                        for (int d = 0; d <= CHILD_SIZE / 2; d++) { //释放内存
                            int l = (CHILD_SIZE + mChildIndex - d) % CHILD_SIZE;
                            if (mUrlIndex - d >= 0 && mUrlIndex - d < mUrls.size())
                                ((SalonExhibit) mExhibits[l]).loadUrl(null);
                            if (d == 0) continue;
                            int r = (CHILD_SIZE + mChildIndex + d) % CHILD_SIZE;
                            if (mUrlIndex + d >= 0 && mUrlIndex + d < mUrls.size())
                                ((SalonExhibit) mExhibits[r]).loadUrl(null);
                        }
                    }
                }.start();
            }
        } else {
            float scale, x, y;
            if (rect.width() >= ratio * rect.height())
                scale = ((float) rect.width()) / ((ratio * getHeight() > getWidth()) ? getWidth() : getHeight() * ratio);
            else
                scale = ((float) rect.height()) / ((ratio * getHeight() > getWidth()) ? getWidth() / ratio : getHeight());
            x = (rect.left + rect.right - getWidth()) / 2;
            y = (rect.top + rect.bottom - getHeight()) / 2;
            if (show) { //共享开启动画
                final float sScale = scale;
                final float eScale = 1;
                final float sDx = x;
                final float eDx = 0;
                final float sDy = y;
                final float eDy = 0;
                final float sLeft = rect.left;
                final float eLeft = 0;
                final float sTop = rect.top;
                final float eTop = 0;
                final float sRight = rect.right;
                final float eRight = getWidth();
                final float sBottom = rect.bottom;
                final float eBottom = getHeight();
                mAnimator = new SalonAnimation(300L, null) {
                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        float value1 = (value - 0f) / 0.2f;
                        if (value1 < 0) value1 = 0;
                        if (value1 > 1) value1 = 1;
                        setAlpha(value1);
                        float value2 = (value - 0.2f) / 0.8f;
                        if (value2 < 0) value2 = 0;
                        if (value2 >= 1) value2 = 1;
                        else value2 = (float) (Math.cos((value2 + 1) * Math.PI) / 2.0f) + 0.5f;
                        mBg.setAlpha((int) (255 * value2));
                        mScales[mChildIndex] = sScale * (1 - value2) + eScale * value2;
                        mDxs[mChildIndex] = sDx * (1 - value2) + eDx * value2;
                        mDys[mChildIndex] = sDy * (1 - value2) + eDy * value2;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                        float value3 = (value - 0.2f) / 0.6f;
                        if (value3 < 0) value3 = 0;
                        if (value3 >= 1) value3 = 1;
                        else value3 = (float) (Math.cos((value3 + 1) * Math.PI) / 2.0f) + 0.5f;
                        mLeft = sLeft * (1 - value3) + eLeft * value3;
                        mTop = sTop * (1 - value3) + eTop * value3;
                        mRight = sRight * (1 - value3) + eRight * value3;
                        mBottom = sBottom * (1 - value3) + eBottom * value3;
                        invalidate();
                        float value4 = (value - 0.8f) / 0.2f;
                        if (value4 < 0) value4 = 0;
                        if (value4 > 1) value4 = 1;
                        for (View view : decorations) view.setAlpha(value4);
                    }

                    @Override
                    protected void onStart() {
                        setVisibility(View.VISIBLE);
                        super.onStart();
                    }

                    @Override
                    protected void onEnd() {
                        super.onEnd();
                        permitTouchEvent(true); //开启动画结束允许触控
                    }
                }.start();
            } else { //共享关闭动画
                final float sScale = mScales[mChildIndex];
                final float eScale = scale;
                final float sDx = mDxs[mChildIndex];
                final float eDx = x;
                final float sDy = mDys[mChildIndex];
                final float eDy = y;
                final float sLeft = 0;
                final float eLeft = rect.left;
                final float sTop = 0;
                final float eTop = rect.top;
                final float sRight = getWidth();
                final float eRight = rect.right;
                final float sBottom = getHeight();
                final float eBottom = rect.bottom;
                mAnimator = new SalonAnimation(300L, null) {
                    @Override
                    protected void display(SalonAnimation animation, float value, long time) {
                        float value1 = (value - 0.8f) / 0.2f;
                        if (value1 < 0) value1 = 0;
                        if (value1 > 1) value1 = 1;
                        setAlpha(1 - value1);
                        float value2 = (value - 0f) / 0.8f;
                        if (value2 < 0) value2 = 0;
                        if (value2 >= 1) value2 = 1;
                        else value2 = (float) (Math.cos((value2 + 1) * Math.PI) / 2.0f) + 0.5f;
                        mBg.setAlpha((int) (255 * (1 - value2)));
                        mScales[mChildIndex] = sScale * (1 - value2) + eScale * value2;
                        mDxs[mChildIndex] = sDx * (1 - value2) + eDx * value2;
                        mDys[mChildIndex] = sDy * (1 - value2) + eDy * value2;
                        ((SalonExhibit) mExhibits[mChildIndex]).setMatrix(mDxs[mChildIndex], mDys[mChildIndex], mScales[mChildIndex]);
                        float value3 = (value - 0.2f) / 0.6f;
                        if (value3 < 0) value3 = 0;
                        if (value3 >= 1) value3 = 1;
                        else value3 = (float) (Math.cos((value3 + 1) * Math.PI) / 2.0f) + 0.5f;
                        mLeft = sLeft * (1 - value3) + eLeft * value3;
                        mTop = sTop * (1 - value3) + eTop * value3;
                        mRight = sRight * (1 - value3) + eRight * value3;
                        mBottom = sBottom * (1 - value3) + eBottom * value3;
                        invalidate();
                        float value4 = (value - 0f) / 0.2f;
                        if (value4 < 0) value4 = 0;
                        if (value4 > 1) value4 = 1;
                        for (View view : decorations) view.setAlpha(1 - value4);
                    }

                    @Override
                    protected void onStart() {
                        permitTouchEvent(false); //关闭动画开始禁止触控
                        super.onStart();
                    }

                    @Override
                    protected void onEnd() {
                        super.onEnd();
                        isShow = false;
                        setVisibility(View.INVISIBLE);
                        for (int d = 0; d <= CHILD_SIZE / 2; d++) { //释放内存
                            int l = (CHILD_SIZE + mChildIndex - d) % CHILD_SIZE;
                            if (mUrlIndex - d >= 0 && mUrlIndex - d < mUrls.size())
                                ((SalonExhibit) mExhibits[l]).loadUrl(null);
                            if (d == 0) continue;
                            int r = (CHILD_SIZE + mChildIndex + d) % CHILD_SIZE;
                            if (mUrlIndex + d >= 0 && mUrlIndex + d < mUrls.size())
                                ((SalonExhibit) mExhibits[r]).loadUrl(null);
                        }
                    }
                }.start();
            }
        }
    }

    /**
     * 创建图片控件
     *
     * @param context 上下文
     * @return
     */
    protected abstract View onCreateImage(Context context);

    /**
     * 根据链接获取图片宽高比（共享动画使用）
     *
     * @param url 链接
     * @return
     */
    protected abstract float getRatio(String url);

    /**
     * 根据控件获取位置信息（共享动画使用）
     *
     * @param view
     * @return
     */
    protected Rect getRect(View view) {
        if (ViewCompat.isAttachedToWindow(view)) {
            RectUtil.measure(view, yRect);
            RectUtil.measure(this, mRect);
            yRect.left -= mRect.left;
            yRect.right -= mRect.left;
            yRect.top -= mRect.top;
            yRect.bottom -= mRect.top;
            return yRect;
        }
        return null;
    }

    public interface SalonDecoration {
    }

    public interface SalonImage {
        float RATIO_LOAD = 0; //加载中宽高比

        float RATIO_FAIL = -1; //加载失败宽高比

        void loadUrl(String url);

        float getRatio();
    }

    private static class SalonFrame extends RelativeLayout implements SalonExhibit {
        private View mImage; //图片控件
        private LoadView mLoad; //加载环控件
        private FailView mFail; //加载失败控件
        private Matrix mMatrix = new Matrix(); //绘制矩阵

        public SalonFrame(Context context, View image) {
            super(context);
            setWillNotDraw(false);
            mImage = image;
            mImage.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(mImage);
            mLoad = new LoadView(context);
            mLoad.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(mLoad);
            mFail = new FailView(context);
            mFail.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            addView(mFail);
        }

        @Override
        public void loadUrl(String url) {
            ((SalonImage) mImage).loadUrl(url);
            if (url == null || url.equals("")) {
                mLoad.display(false);
                mFail.display(false);
            } else {
                mLoad.display(true);
                mFail.display(true);
            }
        }

        @Override
        public float getRatio() {
            return ((SalonImage) mImage).getRatio();
        }

        @Override
        public void setMatrix(float dx, float dy, float scale) {
            mMatrix.reset();
            mMatrix.setScale(scale, scale, getWidth() / 2, getHeight() / 2);
            mMatrix.postTranslate(dx, dy);
            invalidate(); //更新绘制矩阵
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.concat(mMatrix);
            super.onDraw(canvas);
        }

        private class LoadView extends View implements SalonDecoration, TransitionController.TransitionListener {
            private static final float STROKE_WIDTH = 1.5f; //圆环边宽
            private static final float CIRCLE_RADIUS = 14; //圆环半径

            private float mProgress = 0; //圆环进度
            private float mRotation = 0; //圆环旋转度
            private Paint sPaint; //深色画笔
            private Paint lPaint; //浅色画笔
            private RectF mRectF; //圆环位置
            private Matrix mMatrix; //绘制矩阵
            private TransitionController mAnimation; //动画控制器

            public LoadView(Context context) {
                super(context);
                sPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sPaint.setColor(Color.rgb(255, 255, 255));
                sPaint.setStyle(Paint.Style.STROKE);
                sPaint.setStrokeWidth(STROKE_WIDTH * getContext().getResources().getDisplayMetrics().density);
                lPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                lPaint.setColor(Color.rgb(0, 0, 0));
                lPaint.setStyle(Paint.Style.STROKE);
                lPaint.setStrokeWidth(STROKE_WIDTH * getContext().getResources().getDisplayMetrics().density);
                mRectF = new RectF();
                mMatrix = new Matrix();
                mAnimation = new TransitionController(this, this) {
                    @Override
                    protected long reduce(long time) {
                        mProgress += (time / 2000f);
                        while (mProgress < 0) mProgress += 1;
                        while (mProgress >= 1) mProgress -= 1;
                        mRotation += 360 * (time / 1236f);
                        while (mRotation < 0) mRotation += 360;
                        while (mRotation >= 360) mRotation -= 360;
                        return super.reduce(time);
                    }
                };
            }

            public void display(boolean show) {
                if (!show) mAnimation.setValue(0);
                else {
                    mAnimation.setValue(1);
                    mAnimation.setTarget(0);
                }
            }

            @Override
            public void computeScroll() {
                super.computeScroll();
                mAnimation.computeScroll();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float value = mAnimation.getValue();
                if (value > 0) {
                    mMatrix.reset();
                    mMatrix.setScale(value + 0.618f * (1 - value), value + 0.618f * (1 - value), getWidth() / 2, getHeight() / 2);
                    mMatrix.postRotate(mRotation, getWidth() / 2, getHeight() / 2);
                    canvas.concat(mMatrix);
                    float radius = CIRCLE_RADIUS * getContext().getResources().getDisplayMetrics().density;
                    mRectF.left = getWidth() / 2 - radius;
                    mRectF.top = getHeight() / 2 - radius;
                    mRectF.right = mRectF.left + 2 * radius;
                    mRectF.bottom = mRectF.top + 2 * radius;
                    sPaint.setAlpha((int) (255 * value));
                    lPaint.setAlpha((int) (158 * value));
                    float progress = mProgress < 0.618f ? (mProgress / 0.618f) : ((1 - mProgress) / (1 - 0.618f));
                    if (progress < 0) progress = 0;
                    if (progress >= 1) progress = 1;
                    else progress = (float) (Math.cos((progress + 1) * Math.PI) / 2.0f) + 0.5f;
                    canvas.drawArc(mRectF, 0, 360 * progress, false, sPaint);
                    canvas.drawArc(mRectF, 0, 360 * (progress - 1), false, lPaint);
                }
            }

            @Override
            public float speed(float value, float target) {
                if (getRatio() == SalonImage.RATIO_LOAD) return 0;
                return 1 / 180f;
            }

            @Override
            public void display(View self, float value) {
                invalidate();
            }
        }

        private class FailView extends View implements SalonDecoration, TransitionController.TransitionListener {
            private static final float STROKE_WIDTH = 1.5f; //圆环边宽
            private static final float CIRCLE_RADIUS = 14; //圆环半径

            private Paint sPaint; //深色画笔
            private Paint lPaint; //浅色画笔
            private TransitionController mAnimation; //动画控制器

            public FailView(Context context) {
                super(context);
                sPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sPaint.setColor(Color.rgb(255, 255, 255));
                sPaint.setStyle(Paint.Style.STROKE);
                sPaint.setStrokeWidth(STROKE_WIDTH * getContext().getResources().getDisplayMetrics().density);
                sPaint.setTypeface(Typeface.SERIF);
                sPaint.setTextAlign(Paint.Align.CENTER);
                sPaint.setTextSize(CIRCLE_RADIUS * getContext().getResources().getDisplayMetrics().density);
                lPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                lPaint.setColor(Color.argb(158, 0, 0, 0));
                lPaint.setStyle(Paint.Style.STROKE);
                lPaint.setStrokeWidth(STROKE_WIDTH * getContext().getResources().getDisplayMetrics().density);
                lPaint.setTypeface(Typeface.SERIF);
                lPaint.setTextAlign(Paint.Align.CENTER);
                lPaint.setTextSize(CIRCLE_RADIUS * getContext().getResources().getDisplayMetrics().density);
                mAnimation = new TransitionController(this, this);
            }

            public void display(boolean show) {
                if (!show) mAnimation.setValue(0);
                else {
                    mAnimation.setValue(0);
                    mAnimation.setTarget(1);
                }
            }

            @Override
            public void computeScroll() {
                super.computeScroll();
                mAnimation.computeScroll();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (getRatio() == SalonImage.RATIO_FAIL) {
                    float value = mAnimation.getValue();
                    if (value > 0.309f) {
                        canvas.translate(getWidth() / 2, getHeight() / 2);
                        float radius = CIRCLE_RADIUS * getContext().getResources().getDisplayMetrics().density;
                        float lvalue = Math.max(0, Math.min(1, (value - 0.809f) / 0.191f));
                        if (lvalue != 0) {
                            lvalue *= (STROKE_WIDTH * getContext().getResources().getDisplayMetrics().density / 0.618f);
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            canvas.translate(0.618f * lvalue, lvalue);
                            canvas.drawCircle(0, 0, radius, lPaint);
                            canvas.drawText("!", 0, -(lPaint.getFontMetrics().top + lPaint.getFontMetrics().bottom) / 2, lPaint);
                            canvas.restore();
                        }
                        float svalue = Math.max(0, Math.min(1, (value - 0.309f) / 0.5f));
                        sPaint.setAlpha((int) (255 * svalue));
                        canvas.scale((svalue + 0.618f * (1 - svalue)), (svalue + 0.618f * (1 - svalue)));
                        canvas.drawCircle(0, 0, radius, sPaint);
                        canvas.drawText("!", 0, -(sPaint.getFontMetrics().top + sPaint.getFontMetrics().bottom) / 2, sPaint);
                    }
                }
            }

            @Override
            public float speed(float value, float target) {
                if (getRatio() == SalonImage.RATIO_LOAD) return 0;
                return 1 / 360f;
            }

            @Override
            public void display(View self, float value) {
                invalidate();
            }
        }
    }

    private static class SalonIndicator extends View implements SalonDecoration {
        private static final float CIRCLE_RADIUS = 2.8f; //圆点半径
        private static final float CIRCLE_MARGIN = 14; //圆点间距
        private static final float STROKE_WIDTH = 1f; //线条粗细
        private static final float BORDER_PADDING = 36; //边界留白

        private int mSize = 0; //页号总量
        private int mIndex = -1; //当前页号
        private float mProgress = -1; //页号进度
        private Paint sPaint; //深色画笔
        private Paint lPaint; //浅色画笔

        public SalonIndicator(Context context) {
            super(context);
            sPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sPaint.setColor(Color.rgb(255, 255, 255));
            lPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            lPaint.setColor(Color.argb(100, 255, 255, 255));
        }

        public void display(float index, int size) {
            int oSize = mSize;
            int oIndex = mIndex;
            float oProgress = mProgress;
            mSize = size;
            mIndex = size > 0 ? Math.max(0, Math.min(size - 1, (int) (index + 0.5f))) : -1;
            mProgress = size > 1 ? Math.max(0, Math.min(1, index / (size - 1))) : -1;
            if (mSize != oSize || mIndex != oIndex) invalidate();
            else if (overflow() && mProgress != oProgress) invalidate();
        }

        private boolean overflow() {
            return ((mSize - 1) * CIRCLE_MARGIN + CIRCLE_RADIUS) * getContext().getResources().getDisplayMetrics().density > 0.618f * getContext().getResources().getDisplayMetrics().widthPixels;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mSize > 1) {
                float density = getContext().getResources().getDisplayMetrics().density;
                if (!overflow()) {
                    for (int i = 0; i < mSize; i++) {
                        float cx = getWidth() / 2 - (mSize - 1) * CIRCLE_MARGIN * density / 2 + i * CIRCLE_MARGIN * density;
                        float cy = getHeight() - BORDER_PADDING * density;
                        canvas.drawCircle(cx, cy, CIRCLE_RADIUS * density, (i == mIndex) ? sPaint : lPaint);
                    }
                } else {
                    float w = getWidth() - BORDER_PADDING * density;
                    float h = STROKE_WIDTH * density;
                    float l = getWidth() / 2 - w / 2;
                    float t = getHeight() - BORDER_PADDING * density - h / 2;
                    canvas.drawRect(l, t, l + w, t + h, lPaint);
                    canvas.drawCircle(l + w * mProgress, t + h / 2, CIRCLE_RADIUS * density, sPaint);
                }
            }
        }
    }

    private static class SalonPopup extends LinearLayout implements TransitionController.TransitionListener, OnClickListener {
        private static final int CELL_HEIGHT = 50; //按钮高度
        private static final int SMALL_MARGIN = 1; //按钮小间距
        private static final int BIG_MARGIN = 8; //按钮大间距

        private boolean isShow = false; //是否已显示
        private boolean canClick = false; //是否可点击
        private int mHeight = 0; //按钮区域高度
        private Paint mPaint; //蒙层画笔
        private TransitionController mAnimation; //动画控制器
        private PopupAdapter mAdapter; //弹窗适配器

        public SalonPopup(Context context) {
            super(context);
            setWillNotDraw(false);
            setOrientation(VERTICAL);
            setGravity(Gravity.BOTTOM);
            mPaint = new Paint();
            mPaint.setColor(Color.rgb(0, 0, 0));
            mAnimation = new TransitionController(this, this);
            mAnimation.initDisplay();
            setOnClickListener(this, this);
        }

        public boolean display(int index, String url, View view) {
            if (!isShow && mAdapter != null) {
                mAdapter.reset(index, url, view);
                int[] btns = mAdapter.create();
                if (btns != null && btns.length > 0) {
                    isShow = true;
                    canClick = true;
                    float density = getContext().getResources().getDisplayMetrics().density;
                    mHeight = (btns.length + 1) * ((int) (CELL_HEIGHT * density)) + (btns.length - 1) * ((int) (SMALL_MARGIN * density)) + ((int) (BIG_MARGIN * density));
                    removeAllViews();
                    for (int i = 0; i <= btns.length; i++) {
                        if (i != 0) {
                            boolean small = i < btns.length;
                            View divider = new View(getContext()) {
                                @Override
                                public boolean onTouchEvent(MotionEvent event) {
                                    return true;
                                }
                            };
                            divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) ((small ? SMALL_MARGIN : BIG_MARGIN) * density)));
                            divider.setBackgroundColor(Color.parseColor(small ? "#f5e3e3e3" : "#f5ebebeb"));
                            addView(divider);
                        }
                        Integer type = i < btns.length ? btns[i] : null;
                        TextView button = new TextView(getContext());
                        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (CELL_HEIGHT * density)));
                        button.setGravity(Gravity.CENTER);
                        button.setTextColor(Color.parseColor("#585858"));
                        button.setTextSize(18);
                        button.setText(type == null ? mAdapter.exit() : mAdapter.bind(type));
                        StateListDrawable bg = new StateListDrawable();
                        bg.addState(View.PRESSED_ENABLED_STATE_SET, new ColorDrawable(Color.parseColor("#fafafafa")));
                        bg.addState(View.EMPTY_STATE_SET, new ColorDrawable(Color.parseColor("#ffffffff")));
                        button.setBackgroundDrawable(bg);
                        button.setTag(type);
                        button.setOnClickListener(this);
                        addView(button);
                    }
                    setVisibility(View.VISIBLE);
                    mAnimation.setTarget(1);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (canClick && (tag instanceof Integer)) mAdapter.click((Integer) tag);
            canClick = false;
            mAnimation.setTarget(0);
        }

        @Override
        public void computeScroll() {
            super.computeScroll();
            mAnimation.computeScroll();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(0, -mHeight, getWidth(), getHeight() - mHeight, mPaint);
        }

        @Override
        public float speed(float value, float target) {
            return 1 / 300f;
        }

        @Override
        public void display(View self, float value) {
            if ((mAnimation.getValue() == mAnimation.getTarget()) && (mAnimation.getValue() == 0)) {
                isShow = false;
                if (getVisibility() != View.INVISIBLE) setVisibility(View.INVISIBLE);
            }
            setAlpha(value == 0 ? 0 : 1);
            value = (float) (Math.cos((value + 1) * Math.PI) / 2.0f) + 0.5f;
            scrollTo(0, (int) (-mHeight * (1 - value)));
            mPaint.setAlpha((int) (127 * value));
            invalidate();
        }

        private static void setOnClickListener(final View view, final OnClickListener listener) {
            final float limit = 4 * view.getContext().getResources().getDisplayMetrics().density;
            view.setOnTouchListener(new OnTouchListener() {
                float downX = 0;
                float downY = 0;
                boolean canClick = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downX = event.getX();
                            downY = event.getY();
                            canClick = true;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (canClick && (Math.abs(event.getX() - downX) > limit || Math.abs(event.getY() - downY) > limit))
                                canClick = false;
                            break;
                        case MotionEvent.ACTION_UP:
                            if (canClick) listener.onClick(view);
                            break;
                        default:
                            canClick = false;
                            break;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * 设置弹窗适配器
     *
     * @param adapter 弹窗适配器
     */
    public void setPopupAdapter(PopupAdapter adapter) {
        mPopup.mAdapter = adapter;
    }

    public abstract static class PopupAdapter {
        private String mExit; //退出文本内容
        private PopupInfo mInfo = new PopupInfo(); //弹窗上下文信息

        public PopupAdapter() {
            this("取消"); //默认文本
        }

        public PopupAdapter(String exit) {
            mExit = exit;
        }

        private void reset(int index, String url, View exhibit) {
            mInfo.index = index;
            mInfo.url = url;
            mInfo.image = ((SalonFrame) exhibit).mImage;
        }

        private int[] create() {
            return onCreate(mInfo);
        }

        private String bind(int type) {
            return onBind(type, mInfo);
        }

        private void click(int type) {
            onClick(type, mInfo);
        }

        private String exit() {
            return mExit;
        }

        protected abstract int[] onCreate(PopupInfo info);

        protected abstract String onBind(int type, PopupInfo info);

        protected abstract void onClick(int type, PopupInfo info);

        protected static class PopupInfo {
            public int index; //当前链接序号
            public String url; //当前链接内容
            public View image; //当前图片控件
        }
    }
}
