package com.vdian.refresh;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;

import com.vdian.uikit.view.helper.TransitionController;

import java.lang.ref.WeakReference;

/**
 * Created by zhangliang on 16/10/8.
 */
public class RefreshView extends NestedParent implements TransitionController.TransitionListener, RefreshCompat.ViewMonitor.OnScrollListener {
    public final static int CALLBACK_NORMAL = 1; //正常加载回调
    public final static int CALLBACK_END = 2; //数据页尾回调
    public final static int CALLBACK_ERROR = 3; //网络异常回调

    private boolean horizontal = false; //嵌套滚动方向
    private float range = (getContext().getResources().getDisplayMetrics().widthPixels + getContext().getResources().getDisplayMetrics().heightPixels) / 2; //越界偏移范围
    private float touch = 0.346f; //越界触控常数
    private float power = 0.75f; //回弹动画常数
    private long duration = 618L; //回弹动画均时
    private boolean scrollToNormal = false; //强制归位模式
    private boolean doReform = false; //是否复位控件
    private boolean monitorPermit = false; //允许监控滚动
    private boolean overshotPermit = false; //允许衔接滑动
    private DampingController mAnimation = new DampingController(); //动画控制器
    private StayHandler mDelayer = new StayHandler(this); //动画暂留器
    private boolean topPermit = true; //顶部允许越界
    private boolean bottomPermit = true; //底部允许越界
    private float topLimit = 0; //顶部加载门限
    private float bottomLimit = 0; //底部加载门限
    private long topStay = 0; //顶部加载暂留
    private long bottomStay = 0; //底部加载暂留
    private boolean topRefresh = false; //顶部加载中
    private boolean bottomRefresh = false; //底部加载中
    private boolean endRefresh = false; //无更多数据
    private boolean banRefresh = true; //无首页数据
    private boolean errorRefresh = false; //非首页加载异常
    private int autoRefresh = -1; //自动加载触发量
    private RefreshTopListener mRefreshTopListener; //顶部加载监听器
    private RefreshBottomListener mRefreshBottomListener; //底部加载监听器

    public RefreshView(Context context) {
        super(context);
        init();
    }

    public RefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            private boolean attached = false;
            private boolean monitor = false;
            private ViewTreeObserver.OnGlobalLayoutListener layout = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    horizontal = RefreshCompat.ViewOrientation.isHorizontal(getNestedChild()); //滚动方向识别
                    int height = RefreshCompat.ViewVisibility.measureVisibility(getNestedChild()); //实时测量控件
                    for (int i = getChildCount() - 1; i >= 0; i--) {
                        View child = getChildAt(i);
                        if (child instanceof HintEvent) ((HintEvent) child).globalLayout(height);
                    }
                }
            };
            private ViewTreeObserver.OnPreDrawListener draw = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (monitorPermit && !monitor)
                        monitor = RefreshCompat.ViewMonitor.registerMonitor(RefreshView.this, getNestedChild()); //控件滚动监控
                    if (autoRefresh >= 0 && !errorRefresh && RefreshCompat.ViewBottom.isBottom(autoRefresh, getNestedChild()))
                        refresh(false); //底部自动加载
                    return true;
                }
            };

            @Override
            public void onViewAttachedToWindow(View v) {
                if (attached) return;
                attached = true;
                getViewTreeObserver().addOnGlobalLayoutListener(layout);
                getViewTreeObserver().addOnPreDrawListener(draw);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (!attached) return;
                attached = false;
                getViewTreeObserver().removeOnPreDrawListener(draw);
                getViewTreeObserver().removeGlobalOnLayoutListener(layout);
            }
        });
    }

    /**
     * 越界滚动许可
     *
     * @param top
     * @param bottom
     */
    public void permit(boolean top, boolean bottom) {
        topPermit = top;
        bottomPermit = bottom;
    }

    /**
     * 越界触控转移
     *
     * @param delegate
     */
    public void delegate(boolean delegate) {
        setTouchDelegate(delegate ? new TouchDelegate(new Rect(), this) {
            private View child;

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        child = getNestedChild();
                        break;
                }
                child = (child != null && RefreshCompat.ViewDelegate.delegateTouch(event, child)) ? child : null;
                switch (action) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        child = null;
                        break;
                }
                return true;
            }
        } : null);
    }

    /**
     * 边缘滑动转移
     *
     * @param overshoot
     */
    public void overshoot(boolean overshoot) {
        monitorPermit = true; //开启滚动监控
        overshotPermit = overshoot;
    }

    /**
     * 底部自动加载模式
     *
     * @param advance
     */
    public void auto(int advance) {
        autoRefresh = advance;
    }

    /**
     * 进行顶部或底部加载
     *
     * @param isTop
     * @return
     */
    public boolean refresh(boolean isTop) {
        if (isTop) {
            if (refreshPrepare(true)) {
                mRefreshTopListener.topRefresh();
                return true;
            }
        } else {
            if (refreshPrepare(false)) {
                mRefreshBottomListener.bottomRefresh();
                return true;
            }
        }
        return false;
    }

    /**
     * 加载回调，需要会话管理，防止脏回调
     *
     * @param isTop
     * @param status
     */
    public void callback(boolean isTop, int status) {
        boolean success = false;
        if (isTop) {
            if (topRefresh) {
                topRefresh = false;
                success = true;
            }
        } else {
            if (bottomRefresh) {
                bottomRefresh = false;
                success = true;
            }
        }
        if (success) {
            switch (status) {
                case CALLBACK_END:
                    endRefresh = true;
                    break;
                case CALLBACK_ERROR:
                    if (isTop) banRefresh = true;
                    else errorRefresh = true;
                    break;
            }
            refreshStatus();
            float target = mAnimation.getTarget();
            if (!touching() && target != 0.5f) {
                mDelayer.clear();
                mDelayer.post(isTop ? (target > 0.5f ? topStay : 0) : (target < 0.5f ? bottomStay : 0)); //暂留回弹动画
            }
        }
    }

    /**
     * 续载会话
     */
    public void resume() {
        endRefresh = false;
        refreshStatus();
    }

    /**
     * 强制顶部加载，需要会话管理与内容置顶（慎用）
     *
     * @param anim
     * @return
     */
    public boolean violate(boolean anim) {
        if (anim) {
            if (!touching() && !scrollToNormal && mAnimation.getValue() >= 0.5f)
                mAnimation.setTarget(topStay >= 0 ? toValue(topLimit) : 0.5f); //加载动画
            else anim = false;
        }
        topRefresh = false;
        if (refreshPrepare(true)) mRefreshTopListener.topRefresh();
        return anim;
    }

    protected View getNestedChild() {
        return getChildAt(0);
    }

    protected void setTopLimit(float limit, long stay) {
        topLimit = Math.abs(limit);
        topStay = stay;
    }

    protected void setBottomLimit(float limit, long stay) {
        bottomLimit = Math.abs(limit);
        bottomStay = stay;
    }

    private boolean refreshPrepare(boolean isTop) {
        if (isTop) {
            if (!topRefresh && mRefreshTopListener != null) {
                topRefresh = true;
                bottomRefresh = false;
                endRefresh = false;
                banRefresh = false;
                errorRefresh = false;
                refreshStatus();
                return true;
            }
        } else {
            if (!topRefresh && !bottomRefresh && !endRefresh && !banRefresh && mRefreshBottomListener != null) {
                bottomRefresh = true;
                errorRefresh = false;
                refreshStatus();
                return true;
            }
        }
        return false;
    }

    private void refreshStatus() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof HintEvent)
                ((HintEvent) child).refreshStatus(topRefresh, bottomRefresh, endRefresh, banRefresh, errorRefresh);
        }
    }

    @Override
    public void onStartScroll() {
    }

    @Override
    public void onStopScroll(long timePass, float velocityX, float velocityY) {
        if (horizontal) velocityY = velocityX;
        if (touching()) return;
        if (scrollToNormal) return;
        if (!overshotPermit) return;
        if (mAnimation.getValue() != 0.5f) return;
        if (mAnimation.getTarget() != 0.5f) return;
        if (!(topPermit && RefreshCompat.ViewEdge.isEdge(true, getNestedChild()) && velocityY > 0) && !(bottomPermit && RefreshCompat.ViewEdge.isEdge(false, getNestedChild()) && velocityY < 0))
            return;
        mAnimation.damping(timePass, velocityY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!scrollToNormal) {
            float value = mAnimation.getValue();
            if (value != 0.5f) {
                if (!RefreshCompat.ViewEdge.isEdge(value > 0.5f, getNestedChild())) {
                    scrollToNormal = true;
                    mAnimation.setTarget(0.5f); //强制归位
                }
            }
        }
        doReform = false;
        boolean b = super.dispatchTouchEvent(event);
        if (doReform) RefreshCompat.ViewReform.loadLocation(getNestedChild()); //复位控件
        return b;
    }

    @Override
    public void down() {
        if (scrollToNormal) return; //强制归位不停止动画
        mAnimation.setValue(mAnimation.getValue()); //停止动画
    }

    @Override
    public boolean move(float dx, float dy) {
        if (horizontal) dy = dx;
        if (scrollToNormal) return true; //强制归位禁止滚动
        else {
            if (dy == 0) {
                if ((mAnimation.getValue() != 0.5f)) return true; //禁止子控件滚动
            } else {
                float scrollY = toCoordinate(mAnimation.getValue());
                if (scrollY != 0 || ((dy < 0 ? topPermit : bottomPermit) && RefreshCompat.ViewEdge.isEdge(dy < 0, getNestedChild()))) {
                    boolean negative = scrollY < 0 || (scrollY == 0 && dy > 0);
                    if (negative) {
                        dy *= -1;
                        scrollY *= -1;
                    }
                    double x = range * touch * Math.pow((range / (range - scrollY)), 1 / touch) - range * touch;
                    x = (x * (x - dy) < 0) ? 0 : x - dy;
                    scrollY = (float) (range - range * Math.pow(1 + x / (range * touch), -touch));
                    if (negative) scrollY *= -1;
                    mAnimation.setValue(toValue(scrollY));
                    return true; //禁止子控件滚动
                }
            }
        }
        return false;
    }

    @Override
    public void unmove(float dx, float dy) {
        if (horizontal) dy = dx;
        doReform = RefreshCompat.ViewReform.saveLocation(dy, getNestedChild()); //记录位置用于复位
    }

    @Override
    public void up() {
        if (scrollToNormal) return; //强制归位不触发事件
        float scrollY = toCoordinate(mAnimation.getValue());
        if (scrollY > topLimit) {
            if (refreshPrepare(true)) {
                mAnimation.setTarget(topStay >= 0 ? toValue(topLimit) : 0.5f);
                mRefreshTopListener.topRefresh();
                return;
            }
        }
        if (autoRefresh < 0 && scrollY < -bottomLimit) {
            if (refreshPrepare(false)) {
                mAnimation.setTarget(bottomStay >= 0 ? toValue(-bottomLimit) : 0.5f);
                mRefreshBottomListener.bottomRefresh();
                return;
            }
        }
        mAnimation.setTarget(0.5f); //回弹动画
    }

    @Override
    public boolean fling() {
        return mAnimation.getValue() == 0.5f; //子控件抛掷许可
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        mAnimation.computeScroll();
    }

    @Override
    public float speed(float value, float target) {
        double speed = Math.pow(Math.abs(target - value), power) * Math.pow(0.1, 1 - power) / ((1 - power) * duration);
        return Math.max((float) speed, 1f / (60 * 60 * 1000L));
    }

    @Override
    public void display(View self, float value) {
        if (value == 0.5f) scrollToNormal = false; //强制归位结束
        float offset = toCoordinate(value);
        self.scrollTo(horizontal ? ((int) -offset) : 0, horizontal ? 0 : ((int) -offset));
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof HintEvent) ((HintEvent) child).overScroll(offset);
        }
    }

    private float toCoordinate(float value) {
        return (value - 0.5f) * 2 * range;
    }

    private float toValue(float coordinate) {
        return coordinate / (2 * range) + 0.5f;
    }

    /**
     * 设置顶部与底部加载监听器
     *
     * @param refreshTopListener
     * @param refreshBottomListener
     */
    public void setRefreshListener(RefreshTopListener refreshTopListener, RefreshBottomListener refreshBottomListener) {
        mRefreshTopListener = refreshTopListener;
        mRefreshBottomListener = refreshBottomListener;
        refreshStatus();
    }

    /**
     * 设置顶部加载监听器
     *
     * @param refreshTopListener
     */
    public void setRefreshListener(RefreshTopListener refreshTopListener) {
        setRefreshListener(refreshTopListener, null);
    }

    public interface RefreshTopListener {
        void topRefresh();
    }

    public interface RefreshBottomListener {
        void bottomRefresh();
    }

    protected interface HintEvent {
        void refreshStatus(boolean top, boolean bottom, boolean end, boolean ban, boolean error);

        void globalLayout(int height);

        void overScroll(float offset);
    }

    private class DampingController extends TransitionController {
        private boolean running = false; //执行越界动画
        private long timing = 0; //动画起始时间
        private UnderDamping damping = new UnderDamping(0.00025, 0.0266); //欠阻尼计算器

        public DampingController() {
            super(RefreshView.this, RefreshView.this, 0.5f);
        }

        public void damping(long time, float velocity) {
            if (!running) {
                float value = super.getValue();
                super.setValue(value);
                damping.set(toCoordinate(value), velocity);
                if (time < damping.t1) {
                    running = true;
                    timing = AnimationUtils.currentAnimationTimeMillis() - time;
                    postInvalidate();
                }
            }
        }

        @Override
        public float getValue() {
            return super.getValue();
        }

        @Override
        public void setValue(float value) {
            if (running) running = false;
            mDelayer.clear();
            super.setValue(value);
        }

        @Override
        public float getTarget() {
            if (running) return 0.5f;
            return super.getTarget();
        }

        @Override
        public void setTarget(float target) {
            if (running) running = false;
            mDelayer.clear();
            super.setTarget(target);
        }

        @Override
        public void computeScroll() {
            super.computeScroll();
            if (running) {
                long t = AnimationUtils.currentAnimationTimeMillis() - timing;
                if (t < 0) t = 0;
                if (t < damping.t1) {
                    super.setValue(toValue((float) damping.get(t)));
                    postInvalidate();
                } else {
                    super.setValue(0.5f);
                    running = false;
                }
            }
        }

        private class UnderDamping {
            private double tension; //劲度系数
            private double friction; //摩擦系数
            private double x0; //初始位置
            private double v0; //初始速度
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
    }

    private static class StayHandler extends Handler {
        private WeakReference<RefreshView> reference; //弱引用

        public StayHandler(RefreshView view) {
            reference = new WeakReference<>(view);
        }

        public void post(long stay) {
            if (stay <= 0) handleMessage(null);
            else sendEmptyMessageDelayed(0, stay);
        }

        public void clear() {
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            RefreshView view = reference == null ? null : reference.get();
            if (view != null) view.mAnimation.setTarget(0.5f);
        }
    }
}
