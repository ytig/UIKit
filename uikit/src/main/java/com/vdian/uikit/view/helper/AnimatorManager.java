package com.vdian.uikit.view.helper;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by zhangliang on 16/12/26.
 */
public abstract class AnimatorManager {
    private WeakHashMap<View, HashMap<String, Object>> map = new WeakHashMap<>(); //扩展成员变量

    protected abstract float get(View view);

    protected abstract void set(View view, float value);

    protected abstract Animator create(View view, float value, float target);

    public float getValue(View view) {
        return get(view);
    }

    public void setValue(View view, float value) {
        HashMap<String, Object> expand = map.get(view);
        if (expand != null) {
            expand.remove("target"); //重置目标值
            Object a = expand.remove("animator");
            if (a instanceof Animator && ((Animator) a).isRunning()) ((Animator) a).cancel(); //停止动画
        }
        set(view, value);
    }

    public float getTarget(View view) {
        HashMap<String, Object> expand = map.get(view);
        if (expand != null) {
            Object t = expand.get("target");
            if (t instanceof Float) return (Float) t; //返回目标值
        }
        return get(view); //默认返回属性值
    }

    public void setTarget(View view, float target) {
        float value = get(view);
        HashMap<String, Object> expand = map.get(view);
        if (expand == null) {
            if (value == target) return; //防止重置动画
            expand = new HashMap<>();
            map.put(view, expand);
        } else {
            Object t = expand.get("target");
            if (t instanceof Float) {
                if ((Float) t == target) return; //防止重置动画
            } else {
                if (value == target) return; //防止重置动画
            }
            Object a = expand.get("animator");
            if (a instanceof Animator && ((Animator) a).isRunning()) ((Animator) a).cancel(); //停止动画
        }
        Animator animator = create(view, value, target);
        animator.start(); //开始动画
        expand.put("animator", animator);
        expand.put("target", target);
    }

    public abstract static class BaseManager extends AnimatorManager {
        @Override
        protected final Animator create(View view, float value, float target) {
            return new BaseAnimator(view, value, target);
        }

        protected abstract long duration(View view, float from, float to);

        protected abstract TimeInterpolator interpolator(View view, float from, float to);

        private class BaseAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {
            private WeakReference<View> reference; //执行动画控件

            private BaseAnimator(View view, float from, float to) {
                reference = new WeakReference<>(view);
                setFloatValues(from, to);
                addUpdateListener(this);
                setDuration(duration(view, from, to)); //设置动画时间
                setInterpolator(interpolator(view, from, to)); //设置动画插值
            }

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                View view = reference.get();
                if (view == null) return;
                Object value = animation.getAnimatedValue();
                if (!(value instanceof Float)) return;
                set(view, (Float) value); //更新属性值
            }
        }
    }

    private static class PropertyManager extends BaseManager {
        public static final int X = 1;
        public static final int Y = 2;
        public static final int ALPHA = 3;
        public static final int SCALE = 4;
        public static final int ROTATION = 5;

        private int property;

        public PropertyManager(int property) {
            this.property = property;
        }

        @Override
        protected final float get(View view) {
            switch (property) {
                case X:
                    return view.getTranslationX();
                case Y:
                    return view.getTranslationY();
                case ALPHA:
                    return view.getAlpha();
                case SCALE:
                    return (view.getScaleX() + view.getScaleY()) / 2;
                case ROTATION:
                    return view.getRotation();
            }
            return 0;
        }

        @Override
        protected final void set(View view, float value) {
            switch (property) {
                case X:
                    view.setTranslationX(value);
                    break;
                case Y:
                    view.setTranslationY(value);
                    break;
                case ALPHA:
                    view.setAlpha(value);
                    break;
                case SCALE:
                    view.setScaleX(value);
                    view.setScaleY(value);
                    break;
                case ROTATION:
                    view.setRotation(value);
                    break;
            }
            if (invalidate()) view.invalidate();
        }

        @Override
        protected long duration(View view, float from, float to) {
            switch (property) {
                case X:
                    return (long) (300 * Math.abs(from - to) / view.getContext().getResources().getDisplayMetrics().widthPixels);
                case Y:
                    return (long) (300 * Math.abs(from - to) / view.getContext().getResources().getDisplayMetrics().heightPixels);
                case ALPHA:
                    return (long) (300 * Math.abs(from - to));
                case SCALE:
                    return (long) (300 * Math.abs(from - to));
                case ROTATION:
                    return (long) (300 * Math.abs(from - to) / 360);
            }
            return 0;
        }

        @Override
        protected TimeInterpolator interpolator(View view, float from, float to) {
            return null;
        }

        protected boolean invalidate() {
            return false;
        }
    }

    public static class XManager extends PropertyManager {
        public XManager() {
            super(X);
        }
    }

    public static class YManager extends PropertyManager {
        public YManager() {
            super(Y);
        }
    }

    public static class AlphaManager extends PropertyManager {
        public AlphaManager() {
            super(ALPHA);
        }
    }

    public static class ScaleManager extends PropertyManager {
        public ScaleManager() {
            super(SCALE);
        }
    }

    public static class RotationManager extends PropertyManager {
        public RotationManager() {
            super(ROTATION);
        }
    }
}
