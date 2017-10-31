package com.vdian.sample;

import android.content.Context;
import android.view.View;

/**
 * Created by zhangliang on 16/10/19.
 */
public abstract class BaseSample {
    protected Context mContext;

    public BaseSample(Context context) {
        mContext = context;
    }

    public abstract View init();
}
