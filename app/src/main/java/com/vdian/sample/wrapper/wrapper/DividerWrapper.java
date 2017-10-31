package com.vdian.sample.wrapper.wrapper;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.vdian.uikit.wrapper.recycler.ReplaceWrapper;

/**
 * Created by zhangliang on 16/11/3.
 */
public class DividerWrapper extends ReplaceWrapper {
    public DividerWrapper(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    protected ViewGroup onReplaceViewGroup(ViewGroup parent, int viewType) {
        return new DividerView(parent.getContext());
    }

    private static class DividerView extends LinearLayout implements ReplaceEvent {
        protected DividerView(Context context) {
            super(context);
        }

        @Override
        public void create(View view) {
            setOrientation(LinearLayout.VERTICAL);
            addView(view);
            View divider = new View(getContext());
            int height = (int) (1 * getContext().getResources().getDisplayMetrics().density);
            if (height < 1) height = 1;
            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            divider.setBackgroundColor(Color.LTGRAY);
            addView(divider);
        }

        @Override
        public void bind() {
        }
    }
}
