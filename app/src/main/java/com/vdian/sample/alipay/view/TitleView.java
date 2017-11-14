package com.vdian.sample.alipay.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vdian.refresh.RefreshHintView;
import com.vdian.refresh.RefreshView;
import com.vdian.uikit.view.ViewGroupMonitor;
import com.vdian.wrapper.recycler.AppendWrapper;

import java.util.Collections;
import java.util.List;

/**
 * Created by zhangliang on 16/10/24.
 */
public class TitleView extends RelativeLayout {
    public RelativeLayout title0;
    public RelativeLayout title1;
    public RelativeLayout title2;
    public RefreshView refresh;
    public ReformView recycler;
    private RefreshHintView hint;

    public TitleView(Context context) {
        super(context);
        init();
    }

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        title0 = new RelativeLayout(getContext());
        title0.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        title0.setId(title0.hashCode());

        title1 = new RelativeLayout(getContext());
        title1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        title1.setId(title1.hashCode());

        title2 = new RelativeLayout(getContext());
        LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.BELOW, title0.getId());
        lp2.addRule(RelativeLayout.BELOW, title1.getId());
        title2.setLayoutParams(lp2);

        refresh = new RefreshView(getContext());
        LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp1.addRule(RelativeLayout.BELOW, title0.getId());
        lp1.addRule(RelativeLayout.BELOW, title1.getId());
        refresh.setLayoutParams(lp1);
        refresh.auto(5);
        recycler = new ReformView(getContext());
        recycler.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        refresh.addView(recycler);
        hint = new InstagramView(getContext());
        hint.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        refresh.addView(hint);

        addView(refresh);
        addView(title2);
        addView(title1);
        addView(title0);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                LayoutParams lp;
                lp = (LayoutParams) hint.getLayoutParams();
                lp.setMargins(0, title2.getHeight(), 0, 0);
                hint.setLayoutParams(lp);
            }
        });

        ViewGroupMonitor.localMonitor(recycler, new ViewGroupMonitor.ChildListener() {
            @Override
            public List<ViewGroupMonitor.Child> handle(List<ViewGroupMonitor.Child> children) {
                int unvisible = title2.getHeight();
                float value = 1;
                if (children.size() > 0) {
                    Collections.sort(children);
                    if (children.get(0).position == 0) {
                        unvisible = -children.get(0).rect.top;
                        if (unvisible < 0) unvisible = 0;
                        if (unvisible > title2.getHeight()) unvisible = title2.getHeight();
                        value = ((float) unvisible) / title2.getHeight();
                    }
                }
                if (value < 0.25f) {
                    setAlpha(title0, 1 - value / 0.25f);
                    setAlpha(title1, 0);
                } else {
                    setAlpha(title0, 0);
                    setAlpha(title1, (value - 0.25f) / (1 - 0.25f));
                }
                if (title2.getTranslationY() != -unvisible) {
                    title2.setTranslationY(-unvisible);
                    title2.scrollTo(0, -unvisible / 2);
                    title2.invalidate();
                }
                setAlpha(title2, 1 - value);
                if (value != 0 && hint.getAlpha() != 0) hint.setAlpha(0);
                if (value == 0 && hint.getAlpha() != 1) hint.setAlpha(1);
                return children;
            }
        });
    }

    private void setAlpha(ViewGroup group, float alpha) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            child.setAlpha(alpha);
        }
    }

    public void setTitleColor(int color) {
        title1.setBackgroundColor(color);
        title2.setBackgroundColor(color);
    }

    public RecyclerView.Adapter wrapper(RecyclerView.Adapter adapter) {
        return new MyWrapper(adapter);
    }

    private class MyWrapper extends AppendWrapper {
        private TextView header;
        private TextView footer;

        public MyWrapper(RecyclerView.Adapter adapter) {
            super(adapter);
            header = new TextView(getContext());
            footer = new TextView(getContext());
            footer.setWidth(1);
            footer.setHeight(1);
            addHeaderView(header).addFooterView(footer);
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    header.setWidth(title2.getWidth());
                    header.setHeight(title2.getHeight());
                }
            });
        }
    }
}
