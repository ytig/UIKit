package com.vdian.sample.refresh.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vdian.uikit.view.extend.refresh.RefreshHintView;

/**
 * Created by zhangliang on 16/10/21.
 */
public class RefreshBottomView extends RefreshHintView {
    private TextView tv;

    public RefreshBottomView(Context context) {
        super(context);
    }

    public RefreshBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int type() {
        return VIEW_TYPE_BOTTOM;
    }

    @Override
    protected long stay() {
        return 0;
    }

    @Override
    protected View build() {
        tv = new TextView(getContext());
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);
        tv.setText("Null");
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (80 * getResources().getDisplayMetrics().density)));
        return tv;
    }

    @Override
    protected void status(int from, int to) {
        switch (to) {
            case STATUS_NORMAL:
                tv.setText("Normal");
                break;
            case STATUS_READY:
                tv.setText("Ready");
                break;
            case STATUS_REFRESH:
                tv.setText("Refresh");
                break;
            case STATUS_WAIT:
                tv.setText("Wait");
                break;
            case STATUS_END:
                tv.setText("End");
                break;
            case STATUS_BAN:
                tv.setText("Ban");
                break;
        }
    }

    @Override
    protected void layout(int height) {
        setTranslationY(height);
    }

    @Override
    protected void scroll(float offset) {
    }
}
