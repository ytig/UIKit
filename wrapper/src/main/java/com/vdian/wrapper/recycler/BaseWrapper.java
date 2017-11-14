package com.vdian.wrapper.recycler;

import android.support.v7.widget.RecyclerView;

import com.vdian.wrapper.Wrapper;

/**
 * Created by zhangliang on 16/11/1.
 */
public abstract class BaseWrapper extends RecyclerView.Adapter implements Wrapper {
    protected RecyclerView.Adapter mAdapter;

    public BaseWrapper(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public Object getChild() {
        return mAdapter;
    }

    @Override
    public int toChildPosition(int position) {
        if (position < 0) return NO_POSITION;
        if (position >= mAdapter.getItemCount()) return NO_POSITION;
        return position;
    }
}
