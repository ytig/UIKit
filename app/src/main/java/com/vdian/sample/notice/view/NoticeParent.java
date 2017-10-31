package com.vdian.sample.notice.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.vdian.uikit.util.rect.RectUtil;
import com.vdian.uikit.view.helper.TouchController;
import com.vdian.uikit.view.helper.TransitionController;

/**
 * Created by zhangliang on 16/11/22.
 */
public class NoticeParent extends RelativeLayout implements TouchController.TouchListener, TransitionController.TransitionListener {
    public RelativeLayout top;
    public RelativeLayout center;
    public RelativeLayout bottom;
    private ColorDrawable bg;
    private boolean move = false;
    private boolean open = false;
    private float limit;
    private float parallax;
    private TouchController delegate;
    private TransitionController animation;
    private boolean judge = false;
    private boolean intercept = false;
    private float downX;
    private float downY;
    private NoticePatch patch;

    public NoticeParent(Context context) {
        super(context);
        init();
    }

    public NoticeParent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoticeParent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        bg = new ColorDrawable(Color.argb(200, 0, 0, 0));
        setBackgroundDrawable(bg);
        bottom = new RelativeLayout(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        bottom.setLayoutParams(lp);
        addView(bottom);
        center = new RelativeLayout(getContext());
        center.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(center);
        top = new RelativeLayout(getContext());
        addView(top);
        setVisibility(View.INVISIBLE);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean step = false;

            @Override
            public void onGlobalLayout() {
                if (!step) {
                    step = true;
                    patch.centerLayout(top.getChildAt(0).getHeight(), bottom.getChildAt(0).getHeight());
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    setVisibility(View.VISIBLE);
                    animation.initDisplay();
                }
            }
        });
        limit = 0.4f * getContext().getResources().getDisplayMetrics().density;
        parallax = 64 * getContext().getResources().getDisplayMetrics().density;
        delegate = new TouchController(this);
        animation = new TransitionController(this, this);
        patch = new NoticePatch() {
            @Override
            public boolean judgeIntercept(float downX, float downY) {
                return false;
            }

            @Override
            public void centerLayout(int top, int bottom) {
                LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, top, 0, bottom);
                center.getChildAt(0).setLayoutParams(lp);
            }

            @Override
            public void centerDisplay(float value, float parallax) {
                center.scrollTo(0, (int) toCoordinate(value));
            }

            @Override
            public void resetNotice() {
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                intercept = false;
                judge = patch.judgeIntercept(downX, downY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (judge && event.getRawY() != downY) {
                    judge = false;
                    if (event.getRawY() < downY) intercept = true;
                }
                break;
            default:
                judge = false;
                break;
        }
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
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return intercept || animation.getValue() != 1 || animation.getTarget() != 1;
    }

    @Override
    public boolean down(float downX, float downY) {
        if (animation.getValue() == 0 && animation.getTarget() == 0) {
            if (downY - RectUtil.measure(this).top > 20 * getResources().getDisplayMetrics().density)
                return false;
        }
        move = false;
        animation.setValue(animation.getValue());
        return true;
    }

    @Override
    public boolean move(float moveX, float moveY) {
        if (moveY != 0) move = true;
        float value = toValue(toCoordinate(animation.getValue()) - moveY);
        if (value < 0) value = 0;
        if (value > 1) value = 1;
        animation.setValue(value);
        return true;
    }

    @Override
    public void up(float velocityX, float velocityY) {
        if (!move) velocityY = 0;
        boolean open;
        if (velocityY > limit) open = true;
        else {
            if (velocityY < -limit) open = false;
            else {
                if (animation.getValue() >= (this.open ? 1f : 0.5f)) open = true;
                else open = false;
            }
        }
        if (open) animation.setTarget(1);
        else animation.setTarget(0);
        this.open = open;
        check();
    }

    @Override
    public void cancel() {
        up(0, 0);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        animation.computeScroll();
    }

    @Override
    public float speed(float value, float target) {
        return (float) (2 * Math.sqrt(target == 1 ? value : (1 - value)) / 500f);
    }

    @Override
    public void display(View self, float value) {
        patch.centerDisplay(value, parallax);
        float visable = toCoordinate(1 - value);
        float value1 = 0;
        if (visable < top.getChildAt(0).getHeight() + parallax)
            value1 = 1 - visable / (top.getChildAt(0).getHeight() + parallax);
        top.scrollTo(0, (int) (value1 * top.getChildAt(0).getHeight()));
        float value2 = value < 0.25f ? 1 : (1 - value) / 0.75f;
        bottom.scrollTo(0, (int) (-value2 * bottom.getChildAt(0).getHeight()));
        float value3 = value < 0.5f ? value / 0.5f : 1;
        bg.setAlpha((int) (255 * value3));
        check();
    }

    private float toCoordinate(float value) {
        return (1 - value) * (center.getChildAt(0).getBottom());
    }

    private float toValue(float coordinate) {
        return 1 - coordinate / (center.getChildAt(0).getBottom());
    }

    private void check() {
        if (!delegate.isTouch() && animation.getValue() == 0 && animation.getTarget() == 0)
            patch.resetNotice();
    }

    public void setNoticePatch(NoticePatch patch) {
        this.patch = patch;
    }

    public interface NoticePatch {
        boolean judgeIntercept(float downX, float downY);

        void centerLayout(int top, int bottom);

        void centerDisplay(float value, float parallax);

        void resetNotice();
    }
}
