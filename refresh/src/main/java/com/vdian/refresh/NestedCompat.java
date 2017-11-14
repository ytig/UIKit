package com.vdian.refresh;

import android.support.v4.view.NestedScrollingParent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.vdian.uikit.view.TouchController;

/**
 * Created by zhangliang on 16/12/15.
 */
public class NestedCompat {
    /**
     * 嵌套滑动父视图触控控制器
     */
    protected static class NestedParentController {
        public static boolean MIUI_COMPAT = false; //兼容MIUI触控突变

        private int mAction = 0; //事件类型
        private float deltaX = 0; //横向偏移量
        private float deltaY = 0; //纵向偏移量
        private boolean didMove = false; //是否已执行移动事件
        private boolean doConsume = false; //是否正在触发中
        private TouchController mSuper; //基础触控代理
        private NestedParentListener mListener; //嵌套滑动处理实现器

        public NestedParentController(NestedParentListener listener) {
            mListener = listener;
            mSuper = new TouchController(new TouchController.TouchListener() {
                @Override
                public boolean down(float downX, float downY) {
                    return true;
                }

                @Override
                public boolean move(float moveX, float moveY) {
                    if (MIUI_COMPAT && !doConsume) {
                        deltaX = moveX == 0 ? 0 : (moveX > 0 ? 1 : -1);
                        deltaY = moveY == 0 ? 0 : (moveY > 0 ? 1 : -1);
                    } else {
                        deltaX = moveX;
                        deltaY = moveY;
                    }
                    return false;
                }

                @Override
                public void up(float velocityX, float velocityY) {
                }

                @Override
                public void cancel() {
                }
            });
        }

        public boolean isTouch() {
            return mSuper.isTouch();
        }

        public void beforeDispatchTouchEvent(MotionEvent event) {
            mAction = event.getAction();
            deltaX = 0;
            deltaY = 0;
            didMove = false;
            switch (mAction) {
                case MotionEvent.ACTION_DOWN:
                    doConsume = false; //重置触发状态
                    mListener.down();
                    break;
            }
            mSuper.dispatchTouchEvent(event); //进行数据采集
            mSuper.onTouchEvent(event); //获取采集数据
        }

        public void afterDispatchTouchEvent(MotionEvent event) {
            if (!didMove && doConsume) mListener.move(-deltaX, -deltaY); //补全移动事件
            switch (mAction) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mListener.up();
                    break;
            }
        }

        public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
            didMove = true;
            if (doConsume != mListener.move(-deltaX, -deltaY)) { //移动事件
                doConsume = !doConsume;
                if (!doConsume) mListener.unmove(-deltaX, -deltaY); //归还移动事件
            }
            if (doConsume) { //触发中禁止子控件滚动
                consumed[0] += dx;
                consumed[1] += dy;
            }
        }

        public interface NestedParentListener {
            void down();

            boolean move(float dx, float dy);

            void unmove(float dx, float dy);

            void up();
        }
    }

    /**
     * 伪嵌套滑动子视图触控控制器
     */
    public abstract static class NestedChildController {
        private float downX = 0; //落点横向坐标
        private float downY = 0; //落点纵向坐标
        private boolean isSingle = false; //是否为单指触控
        private boolean needJudge = false; //是否需要方向判断
        private boolean doJudge = false; //是否进行方向判断
        private float mLimit = 0; //判断触发门限
        private boolean doConsume = false; //是否触发嵌套滑动
        private boolean mConsume = false; //是否正在触发中
        private int[] mConsumed = new int[2]; //嵌套滑动消耗量
        private View mView; //嵌套滑动子视图

        public NestedChildController(View child, boolean xCancel) {
            mView = child;
            mLimit = 4 * child.getContext().getResources().getDisplayMetrics().density;
            needJudge = xCancel;
        }

        public boolean overrideDispatchTouchEvent(MotionEvent event) {
            boolean obtain = false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    isSingle = true;
                    doJudge = needJudge;
                    doConsume = !needJudge;
                    mConsume = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (doJudge) {
                        float deltaX = Math.abs(event.getRawX() - downX);
                        float deltaY = Math.abs(event.getRawY() - downY);
                        if (Math.abs(deltaX) > mLimit || Math.abs(deltaY) > mLimit) {
                            if (deltaY > deltaX) doConsume = true; //持续触发嵌套滑动
                            doJudge = false;
                        }
                    }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    break;
                default:
                    isSingle = false; //发生多指触控
                    doJudge = false;
                    break;
            }
            if (doConsume || event.getAction() == MotionEvent.ACTION_DOWN) {
                boolean consume = false;
                ViewParent parent = mView.getParent();
                if (parent instanceof NestedScrollingParent) {
                    mConsumed[0] = 0;
                    mConsumed[1] = 0;
                    ((NestedScrollingParent) parent).onNestedPreScroll(mView, 1, 1, mConsumed);
                    if (mConsumed[0] != 0 || mConsumed[1] != 0) consume = true;
                }
                if (consume) doConsume = true; //持续触发嵌套滑动
                if (mConsume && !isSingle) event = null; //多指并触发则截断事件
                else {
                    if (mConsume == consume) {
                        if (mConsume) event = null; //触发中拦截事件
                    } else {
                        mConsume = consume;
                        if (mConsume) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) event = null;
                            else {
                                obtain = true;
                                event = MotionEvent.obtain(event);
                                event.setAction(MotionEvent.ACTION_CANCEL); //伪造取消事件
                            }
                        } else {
                            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
                                event = null;
                            else {
                                obtain = true;
                                event = MotionEvent.obtain(event);
                                event.setAction(MotionEvent.ACTION_DOWN); //伪造落点事件
                            }
                        }
                    }
                }
            }
            if (event != null) {
                superDispatchTouchEvent(event);
                if (obtain) event.recycle();
            }
            return true;
        }

        protected abstract boolean superDispatchTouchEvent(MotionEvent event); //原触控事件
    }
}
