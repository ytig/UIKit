package com.vdian.sample.swap.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 16/12/21.
 */
public class UnRecyclerView extends ScrollView {
    private RelativeLayout mContainer;
    private List<View> mChildren;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.AdapterDataObserver mObserver;

    public UnRecyclerView(Context context) {
        super(context);
        init();
    }

    public UnRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UnRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFillViewport(true);
        mContainer = new RelativeLayout(getContext());
        mContainer.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mContainer);
        mChildren = new ArrayList<>();
        mObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                layoutChild();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                layoutChild();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                layoutChild();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                layoutChild();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                layoutChild();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                layoutChild();
            }
        };
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        if (mAdapter != null) mAdapter.unregisterAdapterDataObserver(mObserver);
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerAdapterDataObserver(mObserver);
            layoutChild();
        }
    }

    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    public int getChildAdapterPosition(View child) {
        for (int i = mChildren.size() - 1; i >= 0; i--) {
            if (child == mChildren.get(i)) return i;
        }
        ViewParent parent = child.getParent();
        if (parent instanceof View) return getChildAdapterPosition((View) parent);
        else return -1;
    }

    protected void layoutChild() {
        mContainer.removeAllViews();
        mChildren.clear();
        int count = mAdapter.getItemCount();
        View tmp = null;
        for (int i = 0; i < count; i++) {
            RecyclerView.ViewHolder holder = mAdapter.onCreateViewHolder(mContainer, mAdapter.getItemViewType(i));
            mAdapter.onBindViewHolder(holder, i);
            mContainer.addView(holder.itemView);
            mChildren.add(holder.itemView);
            if (tmp != null) {
                tmp.setId(i);
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp instanceof RelativeLayout.LayoutParams)
                    ((RelativeLayout.LayoutParams) lp).addRule(RelativeLayout.BELOW, i);
            }
            tmp = holder.itemView;
        }
    }

    protected View getChildAtPosition(int position) {
        return (position >= 0 && position < mChildren.size()) ? mChildren.get(position) : null;
    }

    @Override
    public void bringChildToFront(View child) {
        if (child != null) {
            mContainer.bringChildToFront(child);
            child.requestLayout();
        }
    }
}
