package com.vdian.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.RelativeLayout;

/**
 * Created by zhangliang on 16/10/10.
 */
public abstract class RefreshHintView extends RelativeLayout implements RefreshView.HintEvent {
    protected static final int VIEW_TYPE_TOP = 0; //顶部加载控件
    protected static final int VIEW_TYPE_BOTTOM = 1; //底部加载控件
    protected static final int VIEW_TYPE_AUTO = 2; //自动加载控件
    protected static final int STATUS_NULL = 0; //初始状态(T,B,A)
    protected static final int STATUS_NORMAL = 1; //普通状态(T,B,A)
    protected static final int STATUS_READY = 2; //就绪状态(T,B)
    protected static final int STATUS_REFRESH = 3; //加载状态(T,B,A)
    protected static final int STATUS_WAIT = 4; //等待状态(B,A)
    protected static final int STATUS_END = 5; //结束状态(B,A)
    protected static final int STATUS_BAN = 6; //禁止状态(B,A)
    protected static final int STATUS_ERROR = 7; //异常状态(A)

    private boolean mTop = false;
    private boolean mBottom = false;
    private boolean mEnd = false;
    private boolean mBan = false;
    private boolean mError = false;
    protected int mStatus = STATUS_NULL;
    protected float mOffset = 0;
    protected float mLimit = 0;

    public RefreshHintView(Context context) {
        super(context);
        init();
    }

    public RefreshHintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RefreshHintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = build();
        if (view != null) addView(view);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        ViewParent parent = getParent();
        if (parent instanceof RefreshView) {
            boolean horizontal = RefreshCompat.ViewOrientation.isHorizontal(((RefreshView) parent).getNestedChild());
            switch (type()) {
                case VIEW_TYPE_TOP:
                    mLimit = horizontal ? getWidth() : getHeight();
                    ((RefreshView) parent).setTopLimit(mLimit, stay());
                    break;
                case VIEW_TYPE_BOTTOM:
                    mLimit = horizontal ? getWidth() : getHeight();
                    ((RefreshView) parent).setBottomLimit(mLimit, stay());
                    break;
            }
        }
    }

    @Override
    public void refreshStatus(boolean top, boolean bottom, boolean end, boolean ban, boolean error) {
        mTop = top;
        mBottom = bottom;
        mEnd = end;
        mBan = ban;
        mError = error;
        update();
    }

    @Override
    public void globalLayout(int height) {
        layout(height);
    }

    @Override
    public void overScroll(float offset) {
        scroll(offset);
        mOffset = offset;
        update();
    }

    private void update() {
        int status = STATUS_NORMAL;
        switch (type()) {
            case VIEW_TYPE_TOP:
                if (mTop) status = STATUS_REFRESH;
                else {
                    if (mOffset > mLimit) status = STATUS_READY;
                    else status = STATUS_NORMAL;
                }
                break;
            case VIEW_TYPE_BOTTOM:
                if (mEnd) status = STATUS_END;
                else {
                    if (mBan) status = STATUS_BAN;
                    else {
                        if (mTop) status = STATUS_WAIT;
                        else {
                            if (mBottom) status = STATUS_REFRESH;
                            else {
                                if (mOffset < -mLimit) status = STATUS_READY;
                                else status = STATUS_NORMAL;
                            }
                        }
                    }
                }
                break;
            case VIEW_TYPE_AUTO:
                if (mEnd) status = STATUS_END;
                else {
                    if (mBan) status = STATUS_BAN;
                    else {
                        if (mError) status = STATUS_ERROR;
                        else {
                            if (mTop) status = STATUS_WAIT;
                            else {
                                if (mBottom) status = STATUS_REFRESH;
                                else status = STATUS_NORMAL;
                            }
                        }
                    }
                }
                break;
        }
        if (mStatus == status) return;
        status(mStatus, status);
        mStatus = status;
    }

    protected abstract int type(); //控件类型

    protected abstract long stay(); //暂留时长

    protected abstract View build(); //初始化控件

    protected abstract void status(int from, int to); //状态变化

    protected abstract void layout(int height); //布局变化

    protected abstract void scroll(float offset); //越界滚动
}
