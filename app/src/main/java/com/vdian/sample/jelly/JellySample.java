package com.vdian.sample.jelly;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.vdian.sample.BaseSample;
import com.vdian.sample.R;
import com.vdian.sample.jelly.view.JellyView;

/**
 * Created by zhangliang on 17/12/1.
 */
public class JellySample extends BaseSample {
    public JellySample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        RelativeLayout content = new RelativeLayout(mContext);
        content.setClipChildren(false);
        content.setGravity(Gravity.CENTER);
        JellyView jelly = new JellyView(mContext);
        jelly.setJellyColor(mContext.getResources().getColor(R.color.colorPrimary));
        content.addView(jelly, new ViewGroup.LayoutParams((int) (44 * mContext.getResources().getDisplayMetrics().density), (int) (44 * mContext.getResources().getDisplayMetrics().density)));
        return content;
    }
}
