package com.vdian.uikit.view;

import android.view.View;
import android.view.animation.AnimationUtils;

/**
 * 过渡属性控制器
 *
 * @author YangTao
 */
public class TransitionController {
    private boolean resetTime = true; //是否重置动画时间
    private long lastTime = 0; //动画计时器
    private float mValue; //属性值，[0,1]
    private float mTarget; //目标值，[0,1]
    private View mView; //过渡控件
    private TransitionListener mTransitionListener; //属性效果实现器

    public TransitionController(TransitionListener listener, View view) {
        this(listener, view, -1);
    }

    public TransitionController(TransitionListener listener, View view, float value) {
        if (value < 0 || value > 1) value = 0;
        mTransitionListener = listener;
        mView = view;
        mValue = value;
        mTarget = value;
    }

    /**
     * 初始化显示，初始值通过构造器设置
     */
    public void initDisplay() {
        mTransitionListener.display(mView, mValue);
    }

    /**
     * 获取属性值
     *
     * @return
     */
    public float getValue() {
        return mValue;
    }

    /**
     * 突变属性值（不建议通过此方法初始化）
     *
     * @param value
     */
    public void setValue(float value) {
        if (value < 0 || value > 1) return;
        mTarget = value;
        mValue = value;
        resetTime = true;
        mTransitionListener.display(mView, mValue); //嵌套时注意不要无限循环
    }

    /**
     * 获取目标值
     *
     * @return
     */
    public float getTarget() {
        return mTarget;
    }

    /**
     * 过渡属性值
     *
     * @param target
     */
    public void setTarget(float target) {
        if (target < 0 || target > 1) return;
        mTarget = target;
        if (mValue == mTarget) resetTime = true;
        else mView.invalidate();
    }

    /**
     * view同名调用，添加
     */
    public void computeScroll() {
        if (mValue != mTarget) {
            if (resetTime) {
                lastTime = AnimationUtils.currentAnimationTimeMillis(); //重置时间轴
                resetTime = false;
            } else {
                long dTime = AnimationUtils.currentAnimationTimeMillis() - lastTime;
                if (dTime > 0) {
                    lastTime += dTime;
                    float dValue = reduce(dTime) * mTransitionListener.speed(mValue, mTarget);
                    if (mValue < mTarget) {
                        mValue += dValue;
                        if (mValue >= mTarget) {
                            mValue = mTarget;
                            resetTime = true;
                        }
                    } else {
                        mValue -= dValue;
                        if (mValue <= mTarget) {
                            mValue = mTarget;
                            resetTime = true;
                        }
                    }
                    mTransitionListener.display(mView, mValue); //属性显示
                }
            }
            mView.postInvalidate(); //循环刷新
        }
    }

    /**
     * 缩减刷新间隔，覆盖以减缓动画卡顿
     *
     * @param time
     * @return
     */
    protected long reduce(long time) {
        return time;
    }

    public interface TransitionListener {
        float speed(float value, float target);

        void display(View self, float value);
    }
}
